package com.example.manager.data.model.uimodel

import androidx.room.Embedded
import com.example.manager.data.model.entity.Payment

// 需要在 uimodel 包下创建这个数据类
data class PaymentWithDetails(
    @Embedded
    val payment: Payment,
    val customerName: String?,
    val orderNumber: String?
)