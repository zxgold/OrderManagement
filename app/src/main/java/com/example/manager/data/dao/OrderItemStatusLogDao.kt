package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.OrderItemStatusLog
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemStatusLogDao {
    @Insert
    suspend fun insertLog(log: OrderItemStatusLog)

    @Query("SELECT * FROM order_item_status_logs WHERE order_item_id = :orderItemId ORDER BY timestamp ASC")
    fun getLogsForOrderItemFlow(orderItemId: Long): Flow<List<OrderItemStatusLog>>

}