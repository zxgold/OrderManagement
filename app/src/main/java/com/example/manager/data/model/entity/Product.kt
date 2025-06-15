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
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplier_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["supplier_id", "name", "model"], unique = true),
        Index(value = ["supplier_id"])
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "supplier_id") // <-- **修改**
    val supplierId: Long,

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