package com.example.manager.data.model.entity

import androidx.room.*
import com.example.manager.data.model.enums.OrderItemStatus


// 这个表用于记录每一次状态变更的历史。
@Entity(
    tableName = "order_item_status_logs",
    foreignKeys = [
        ForeignKey(
            entity = OrderItem::class,
            parentColumns = ["id"],
            childColumns = ["order_item_id"],
            onDelete = ForeignKey.CASCADE // 订单项删除，其日志也删除
        ),
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staff_id"],
            onDelete = ForeignKey.SET_NULL // 操作员工被删除，日志中的ID设为NULL
        )
    ],
    indices = [Index(value = ["order_item_id"])]
)
data class OrderItemStatusLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "order_item_id")
    val orderItemId: Long,

    @ColumnInfo(name = "status")
    val status: OrderItemStatus, // 变更到的状态

    @ColumnInfo(name = "staff_id")
    val staffId: Long, // 操作员工的ID

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis() // 操作时间
)