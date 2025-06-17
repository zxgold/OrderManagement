package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Payment
import com.example.manager.data.model.uimodel.PaymentWithDetails // 需要创建
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insertPayment(payment: Payment): Long

    @Update // 是否允许更新？如果允许，需要严格控制
    suspend fun updatePayment(payment: Payment): Int

    @Delete // 是否允许删除？财务数据删除需谨慎
    suspend fun deletePayment(payment: Payment): Int

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): Payment?

    // **核心查询：获取店铺在某日期范围内的所有回款，并关联客户和订单信息**
    @Query("""
        SELECT 
            p.*,
            c.name as customerName,
            o.order_number as orderNumber
        FROM payments AS p
        LEFT JOIN customers AS c ON p.customer_id = c.id
        LEFT JOIN orders AS o ON p.order_id = o.id
        WHERE p.store_id = :storeId AND p.payment_date BETWEEN :startDate AND :endDate
        ORDER BY p.payment_date DESC
    """)
    fun getPaymentsByDateRangeFlow(storeId: Long, startDate: Long, endDate: Long): Flow<List<PaymentWithDetails>>

    // 获取特定订单的所有回款
    @Query("SELECT * FROM payments WHERE order_id = :orderId ORDER BY payment_date ASC")
    suspend fun getPaymentsByOrderId(orderId: Long): List<Payment>

    // 计算订单已付总额 (不含首付)
    @Query("SELECT SUM(amount) FROM payments WHERE order_id = :orderId")
    suspend fun getTotalPaymentsForOrder(orderId: Long): Double? // 返回 Double? 因为可能没有付款记录
}