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
    val orderId: Long,

    @ColumnInfo(name = "sequence", defaultValue = "1")
    val sequence: Int = 1, // 第几次计划的回访

    @ColumnInfo(name = "status")
    val status: FollowUpStatus = FollowUpStatus.PENDING, // 默认待处理

    @ColumnInfo(name = "scheduled_date")
    val scheduledDate: Long, // 计划回访日期

    @ColumnInfo(name = "actual_follow_up_date")
    val actualFollowUpDate: Long? = null, // 实际完成日期

    @ColumnInfo(name = "staff_id")
    val staffId: Long? = null, // 执行人 ID

    @ColumnInfo(name = "notes")
    val notes: String? = null, // 回访记录内容

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)