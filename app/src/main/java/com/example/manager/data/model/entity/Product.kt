package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["category"]),
        Index(value = ["name"]),
        Index(value = ["model"])
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "category")
    val category: String? = null,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "model")
    val model: String? = null,

    @ColumnInfo(name = "default_price", defaultValue = "0.0")
    val defaultPrice: Double = 0.0,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "specifications")
    val specifications: String? = null, // 可以考虑存储 JSON

    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
