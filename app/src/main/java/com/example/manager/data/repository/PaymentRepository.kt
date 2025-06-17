package com.example.manager.data.repository

import com.example.manager.data.model.entity.Payment
import com.example.manager.data.model.uimodel.PaymentWithDetails
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getPaymentsByDateRangeFlow(storeId: Long, startDate: Long, endDate: Long): Flow<List<PaymentWithDetails>>
    suspend fun insertPayment(payment: Payment): Result<Long>
}