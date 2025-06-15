package com.example.manager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.uimodel.WorkOrderItem
import com.example.manager.data.model.uimodel.WorkOrderListUiState
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.CustomerRepository
import com.example.manager.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkOrderListUiState())
    val uiState: StateFlow<WorkOrderListUiState> = _uiState.asStateFlow()

    init {
        loadAllWorkOrders()
    }

    fun loadAllWorkOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "无法获取店铺信息") }
                return@launch
            }

            try {
                val orders = orderRepository.getAllOrdersByStoreId(storeId)
                val allWorkOrders = mutableListOf<WorkOrderItem>()

                for (order in orders) {
                    val items = orderRepository.getOrderItemsByOrderId(order.id)
                    val customerName = order.customerId?.let { customerRepository.getCustomerByIdAndStoreId(it, storeId)?.name }
                    items.forEach { item ->
                        allWorkOrders.add(
                            WorkOrderItem(
                                orderItem = item,
                                orderNumber = order.orderNumber,
                                customerName = customerName,
                                storeId = storeId
                            )
                        )
                    }
                }
                // TODO: 按需排序，例如按订单日期倒序
                allWorkOrders.sortByDescending { it.orderItem.id } // 简单按ID倒序
                _uiState.update { it.copy(workOrders = allWorkOrders, isLoading = false) }
                Log.d("WorkOrderViewModel", "Loaded ${allWorkOrders.size} work orders.")

            } catch (e: Exception) {
                Log.e("WorkOrderViewModel", "Error loading work orders", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "加载工单列表失败: ${e.localizedMessage}") }
            }
        }
    }
}