package com.example.manager.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.manager.data.dao.InventoryItemDao
import com.example.manager.data.dao.OrderDao
import com.example.manager.data.dao.OrderItemDao
import com.example.manager.data.dao.OrderItemStatusLogDao
import com.example.manager.data.db.AppDatabase
import com.example.manager.data.model.entity.InventoryItem
import com.example.manager.data.model.entity.Order
import com.example.manager.data.model.entity.OrderItem
import com.example.manager.data.model.entity.OrderItemStatusLog
import com.example.manager.data.model.enums.OrderItemStatus
import com.example.manager.data.model.enums.OrderStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import com.example.manager.data.repository.InventoryRepository



@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val appDatabase: AppDatabase,
    private val orderItemStatusLogDao: OrderItemStatusLogDao,
    private val inventoryRepository: InventoryRepository
) : OrderRepository {

    override suspend fun insertOrderWithItems(order: Order, items: List<OrderItem>): Result<Long> {
        return try {
            val orderId = appDatabase.withTransaction {
                val insertedOrderId = orderDao.insertOrder(order)
                if (insertedOrderId <= 0) throw Exception("插入订单失败")

                val itemsWithOrderId = items.map { it.copy(orderId = insertedOrderId) }
                orderItemDao.insertOrUpdateOrderItems(itemsWithOrderId)
                insertedOrderId
            }
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderByIdAndStoreId(orderId: Long, storeId: Long): Order? {
        return orderDao.getOrderByIdAndStoreId(orderId, storeId)
    }

    override suspend fun getOrderWithItemsByIdAndStoreId(orderId: Long, storeId: Long): Pair<Order, List<OrderItem>>? {
        val order = orderDao.getOrderByIdAndStoreId(orderId, storeId)
        return if (order != null) {
            val items = orderItemDao.getOrderItemsByOrderId(orderId) // OrderId 是全局唯一的
            Pair(order, items)
        } else {
            null
        }
    }

    override suspend fun getAllOrdersByStoreId(storeId: Long): List<Order> {
        return orderDao.getAllOrdersByStoreId(storeId)
    }

    override suspend fun getOrdersByCustomerIdAndStoreId(customerId: Long, storeId: Long): List<Order> {
        return orderDao.getOrdersByCustomerIdAndStoreId(customerId, storeId)
    }

    override suspend fun getOrdersByStatusAndStoreId(status: OrderStatus, storeId: Long): List<Order> {
        return orderDao.getOrdersByStatusAndStoreId(status, storeId)
    }

    override suspend fun updateOrder(order: Order): Result<Int> {
        return try {
            // 假设 order 对象中的 storeId 是正确的，并且与当前操作员的 storeId 匹配
            // 这个校验最好在 ViewModel 中进行
            Result.success(orderDao.updateOrder(order))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteOrder(orderId: Long, storeId: Long): Result<Int> {
        return try {
            // 在这里可以先获取订单，校验 storeId 是否匹配，然后再删除
            // 或者依赖 DAO 的外键约束（如果 Order 表有 onDelete=RESTRICT on Store）
            // 或者，如果 OrderDao.deleteOrder 本身只按ID删除，那么这里需要先查询再删除
            // 假设 OrderDao.deleteOrder(Order) 会被级联，且 Order ID 全局唯一
            // 更安全的做法是 OrderDao 有 deleteOrderByIdAndStoreId
            val orderToDelete = orderDao.getOrderByIdAndStoreId(orderId, storeId)
            if (orderToDelete != null) {
                Result.success(orderDao.deleteOrder(orderToDelete)) // DAO 的 @Delete(entity) 会根据主键删除
            } else {
                Result.failure(NoSuchElementException("订单 (ID: $orderId) 不存在于店铺 (ID: $storeId) 中"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderItemsByOrderId(orderId: Long): List<OrderItem> {
        return orderItemDao.getOrderItemsByOrderId(orderId)
    }

    override suspend fun getOrderItemById(orderItemId: Long): OrderItem? {
        return orderItemDao.getOrderItemById(orderItemId)
    }

    override suspend fun updateOrderItem(orderItem: OrderItem): Result<Int> {
        return try {
            Result.success(orderItemDao.updateOrderItem(orderItem))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderItemStatus(
        orderItemId: Long,
        newStatus: OrderItemStatus,
        staffId: Long,
        storeId: Long
    ): Result<Boolean> {
        return try {
            appDatabase.withTransaction {
                val orderItem = orderItemDao.getOrderItemById(orderItemId)
                    ?: throw NoSuchElementException("OrderItem not found with ID: $orderItemId")

                val oldStatus = orderItem.status

                // 只在状态实际改变时才执行操作
                if (newStatus == oldStatus) {
                    Log.d("OrderRepoImpl", "Status is already $newStatus. No update performed.")
                    return@withTransaction // 提前退出事务
                }

                // 更新订单项状态
                // 这里应该更新 orderItem 表，而不是 order 表
                orderItemDao.updateOrderItem(orderItem.copy(
                    status = newStatus,
                    statusLastUpdateAt = System.currentTimeMillis(),
                    statusLastUpdateStaffId = staffId
                ))

                // 插入状态变更日志
                orderItemStatusLogDao.insertLog(
                    OrderItemStatusLog(
                        orderItemId = orderItemId,
                        status = newStatus,
                        staffId = staffId
                    )
                )

                // --- 库存联动逻辑 ---
                if (orderItem.productId != null) {
                    // 1. 入库：从“物流中”或更早的状态变为“已到库”
                    // 确保不会重复入库
                    if (newStatus == OrderItemStatus.IN_STOCK && oldStatus < OrderItemStatus.IN_STOCK) {
                        Log.d("OrderRepoImpl", "Increasing stock for product ${orderItem.productId} by ${orderItem.quantity}")
                        // **调用 inventoryRepository 的方法**
                        inventoryRepository.increaseStock(storeId, orderItem.productId, orderItem.quantity)
                    }
                    // 2. 出库：从“已到库”变为“已安装”
                    else if (newStatus == OrderItemStatus.INSTALLED && oldStatus == OrderItemStatus.IN_STOCK) {
                        Log.d("OrderRepoImpl", "Decreasing stock for product ${orderItem.productId} by ${orderItem.quantity}")
                        // **调用 inventoryRepository 的方法**
                        val decreaseResult = inventoryRepository.decreaseStock(storeId, orderItem.productId, orderItem.quantity)
                        if (decreaseResult.isFailure) {
                            // 如果减库存失败（库存不足），抛出异常以回滚整个事务
                            throw IllegalStateException("库存不足，无法将产品(ID: ${orderItem.productId})状态更新为已安装。")
                        }
                    }
                }

                // TODO: 检查并更新订单主状态的逻辑
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.e("OrderRepoImpl", "Failed to update order item status.", e)
            Result.failure(e)
        }
    }

    override fun getLogsForOrderItemFlow(orderItemId: Long): Flow<List<OrderItemStatusLog>> {
        return orderItemStatusLogDao.getLogsForOrderItemFlow(orderItemId)
    }


}
