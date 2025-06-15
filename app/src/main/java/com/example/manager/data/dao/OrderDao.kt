package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Order
import com.example.manager.data.model.enums.OrderStatus

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order): Int

    // 删除时，如果要在 DAO 层面也确保是本店订单，可以加 storeId
    // 但通常 Order ID 是全局唯一的，ViewModel/Repository 层会做校验
    @Delete
    suspend fun deleteOrder(order: Order): Int

    @Query("SELECT * FROM orders WHERE id = :orderId AND store_id = :storeId") // 添加 store_id 条件
    suspend fun getOrderByIdAndStoreId(orderId: Long, storeId: Long): Order?

    @Query("SELECT * FROM orders WHERE order_number = :orderNumber AND store_id = :storeId") // 添加 store_id
    suspend fun getOrderByOrderNumberAndStoreId(orderNumber: String, storeId: Long): Order?

    // 获取特定店铺的所有订单
    @Query("SELECT * FROM orders WHERE store_id = :storeId ORDER BY order_date DESC")
    suspend fun getAllOrdersByStoreId(storeId: Long): List<Order>

    // 获取特定店铺、特定客户的订单
    @Query("SELECT * FROM orders WHERE customer_id = :customerId AND store_id = :storeId ORDER BY order_date DESC")
    suspend fun getOrdersByCustomerIdAndStoreId(customerId: Long, storeId: Long): List<Order>

    // 获取特定店铺、特定状态的订单
    @Query("SELECT * FROM orders WHERE status = :status AND store_id = :storeId ORDER BY order_date DESC")
    suspend fun getOrdersByStatusAndStoreId(status: OrderStatus, storeId: Long): List<Order>

    @Query("SELECT * FROM orders WHERE status = :statusCompleted AND store_id = :storeId ORDER BY completion_date DESC")
    suspend fun getCompletedOrdersByStoreId(storeId: Long, statusCompleted: OrderStatus = OrderStatus.COMPLETED): List<Order>

    // 更新订单状态 (通常在 Repository/ViewModel 层面会先校验订单是否属于当前店铺)
    @Query("UPDATE orders SET status = :newStatus, updated_at = :updateTime WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus, updateTime: Long = System.currentTimeMillis()): Int

    @Query("UPDATE orders SET status = :status, completion_date = :completionDate, updated_at = :updateTime WHERE id = :orderId")
    suspend fun updateOrderCompletion(orderId: Long, status: OrderStatus = OrderStatus.COMPLETED, completionDate: Long, updateTime: Long = System.currentTimeMillis()): Int
}