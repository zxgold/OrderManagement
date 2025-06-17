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
            onDelete = ForeignKey.SET_NULL // **重要：订单删除时，保留回款记录，只将order_id设为NULL**
        ),
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staff_id"],
            onDelete = ForeignKey.SET_NULL // 收款员工被删除，保留记录，将staff_id设为NULL
        ),
        ForeignKey(
            entity = Store::class, // **新增：关联到店铺**
            parentColumns = ["id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.RESTRICT // 店铺删除前必须处理完所有回款记录
        )
    ],
    indices = [
        Index(value = ["order_id"]),
        Index(value = ["staff_id"]),
        Index(value = ["store_id"]), // **新增：为 store_id 创建索引**
        Index(value = ["payment_date"])
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "store_id") // **新增：店铺ID**
    val storeId: Long,

    @ColumnInfo(name = "order_id")
    val orderId: Long?, // **改为可空，支持非订单关联的收款**

    @ColumnInfo(name = "customer_id") // **新增：客户ID，用于非订单收款或订单删除后追溯**
    val customerId: Long?,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "payment_date")
    val paymentDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String? = null,

    @ColumnInfo(name = "staff_id")
    val staffId: Long?, // **改为可空**

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)