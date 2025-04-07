package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Order
import com.example.manager.data.model.enums.OrderStatus

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.ABORT) // 订单号通常唯一，不允许替换
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order): Int

    @Delete
    suspend fun deleteOrder(order: Order): Int // 会级联删除 OrderItems, Payments, FollowUps

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): Order?

    @Query("SELECT * FROM orders WHERE order_number = :orderNumber")
    suspend fun getOrderByOrderNumber(orderNumber: String): Order?

    @Query("SELECT * FROM orders WHERE customer_id = :customerId ORDER BY order_date DESC")
    suspend fun getOrdersByCustomerId(customerId: Long): List<Order>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY order_date DESC")
    suspend fun getOrdersByStatus(status: OrderStatus): List<Order>

    // 查询需要回访的订单 (状态为 COMPLETED 且还没有生成 PENDING 的回访记录？ - 逻辑较复杂，可能放 Repository)
    // 或者简单查询状态为 COMPLETED 的订单
    @Query("SELECT * FROM orders WHERE status = :statusCompleted ORDER BY completion_date DESC")
    suspend fun getCompletedOrders(statusCompleted: OrderStatus = OrderStatus.COMPLETED): List<Order>

    @Query("SELECT * FROM orders ORDER BY order_date DESC")
    suspend fun getAllOrders(): List<Order>

    // 查询特定客户、特定状态的订单
    @Query("SELECT * FROM orders WHERE customer_id = :customerId AND status = :status ORDER BY order_date DESC")
    suspend fun getOrdersByCustomerAndStatus(customerId: Long, status: OrderStatus): List<Order>

    // 更新订单状态
    @Query("UPDATE orders SET status = :newStatus, updated_at = :updateTime WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus, updateTime: Long = System.currentTimeMillis()): Int

    // 更新订单完成状态和时间
    @Query("UPDATE orders SET status = :status, completion_date = :completionDate, updated_at = :updateTime WHERE id = :orderId")
    suspend fun updateOrderCompletion(orderId: Long, status: OrderStatus = OrderStatus.COMPLETED, completionDate: Long, updateTime: Long = System.currentTimeMillis()): Int

    // 获取下一个订单号？ (这通常是业务逻辑，不在 DAO)

    // 注意：获取 Order 及其关联数据 (如 Customer, OrderItems) 通常有几种方式：
    // 1. 分别查询，在 Repository 组合。
    // 2. 使用 @Query + POJO (Plain Old Java Object) 返回组合结果。
    // 3. 使用 @Relation 注解（更推荐的方式，后续可在 Repository 中实现）。
}
