package com.example.manager.data.model.uimodel

import androidx.room.Embedded
import com.example.manager.data.model.entity.InventoryItem

/**
 * 一个数据类，用于封装库存项及其关联的产品信息。
 * 主要用于 Room 的 JOIN 查询结果。
 */
data class InventoryItemWithProductInfo(
    // 使用 @Embedded 注解，Room 会将 inventory_items 表的所有列
    // 映射到这个 InventoryItem 对象中。
    @Embedded
    val inventoryItem: InventoryItem,

    // 这两个字段需要与 DAO 查询中的别名完全匹配。
    // SELECT p.name as productName ... -> val productName: String
    // SELECT p.model as productModel ... -> val productModel: String?
    val productName: String,
    val productModel: String?
)