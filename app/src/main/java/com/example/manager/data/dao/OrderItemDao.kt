package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.OrderItem
import com.example.manager.data.model.enums.OrderItemStatus

@Dao
interface OrderItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOrderItem(orderItem: OrderItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOrderItems(orderItems: List<OrderItem>): List<Long> // 批量插入/更新

    @Update
    suspend fun updateOrderItem(orderItem: OrderItem): Int

    @Delete
    suspend fun deleteOrderItem(orderItem: OrderItem): Int

    @Query("DELETE FROM order_items WHERE order_id = :orderId")
    suspend fun deleteOrderItemsByOrderId(orderId: Long): Int // 删除某个订单下的所有项

    @Query("SELECT * FROM order_items WHERE id = :id")
    suspend fun getOrderItemById(id: Long): OrderItem?

    @Query("SELECT * FROM order_items WHERE order_id = :orderId ORDER BY id ASC") // 按加入顺序或产品名排序？
    suspend fun getOrderItemsByOrderId(orderId: Long): List<OrderItem>

    @Query("SELECT * FROM order_items WHERE order_id = :orderId AND status = :status ORDER BY id ASC")
    suspend fun getOrderItemsByOrderIdAndStatus(orderId: Long, status: OrderItemStatus): List<OrderItem>

    @Query("SELECT COUNT(*) FROM order_items WHERE order_id = :orderId")
    suspend fun countOrderItemsByOrderId(orderId: Long): Int

    // 检查订单下是否所有 Item 都已安装
    @Query("SELECT COUNT(*) FROM order_items WHERE order_id = :orderId AND status != :installedStatus")
    suspend fun countNonInstalledItemsByOrderId(orderId: Long, installedStatus: OrderItemStatus = OrderItemStatus.INSTALLED): Int

    // 更新订单项状态
    @Query("UPDATE order_items SET status = :newStatus, status_last_update_staff_id = :staffId, status_last_update_at = :updateTime WHERE id = :orderItemId")
    suspend fun updateOrderItemStatus(orderItemId: Long, newStatus: OrderItemStatus, staffId: Long, updateTime: Long = System.currentTimeMillis()): Int

    // 更新安装状态和时间
    @Query("UPDATE order_items SET status = :status, installed_at = :installedAt, status_last_update_staff_id = :staffId, status_last_update_at = :updateTime WHERE id = :orderItemId")
    suspend fun updateOrderItemInstallation(orderItemId: Long, status: OrderItemStatus = OrderItemStatus.INSTALLED, installedAt: Long, staffId: Long, updateTime: Long = System.currentTimeMillis()): Int

}
