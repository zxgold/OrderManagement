package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.manager.data.model.enums.FollowUpStatus
import com.example.manager.data.model.typeconverter.Converters
// import com.example.manager.data.model.typeconverter.FollowUpStatusConverter // 下一步创建

@Entity(
    tableName = "follow_ups",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE // 删除客户则其回访记录/计划也删除
        ),
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE // 删除订单则其回访记录/计划也删除
        ),
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staff_id"],
            onDelete = ForeignKey.SET_NULL // 执行回访的员工删除，记录设为 NULL
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["order_id"]),
        Index(value = ["status"]),
        Index(value = ["scheduled_date"]),
        Index(value = ["staff_id"])
    ]
)
@TypeConverters(Converters::class)
data class FollowUp(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "customer_id")
    val customerId: Long,

    @ColumnInfo(name = "order_id")
    val orderId: Long?, // **改为可空，允许只跟进客户**

    // --- 新增/修改字段 ---
    @ColumnInfo(name = "follow_up_date") // **新增：本次跟进的日期**
    val followUpDate: Long,

    @ColumnInfo(name = "notes") // **将 notes 设为非空**
    val notes: String,

    @ColumnInfo(name = "staff_id")
    val staffId: Long, // **改为非空，操作人必须有**

    // --- 用于计划性回访的字段 (保持可空) ---
    @ColumnInfo(name = "is_planned") // **新增：标记是否为计划回访**
    val isPlanned: Boolean = false,
    @ColumnInfo(name = "status")
    val status: FollowUpStatus? = null,
    @ColumnInfo(name = "scheduled_date")
    val scheduledDate: Long? = null,

    // --- 用于记录下一次行动的字段 (保持可空) ---
    @ColumnInfo(name = "next_action_date")
    val nextActionDate: Long? = null,
    @ColumnInfo(name = "next_action_note")
    val nextActionNote: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)