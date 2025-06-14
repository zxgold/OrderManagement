package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stores",
    indices = [Index(value = ["store_name"], unique = true)] // 店铺名称通常应该是唯一的
)
data class Store(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "store_name")
    val storeName: String,

    @ColumnInfo(name = "address") // 店铺地址 (可选)
    val address: String? = null,

    @ColumnInfo(name = "phone") // 店铺联系电话 (可选)
    val phone: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "owner_staff_id") // 记录创建这个店铺的初始老板ID (可选，但有用)
    val ownerStaffId: Long? = null // 在老板创建后回填此ID
)
