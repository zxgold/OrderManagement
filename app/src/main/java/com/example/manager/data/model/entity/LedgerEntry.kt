package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.manager.data.model.enums.LedgerEntryType
import com.example.manager.data.model.typeconverter.Converters // 下一步创建

@Entity(
    tableName = "ledger_entries",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["related_order_id"],
            onDelete = ForeignKey.SET_NULL // 关联订单删除，关联关系设为 NULL
        ),
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["related_customer_id"],
            onDelete = ForeignKey.SET_NULL // 关联客户删除，关联关系设为 NULL
        ),
        ForeignKey(
            entity = Payment::class,
            parentColumns = ["id"],
            childColumns = ["payment_id"],
            onDelete = ForeignKey.SET_NULL // 关联付款记录删除，关联关系设为 NULL
        ),
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staff_id"],
            onDelete = ForeignKey.RESTRICT // 记录人不能随意删除
        ),
        ForeignKey(
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.CASCADE // 店铺删除，账目也删除 (慎重!) 或 RESTRICT
        )
    ],
    indices = [
        Index(value = ["entry_type"]),
        Index(value = ["entry_date"]),
        Index(value = ["related_order_id"]),
        Index(value = ["related_customer_id"]),
        Index(value = ["payment_id"]),
        Index(value = ["staff_id"]),
        Index(value = ["store_id"])
    ]
)
@TypeConverters(Converters::class)
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "store_id")
    val storeId: Long, // 关联到 Store 表的 ID

    @ColumnInfo(name = "entry_type")
    val entryType: LedgerEntryType, // INCOME or EXPENSE

    @ColumnInfo(name = "amount")
    val amount: Double, // 始终为正数

    @ColumnInfo(name = "entry_date")
    val entryDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "related_order_id")
    val relatedOrderId: Long? = null,

    @ColumnInfo(name = "related_customer_id")
    val relatedCustomerId: Long? = null,

    @ColumnInfo(name = "payment_id")
    val paymentId: Long? = null,

    @ColumnInfo(name = "staff_id")
    val staffId: Long, // 记录人

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)