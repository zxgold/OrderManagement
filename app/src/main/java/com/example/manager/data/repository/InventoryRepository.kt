package com.example.manager.data.repository

import com.example.manager.data.model.entity.InventoryItem
import com.example.manager.data.model.uimodel.InventoryItemWithProductInfo
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getInventoryItemsWithProductInfoFlow(storeId: Long): Flow<List<InventoryItemWithProductInfo>>

    suspend fun increaseStock(
        storeId: Long,
        productId: Long,
        amount: Int,
        isStandard: Boolean,
        customization: String?,
        reservedForOrderItemId: Long?
    )

    suspend fun decreaseStandardStock(storeId: Long, productId: Long, amount: Int): Result<Unit>

    suspend fun decreaseCustomizedStockByOrderItemId(orderItemId: Long): Result<Unit>
}