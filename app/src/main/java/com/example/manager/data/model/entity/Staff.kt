package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.manager.data.model.enums.StaffRole
import com.example.manager.data.model.typeconverter.Converters // 下一步创建

@Entity(
    tableName = "staff",
    foreignKeys = [
        ForeignKey(
            entity = Store::class, // 关联到 Store 表
            parentColumns = ["id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.RESTRICT // 通常不允许删除还有员工的店铺，或根据业务定为 CASCADE 或 SET_NULL
        )
    ],
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["store_id"])
    ]

)
@TypeConverters(Converters::class) // 应用 StaffRole 的转换器
data class Staff(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "store_id")
    val storeId: Long, // 关联到 Store 表的 ID

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "role")
    val role: StaffRole, // 使用枚举

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String, // 存储哈希后的密码

    @ColumnInfo(name = "is_active", defaultValue = "1") // 默认激活 (1 for true)
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)