package com.example.manager.data.model.uimodel

import com.example.manager.data.model.entity.OrderItem

data class WorkOrderItem(
    val orderItem: OrderItem,
    val orderNumber: String,
    val customerName: String?,
    val storeId: Long
)