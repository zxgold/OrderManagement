package com.example.manager.data.repository

import com.example.manager.data.model.entity.Payment
import com.example.manager.data.model.uimodel.PaymentWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * 回款/支付数据的 Repository 接口
 */
interface PaymentRepository {

    /**
     * 按店铺和日期范围获取回款流水（包含客户和订单信息）。
     */
    fun getPaymentsByDateRangeFlow(storeId: Long, startDate: Long, endDate: Long): Flow<List<PaymentWithDetails>>

    /**
     * 获取特定订单的所有回款记录。
     */
    suspend fun getPaymentsByOrderId(orderId: Long): List<Payment>

    /**
     * 插入一笔新的回款记录。
     */
    suspend fun insertPayment(payment: Payment): Result<Long>
}