package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    foreignKeys = [
        ForeignKey(
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["store_id"],
            // 当店铺被删除时，如何处理其下的客户？
            // SET_NULL: 客户的 store_id 变为空，客户记录保留（可能变为“公共客户”或无主客户）
            // CASCADE: 客户记录也随店铺一起删除 (危险，除非业务明确需要)
            // RESTRICT: 如果店铺下有客户，不允许删除店铺
            onDelete = ForeignKey.SET_NULL // 或者 ForeignKey.RESTRICT，取决于业务逻辑
        )
    ],
    indices = [
        Index(value = ["name"]), // 仍然可以按姓名搜索
        // 电话号码在同一个店铺内应该是唯一的
        // 如果电话号码允许为空，则 NULL 值不参与唯一性约束
        Index(value = ["store_id", "phone"], unique = true), // <-- **关键：店铺ID和电话的组合唯一**
        Index(value = ["store_id"]) // <-- **为 store_id 单独创建一个索引，用于外键和查询**
    ]
)
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "store_id") // <-- **新增：关联到店铺的 ID**
    val storeId: Long,            // **客户必须属于一个店铺，设为非空**

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone")
    val phone: String?, // 电话号码允许为空吗？如果唯一性基于它，最好非空或有特殊处理

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "remark")
    val remark: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
