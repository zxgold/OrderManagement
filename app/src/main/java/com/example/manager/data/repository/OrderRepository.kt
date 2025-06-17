package com.example.manager.data.repository

import com.example.manager.data.model.entity.Order
import com.example.manager.data.model.entity.OrderItem
import com.example.manager.data.model.entity.OrderItemStatusLog
import com.example.manager.data.model.enums.OrderItemStatus
import com.example.manager.data.model.enums.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    // 创建订单时，Order 对象应已包含正确的 storeId, creatingStaffId 等
    // items 列表中的 OrderItem 对象在传入时，orderId 可以是临时的或0，在实现中会被正确设置
    suspend fun insertOrderWithItems(order: Order, items: List<OrderItem>): Result<Long>

    suspend fun getOrderByIdAndStoreId(orderId: Long, storeId: Long): Order?
    suspend fun getOrderWithItemsByIdAndStoreId(orderId: Long, storeId: Long): Pair<Order, List<OrderItem>>?

    suspend fun getAllOrdersByStoreId(storeId: Long): List<Order>
    suspend fun getOrdersByCustomerIdAndStoreId(customerId: Long, storeId: Long): List<Order>
    suspend fun getOrdersByStatusAndStoreId(status: OrderStatus, storeId: Long): List<Order>

    suspend fun updateOrder(order: Order): Result<Int> // Order 对象应包含正确的 id 和 storeId
    suspend fun deleteOrder(orderId: Long, storeId: Long): Result<Int> // 按ID和店铺ID删除

    // OrderItem 相关，通常通过 OrderId 操作，间接实现店铺隔离
    suspend fun getOrderItemsByOrderId(orderId: Long): List<OrderItem>
    suspend fun updateOrderItem(orderItem: OrderItem): Result<Int> // orderItem.orderId -> Order.storeId

    suspend fun updateOrderItemStatus(
        orderItemId: Long,
        newStatus: OrderItemStatus,
        staffId: Long,
        storeId: Long // 用于库存操作
    ): Result<Boolean>

    fun getLogsForOrderItemFlow(orderItemId: Long): Flow<List<OrderItemStatusLog>> // <-- **添加此方法**

    suspend fun getOrderItemById(orderItemId: Long): OrderItem? // <-- 添加到 Repository

    suspend fun checkAndCompleteOrder(orderId: Long, storeId: Long): Boolean // 返回订单是否被完成
}