package com.example.manager.data.model.entity

import androidx.room.*

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
        )
    ],
    indices = [
        // 一个店铺中的同一种产品，只应该有一条库存记录
        Index(value = ["store_id", "product_id"], unique = true)
    ]
)
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "store_id")
    val storeId: Long,

    @ColumnInfo(name = "product_id")
    val productId: Long,

    @ColumnInfo(name = "quantity")
    var quantity: Int, // 当前库存数量

    @ColumnInfo(name = "last_updated_at")
    var lastUpdatedAt: Long = System.currentTimeMillis()

    // 还可以添加字段，如：
    // @ColumnInfo(name = "location_in_warehouse")
    // val locationInWarehouse: String? = null, // 仓库位置
    // @ColumnInfo(name = "low_stock_threshold")
    // val lowStockThreshold: Int? = null // 低库存阈值
)