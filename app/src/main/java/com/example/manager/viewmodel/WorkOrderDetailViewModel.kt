package com.example.manager.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.OrderItemStatusLog
import com.example.manager.data.model.enums.OrderItemStatus
import com.example.manager.data.model.uimodel.WorkOrderItem
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.CustomerRepository
import com.example.manager.data.repository.OrderRepository
import com.example.manager.data.repository.StaffRepository
import com.example.manager.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkOrderDetailUiState(
    val workOrderItem: WorkOrderItem? = null,
    val statusLogs: List<OrderItemStatusLog> = emptyList(),
    val staffNames: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = true, // 初始为 true
    val errorMessage: String? = null,
    val isUpdatingStatus: Boolean = false
)

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

    init {
        if (orderItemId != -1L) {
            // 启动一个主协程来加载所有数据
            viewModelScope.launch {
                loadInitialDetails()
            }
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "无效的工单ID") }
        }
    }

    private suspend fun loadInitialDetails() {
        _uiState.update { it.copy(isLoading = true) }

        val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
        if (storeId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "无法获取店铺信息") }
            return
        }

        // 1. 加载工单本身的基础信息
        try {
            val orderItem = orderRepository.getOrderItemById(orderItemId)
            if (orderItem == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "工单不存在") }
                return
            }
            val order = orderRepository.getOrderByIdAndStoreId(orderItem.orderId, storeId)
            if (order == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "关联的订单不存在于本店") }
                return
            }
            val customerName = order.customerId?.let { customerRepository.getCustomerByIdAndStoreId(it, storeId)?.name }
            val workOrderItem = WorkOrderItem(orderItem, order.orderNumber, customerName, storeId)

            _uiState.update { it.copy(workOrderItem = workOrderItem) }

        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "加载工单基础信息失败: ${e.message}") }
            return // 如果基础信息加载失败，则不继续加载日志
        }

        // 2. 订阅日志信息和加载操作员姓名
        orderRepository.getLogsForOrderItemFlow(orderItemId)
            .catch { e ->
                Log.e("WorkOrderDetailVM", "Error collecting status logs.", e)
                _uiState.update { it.copy(errorMessage = "加载状态历史失败") }
            }
            .collect { logs ->
                val staffIds = logs.map { it.staffId }.distinct()
                val staffNames = if (staffIds.isNotEmpty()) {
                    try {
                        staffRepository.getStaffByIds(staffIds).associateBy({ it.id }, { it.name })
                    } catch (e: Exception) {
                        Log.e("WorkOrderDetailVM", "Failed to get staff names for logs.", e)
                        emptyMap()
                    }
                } else {
                    emptyMap()
                }

                _uiState.update {
                    it.copy(
                        statusLogs = logs,
                        staffNames = staffNames,
                        isLoading = false // 所有数据加载完毕
                    )
                }
            }
    }

    fun updateStatus(newStatus: OrderItemStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null) }

            // **修正解构声明**
            val session = sessionManager.userSessionFlow.firstOrNull()
            val storeId = session?.storeId
            val staffId = session?.staffId

            if (storeId == null || staffId == null) {
                _uiState.update { it.copy(isUpdatingStatus = false, errorMessage = "无法获取用户信息，操作失败") }
                return@launch
            }

            orderRepository.updateOrderItemStatus(orderItemId, newStatus, staffId, storeId)
                .onSuccess { success ->
                    if (success) {
                        Log.d("WorkOrderDetailVM", "Status updated successfully to $newStatus")
                        // 因为日志 Flow 会自动更新，所以 UI 状态会自动刷新
                    } else {
                        // 这个分支可能在 updateOrderItemStatus 中不会出现，因为它会抛异常
                        Log.w("WorkOrderDetailVM", "updateOrderItemStatus returned success but false")
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = "状态更新失败: ${e.localizedMessage}") }
                }

            _uiState.update { it.copy(isUpdatingStatus = false) } // 无论成功失败，都清除加载状态
        }
    }

    fun errorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}