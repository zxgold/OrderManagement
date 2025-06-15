package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "suppliers",
    foreignKeys = [
        ForeignKey(
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["store_id", "name"], unique = true),
        Index(value = ["store_id"])
    ]
)
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "store_id")
    val storeId: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "contact_person")
    val contactPerson: String? = null,
    @ColumnInfo(name = "phone")
    val phone: String? = null,
    @ColumnInfo(name = "address")
    val address: String? = null,
    @ColumnInfo(name = "remark")
    val remark: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)