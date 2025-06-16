package com.example.manager.data.model.entity

import androidx.room.*
import com.example.manager.data.model.enums.InventoryItemStatus
import com.example.manager.data.model.typeconverter.Converters

@Entity(
    tableName = "inventory_items",
    foreignKeys = [
        ForeignKey(
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE // 如果产品从目录中被删除，对应的库存项也应被删除
        ),
        ForeignKey(entity = OrderItem::class,
            parentColumns = ["id"],
            childColumns = ["reserved_for_order_item_id"],
            onDelete = ForeignKey.SET_NULL)// OrderItem的外键，并设为可空
    ],
    indices = [
        // 这个唯一索引对于区分标准化和定制化库存非常重要
        // 但如果标准化库存也想按批次入库（而不是简单增减数量），这个模型还需要再调整
        // 目前这个模型：每个店铺对同一种产品，只有一条“标准化库存”记录
        Index(value = ["is_standard_stock"]),
        Index(value = ["store_id"]),
        Index(value = ["product_id"]),
        Index(value = ["reserved_for_order_item_id"],  unique = true) // 确保一个订单项只预定一个库存项
    ]
)

data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "store_id")
    val storeId: Long,

    @ColumnInfo(name = "product_id")
    val productId: Long,

    @ColumnInfo(name = "is_standard_stock") // **新增：是否为标准化库存**
    val isStandardStock: Boolean, // true 表示是通用库存品，false 表示是为特定订单定制的

    @ColumnInfo(name = "quantity") // **对于 isStandardStock=true, 这个字段表示数量；对于 false, 它总是 1**
    val quantity: Int = 1,

    @ColumnInfo(name = "customization_details", defaultValue = "NULL") // **新增：定制化详情**
    val customizationDetails: String? = null, // 存储该单件的定制信息

    @ColumnInfo(name = "reserved_for_order_item_id") // **为外键添加索引注解**
    val reservedForOrderItemId: Long? = null,

    @ColumnInfo(name = "status") // **新增：库存项自身的状态**
    val status: InventoryItemStatus, // 例如：AVAILABLE, RESERVED, SOLD


    @ColumnInfo(name = "last_updated_at")
    var lastUpdatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "location_in_warehouse")
    val locationInWarehouse: String? = null, // 仓库位置
    // @ColumnInfo(name = "low_stock_threshold")
    // val lowStockThreshold: Int? = null // 低库存阈值
)

