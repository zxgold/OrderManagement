package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.manager.data.model.enums.OrderStatus
import com.example.manager.data.model.typeconverter.Converters // 下一步创建

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.RESTRICT // 不允许删除有关联订单的客户
        ),
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["creating_staff_id"],
            onDelete = ForeignKey.SET_NULL // 通常不允许删除创建了订单的员工，或设为 SET_NULL?
        ),
        ForeignKey( // <-- 新增外键
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.CASCADE // 如果店铺被删除，其下所有订单也级联删除 (这个策略要慎重!)
        )

    ],
    indices = [
        Index(value = ["order_number"], unique = true),
        Index(value = ["customer_id"]),
        Index(value = ["status"]),
        Index(value = ["creating_staff_id"]),
        Index(value = ["store_id"])
        // Index(value = ["completion_date"]) // 如果经常按完成日期查，可加
    ]
)
@TypeConverters(Converters::class, Converters::class) // 应用多个转换器
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "store_id")
    val storeId: Long, // 关联到 Store 表的 ID

    @ColumnInfo(name = "order_number")
    val orderNumber: String, // 需要业务逻辑生成唯一号

    @ColumnInfo(name = "customer_id")
    val customerId: Long?, // V3 设计为非空

    @ColumnInfo(name = "order_date")
    val orderDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "total_amount", defaultValue = "0.0")
    val totalAmount: Double = 0.0, // 初始为0，由 OrderItems 计算

    @ColumnInfo(name = "discount", defaultValue = "0.0")
    val discount: Double = 0.0,

    @ColumnInfo(name = "final_amount", defaultValue = "0.0")
    val finalAmount: Double = 0.0, // 初始为0，由 totalAmount - discount 计算

    @ColumnInfo(name = "down_payment", defaultValue = "0.0")
    val downPayment: Double = 0.0,

    @ColumnInfo(name = "status")
    val status: OrderStatus = OrderStatus.PENDING, // 默认状态 PENDING

    @ColumnInfo(name = "completion_date")
    val completionDate: Long? = null,

    @ColumnInfo(name = "responsible_staff_ids")
    val responsibleStaffIds: List<Long>? = null, // 使用 List<Long>，由 ListLongConverter 处理

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "creating_staff_id")
    val creatingStaffId: Long? // 记录订单创建者
)