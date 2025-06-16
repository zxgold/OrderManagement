package com.example.manager.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.OrderItemStatusLog
import com.example.manager.data.model.enums.OrderItemStatus
import com.example.manager.data.model.uimodel.WorkOrderItem
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.*
import com.example.manager.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkOrderDetailUiState(
    val workOrderItem: WorkOrderItem? = null,
    val statusLogs: List<OrderItemStatusLog> = emptyList(),
    val staffNames: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isUpdatingStatus: Boolean = false,
    val updateSuccessMessage: String? = null // 用于显示成功的提示
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkOrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val staffRepository: StaffRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkOrderDetailUiState())
    val uiState: StateFlow<WorkOrderDetailUiState> = _uiState.asStateFlow()

    private val orderItemId: Long = savedStateHandle[AppDestinations.WORK_ORDER_DETAIL_ARG_ID] ?: -1L

    // 一个触发器，用于手动刷新数据
    private val refreshTrigger = MutableStateFlow(0)

    init {
        if (orderItemId != -1L) {
            // 使用 combine 将所有数据源组合成一个统一的 UI 状态流
            viewModelScope.launch {
                // Flow for the main WorkOrderItem details
                val workOrderItemFlow = refreshTrigger.flatMapLatest {
                    flow { emit(loadWorkOrderItemDetails()) }
                }

                // Flow for the status logs
                val logsFlow = orderRepository.getLogsForOrderItemFlow(orderItemId)

                // Combine them all
                combine(workOrderItemFlow, logsFlow) { workOrderItemResult, logs ->
                    val workOrderItem = workOrderItemResult?.getOrNull()
                    val staffIds = logs.map { it.staffId }.distinct()
                    val staffNames = if (staffIds.isNotEmpty()) {
                        try {
                            staffRepository.getStaffByIds(staffIds).associateBy({ it.id }, { it.name })
                        } catch (e: Exception) { emptyMap() }
                    } else {
                        emptyMap()
                    }

                    // 更新 UI State
                    _uiState.update {
                        it.copy(
                            workOrderItem = workOrderItem,
                            statusLogs = logs,
                            staffNames = staffNames,
                            isLoading = false, // 加载完成
                            errorMessage = workOrderItemResult?.exceptionOrNull()?.localizedMessage
                        )
                    }
                }.catch { e ->
                    Log.e("WorkOrderDetailVM", "Error in combined flow.", e)
                    _uiState.update { it.copy(isLoading = false, errorMessage = "加载数据时发生错误") }
                }.collect()
            }
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "无效的工单ID") }
        }
    }

    private suspend fun loadWorkOrderItemDetails(): Result<WorkOrderItem?> {
        return try {
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId == null) throw IllegalStateException("无法获取店铺信息")

            val orderItem = orderRepository.getOrderItemById(orderItemId) ?: return Result.success(null) // 工单不存在
            val order = orderRepository.getOrderByIdAndStoreId(orderItem.orderId, storeId) ?: throw IllegalStateException("关联的订单不存在于本店")
            val customerName = order.customerId?.let { customerRepository.getCustomerByIdAndStoreId(it, storeId)?.name }

            Result.success(WorkOrderItem(orderItem, order.orderNumber, customerName, storeId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateStatus(newStatus: OrderItemStatus) {
        val currentStatus = _uiState.value.workOrderItem?.orderItem?.status
        if (currentStatus == null) {
            _uiState.update { it.copy(errorMessage = "当前工单状态未知，无法更新。") }
            return
        }

        // --- 状态更新逻辑校验 ---
        if (newStatus.ordinal != currentStatus.ordinal + 1) {
            _uiState.update { it.copy(errorMessage = "操作无效：工单状态不能跳跃更新。") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null, updateSuccessMessage = null) }


            viewModelScope.launch {
                _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null, updateSuccessMessage = null) }

                // --- 这是修正后的部分 ---
                val session = sessionManager.userSessionFlow.firstOrNull()
                val storeId = session?.storeId
                val staffId = session?.staffId
                // -----------------------

                if (storeId == null || staffId == null) {
                    _uiState.update { it.copy(isUpdatingStatus = false, errorMessage = "无法获取用户信息，操作失败。") }
                    return@launch
                }

                orderRepository.updateOrderItemStatus(orderItemId, newStatus, staffId, storeId)
                    .onSuccess { success ->
                        if (success) {
                            Log.d("WorkOrderDetailVM", "Status updated successfully to $newStatus")
                            // **手动调用一次加载方法来刷新工单详情**
                            loadWorkOrderItemDetails() // 这会获取最新的 OrderItem 并更新 _uiState
                            _uiState.update { it.copy(updateSuccessMessage = "状态已更新为: ${newStatus.name}") }

                            if (newStatus == OrderItemStatus.INSTALLED) {
                                _uiState.value.workOrderItem?.let { workItem -> // 使用 let 确保 workOrderItem 非空
                                    val orderId = workItem.orderItem.orderId
                                    val storeId = workItem.storeId
                                    val completed = orderRepository.checkAndCompleteOrder(orderId, storeId) // <-- 传递 storeId
                                    if (completed) {
                                        Log.i("WorkOrderDetailVM", "Order $orderId has been auto-completed.")
                                    }
                                }
                            }
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(errorMessage = "状态更新失败: ${e.localizedMessage}") }
                    }

                _uiState.update { it.copy(isUpdatingStatus = false) }
            }
        }
    }

    fun manualRefresh() {
        refreshTrigger.value++
    }

    fun errorShown() { _uiState.update { it.copy(errorMessage = null) } }
    fun successMessageShown() { _uiState.update { it.copy(updateSuccessMessage = null) } }
}