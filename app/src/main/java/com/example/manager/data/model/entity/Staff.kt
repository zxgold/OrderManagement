package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.manager.data.model.enums.StaffRole
import com.example.manager.data.model.typeconverter.Converters // 下一步创建

@Entity(
    tableName = "staff",
    indices = [Index(value = ["username"], unique = true)]
)
@TypeConverters(Converters::class) // 应用 StaffRole 的转换器
data class Staff(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

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