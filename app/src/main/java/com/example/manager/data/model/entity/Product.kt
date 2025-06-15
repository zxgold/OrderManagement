package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["store_id"],
            // 当店铺被删除时，其下的产品如何处理？
            // CASCADE: 店铺删除，该店铺的所有产品也删除 (如果产品是严格店铺专属)
            // RESTRICT: 如果店铺下还有产品，不允许删除店铺
            onDelete = ForeignKey.CASCADE // 假设产品严格属于店铺，确认这是期望行为
        )
    ],
    indices = [
        // 店铺内的产品名称和型号组合通常应该是唯一的
        // 如果 name 或 model 可能为 null，你需要考虑 SQLite 对 NULL 在唯一索引中的处理
        // 通常，多行可以有相同的 NULL 值而不会违反唯一约束。
        // 如果 name 和 model 都是非空的，这个唯一索引会更有力。
        Index(value = ["store_id", "name", "model"], unique = true),
        Index(value = ["store_id", "category"]), // 按店铺和分类查询
        Index(value = ["store_id", "name"]),    // 按店铺和名称查询
        Index(value = ["store_id"])             // **为 store_id 外键创建索引**
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "store_id") // <-- **新增：关联到店铺的 ID**
    val storeId: Long,            // **产品必须属于一个店铺，设为非空**

    @ColumnInfo(name = "category")
    val category: String? = null, // 产品分类，例如 "沙发", "床垫"

    @ColumnInfo(name = "name")
    val name: String, // 产品名称，例如 "现代简约布艺沙发"

    @ColumnInfo(name = "model")
    val model: String? = null, // 产品型号，例如 "SF-001"

    @ColumnInfo(name = "default_price", defaultValue = "0.0")
    val defaultPrice: Double = 0.0, // 默认/参考售价

    @ColumnInfo(name = "description")
    val description: String? = null, // 产品详细描述

    @ColumnInfo(name = "specifications") // 例如颜色、尺寸、材质等，可以用JSON或特定格式文本
    val specifications: String? = null,

    @ColumnInfo(name = "is_active", defaultValue = "1") // 产品是否仍在销售或可用
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)