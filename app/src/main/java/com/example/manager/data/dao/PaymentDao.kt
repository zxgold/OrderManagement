package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Payment

@Dao
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // 通常收款记录不应替换，用 ABORT？
    suspend fun insertPayment(payment: Payment): Long

    @Update // 是否允许更新？如果允许，需要严格控制
    suspend fun updatePayment(payment: Payment): Int

    @Delete // 是否允许删除？财务数据删除需谨慎
    suspend fun deletePayment(payment: Payment): Int

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): Payment?

    @Query("SELECT * FROM payments WHERE order_id = :orderId ORDER BY payment_date ASC")
    suspend fun getPaymentsByOrderId(orderId: Long): List<Payment>

    // 计算订单已付总额 (不含首付)
    @Query("SELECT SUM(amount) FROM payments WHERE order_id = :orderId")
    suspend fun getTotalPaymentsForOrder(orderId: Long): Double? // 返回 Double? 因为可能没有付款记录

    @Query("SELECT * FROM payments WHERE payment_date BETWEEN :startDate AND :endDate ORDER BY payment_date DESC")
    suspend fun getPaymentsByDateRange(startDate: Long, endDate: Long): List<Payment>
}