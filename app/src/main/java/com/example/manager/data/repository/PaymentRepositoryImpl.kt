package com.example.manager.data.repository

import android.util.Log
import com.example.manager.data.dao.PaymentDao
import com.example.manager.data.model.entity.Payment
import com.example.manager.data.model.uimodel.PaymentWithDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao
) : PaymentRepository {

    override fun getPaymentsByDateRangeFlow(storeId: Long, startDate: Long, endDate: Long): Flow<List<PaymentWithDetails>> {
        return paymentDao.getPaymentsByDateRangeFlow(storeId, startDate, endDate)
    }

    override suspend fun getPaymentsByOrderId(orderId: Long): List<Payment> {
        return paymentDao.getPaymentsByOrderId(orderId)
    }

    override suspend fun insertPayment(payment: Payment): Result<Long> {
        return try {
            val newId = paymentDao.insertPayment(payment)
            if (newId > 0) {
                Result.success(newId)
            } else {
                Log.e("PaymentRepo", "Insert payment failed, DAO returned non-positive ID.")
                Result.failure(Exception("插入回款记录失败，DAO返回ID无效"))
            }
        } catch (e: Exception) {
            Log.e("PaymentRepo", "Error inserting payment", e)
            Result.failure(e)
        }
    }
}