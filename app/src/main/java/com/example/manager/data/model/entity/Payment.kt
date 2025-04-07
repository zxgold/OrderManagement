package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE // 订单删除则收款记录也删除？或 RESTRICT? 看业务需求
        ),
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staff_id"],
            onDelete = ForeignKey.RESTRICT // 收款员工通常不能随意删除
        )
    ],
    indices = [
        Index(value = ["order_id"]),
        Index(value = ["staff_id"]),
        Index(value = ["payment_date"])
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "order_id", index = true)
    val orderId: Long,

    @ColumnInfo(name = "amount")
    val amount: Double, // 本次收款金额

    @ColumnInfo(name = "payment_date", index = true)
    val paymentDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String? = null, // 如 "现金", "微信", "银行转账"

    @ColumnInfo(name = "staff_id", index = true)
    val staffId: Long, // 收款操作人

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)