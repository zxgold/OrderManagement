package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.manager.data.model.enums.OrderItemStatus
import com.example.manager.data.model.typeconverter.Converters // 下一步创建

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE // 删除订单时，级联删除其下的订单项
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.SET_NULL // 产品被删除时，订单项中的关联设为 NULL
        ),
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["status_last_update_staff_id"],
            onDelete = ForeignKey.SET_NULL // 更新状态的员工被删除时，设为 NULL
        )
    ],
    indices = [
        Index(value = ["order_id"]),
        Index(value = ["product_id"]),
        Index(value = ["status"]),
        Index(value = ["product_name"]), // 可能按产品名搜索订单项
        Index(value = ["status_last_update_staff_id"])
    ]
)
@TypeConverters(Converters::class)
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "order_id", index = true)
    val orderId: Long,

    @ColumnInfo(name = "product_id", index = true)
    val productId: Long? = null, // 允许非标品

    @ColumnInfo(name = "product_category")
    val productCategory: String? = null, // 快照

    @ColumnInfo(name = "product_name", index = true)
    val productName: String, // 快照，非空

    @ColumnInfo(name = "product_model")
    val productModel: String? = null, // 快照

    @ColumnInfo(name = "dimensions")
    val dimensions: String? = null,

    @ColumnInfo(name = "color")
    val color: String? = null,

    @ColumnInfo(name = "quantity")
    val quantity: Int = 1, // 默认数量为 1

    @ColumnInfo(name = "actual_unit_price")
    val actualUnitPrice: Double,

    @ColumnInfo(name = "item_total_amount")
    val itemTotalAmount: Double, // quantity * actualUnitPrice

    @ColumnInfo(name = "status")
    val status: OrderItemStatus = OrderItemStatus.NOT_ORDERED, // 默认未下单

    @ColumnInfo(name = "status_last_update_staff_id", index = true)
    val statusLastUpdateStaffId: Long? = null,

    @ColumnInfo(name = "status_last_update_at")
    val statusLastUpdateAt: Long? = null,

    @ColumnInfo(name = "ordered_vendor_at")
    val orderedFromVendorAt: Long? = null,

    @ColumnInfo(name = "arrived_stock_at")
    val arrivedAtStockAt: Long? = null,

    @ColumnInfo(name = "installed_at")
    val installedAt: Long? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
