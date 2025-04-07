package com.example.manager.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.manager.data.model.enums.ActionLogType
import com.example.manager.data.model.typeconverter.Converters // 下一步创建

@Entity(
    tableName = "action_logs",
    foreignKeys = [
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staff_id"],
            onDelete = ForeignKey.CASCADE // 如果员工被删除，他相关的日志也删除？或者 RESTRICT?
        )
    ],
    indices = [
        Index(value = ["action_time"]),
        Index(value = ["staff_id"]),
        Index(value = ["action_type"]),
        Index(value = ["target_entity_type", "target_entity_id"]) // 复合索引，方便查特定对象的日志
    ]
)
@TypeConverters(Converters::class)
data class ActionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "action_time", index = true)
    val actionTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "staff_id", index = true)
    val staffId: Long, // 操作人

    @ColumnInfo(name = "action_type", index = true)
    val actionType: ActionLogType, // 使用枚举

    @ColumnInfo(name = "target_entity_type")
    val targetEntityType: String? = null, // 如 "Order", "Customer"

    @ColumnInfo(name = "target_entity_id")
    val targetEntityId: Long? = null, // 对应实体的 ID

    @ColumnInfo(name = "details")
    val details: String? = null // JSON 或文本描述详情
)
