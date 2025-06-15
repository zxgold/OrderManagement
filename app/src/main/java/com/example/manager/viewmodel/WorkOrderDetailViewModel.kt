package com.example.manager.viewmodel

// ... imports ...
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.manager.data.model.entity.OrderItemStatusLog
import com.example.manager.data.model.uimodel.WorkOrderItem
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.CustomerRepository
import com.example.manager.data.repository.OrderRepository
import com.example.manager.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkOrderDetailUiState(
    val workOrderItem: WorkOrderItem? = null,
    val statusLogs: List<OrderItemStatusLog> = emptyList(),
    val staffNames: Map<Long, String> = emptyMap(), // staffId -> staffName
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WorkOrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    // ... 其他 Repositories ...
    private val staffRepository: StaffRepository, // <-- 注入 StaffRepository
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderItemId: Long = savedStateHandle.get<Long>("orderItemId") ?: -1L

    private val _uiState = MutableStateFlow(WorkOrderDetailUiState())
    val uiState: StateFlow<WorkOrderDetailUiState> = _uiState.asStateFlow()

    init {
        if (orderItemId != -1L) {
            viewModelScope.launch {
                val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId ?: return@launch

                // 使用 combine 来合并多个 Flow
                // Flow for WorkOrderItem details
                val workOrderItemFlow = flow { emit(loadWorkOrderItemDetails(orderItemId, storeId)) }

                // Flow for status logs
                val logsFlow = orderRepository.getLogsForOrderItemFlow(orderItemId)

                workOrderItemFlow.combine(logsFlow) { workOrderItemDetails, logs ->
                    val staffIds = logs.map { it.staffId }.distinct()
                    val staffNames = staffRepository.getStaffByIds(staffIds).associateBy({ it.id }, { it.name })

                    _uiState.update {
                        it.copy(
                            workOrderItem = workOrderItemDetails,
                            statusLogs = logs,
                            staffNames = staffNames,
                            isLoading = false
                        )
                    }
                }.collect()
            }
        }
    }

    private suspend fun loadWorkOrderItemDetails(itemId: Long, storeId: Long): WorkOrderItem? {
        val orderItem = orderRepository.getOrderItemById(itemId) ?: return null
        val order = orderRepository.getOrderByIdAndStoreId(orderItem.orderId, storeId) ?: return null
        val customerName = order.customerId?.let { customerRepository.getCustomerByIdAndStoreId(it, storeId)?.name }
        return WorkOrderItem(orderItem, order.orderNumber, customerName, storeId)
    }

    fun updateStatus(newStatus: OrderItemStatus) {
        viewModelScope.launch {
            val (storeId, staffId) = sessionManager.userSessionFlow.firstOrNull()?.let { it.storeId to it.staffId }
            if (storeId == null || staffId == null) {
                _uiState.update { it.copy(errorMessage = "无法获取用户信息，操作失败") }
                return@launch
            }
            orderRepository.updateOrderItemStatus(orderItemId, newStatus, staffId, storeId)
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = "状态更新失败: ${e.localizedMessage}") }
                }
            // 成功时，因为我们观察的是 Flow，UI 会自动刷新
        }
    }
    // ...
}