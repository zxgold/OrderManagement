package com.example.manager.data.model.uimodel

data class WorkOrderListUiState(
    val workOrders: List<WorkOrderItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)