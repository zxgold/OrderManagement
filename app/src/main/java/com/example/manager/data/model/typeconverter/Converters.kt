package com.example.manager.data.model.typeconverter

import androidx.room.TypeConverter
import com.example.manager.data.model.enums.ActionLogType
import com.example.manager.data.model.enums.FollowUpStatus
import com.example.manager.data.model.enums.LedgerEntryType
import com.example.manager.data.model.enums.OrderItemStatus
import com.example.manager.data.model.enums.OrderStatus
import com.example.manager.data.model.enums.StaffRole

object Converters { // 使用 object 使方法静态可用

    // --- Enum Converters ---

    @TypeConverter
    fun fromStaffRole(value: StaffRole?): String? {
        return value?.name // 存储枚举的名称，如果为 null 则存 null
    }

    @TypeConverter
    fun toStaffRole(value: String?): StaffRole? {
        return value?.let { enumValueOf<StaffRole>(it) } // 从名称转换回枚举，如果为 null 则返回 null
    }

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toOrderStatus(value: String?): OrderStatus? {
        // 提供默认值或处理无效值，如果数据库中存了意外的字符串
        return try {
            value?.let { enumValueOf<OrderStatus>(it) }
        } catch (e: IllegalArgumentException) {
            null // 或者返回一个默认状态如 OrderStatus.PENDING
        }
    }

    @TypeConverter
    fun fromOrderItemStatus(value: OrderItemStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toOrderItemStatus(value: String?): OrderItemStatus? {
        return try {
            value?.let { enumValueOf<OrderItemStatus>(it) }
        } catch (e: IllegalArgumentException) {
            null // 或者返回一个默认状态如 OrderItemStatus.NOT_ORDERED
        }
    }

    @TypeConverter
    fun fromFollowUpStatus(value: FollowUpStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toFollowUpStatus(value: String?): FollowUpStatus? {
        return try {
            value?.let { enumValueOf<FollowUpStatus>(it) }
        } catch (e: IllegalArgumentException) {
            null // 或者返回一个默认状态如 FollowUpStatus.PENDING
        }
    }

    @TypeConverter
    fun fromLedgerEntryType(value: LedgerEntryType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toLedgerEntryType(value: String?): LedgerEntryType? {
        return try {
            value?.let { enumValueOf<LedgerEntryType>(it) }
        } catch (e: IllegalArgumentException) {
            null // 这种情况比较少见，但可以返回 null 或抛出异常
        }
    }

    @TypeConverter
    fun fromActionLogType(value: ActionLogType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toActionLogType(value: String?): ActionLogType? {
        return try {
            value?.let { enumValueOf<ActionLogType>(it) }
        } catch (e: IllegalArgumentException) {
            null // 日志类型不匹配，返回 null
        }
    }


    // --- List<Long> Converter ---
    // 用于 Order.responsibleStaffIds

    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        // 将 List<Long> 转换为逗号分隔的字符串，例如 "[1,5,10]" -> "1,5,10"
        // 处理 null 和空列表的情况
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        // 将逗号分隔的字符串转换回 List<Long>，例如 "1,5,10" -> [1, 5, 10]
        // 处理 null、空字符串和无效数字的情况
        if (value.isNullOrBlank()) {
            return null // 或者返回 emptyList()，取决于业务逻辑
        }
        return try {
            value.split(',').mapNotNull {
                // 尝试将每个部分转换为 Long，忽略无法转换的部分
                it.trim().toLongOrNull()
            }
        } catch (e: Exception) {
            // 如果发生任何预料之外的错误（虽然 toLongOrNull 应该处理了数字格式问题）
            null // 或者返回 emptyList()
        }
    }

// 注意：如果你需要存储其他 List 类型（如 List<String>），需要创建相应的转换器
}