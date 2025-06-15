package com.example.manager.data.repository

import android.util.Log
import com.example.manager.data.dao.InventoryItemDao
import com.example.manager.data.model.entity.InventoryItem
import com.example.manager.data.model.uimodel.InventoryItemWithProductInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val inventoryItemDao: InventoryItemDao
) : InventoryRepository {

    override fun getInventoryItemsWithProductInfoFlow(storeId: Long): Flow<List<InventoryItemWithProductInfo>> {
        return inventoryItemDao.getInventoryItemsWithProductInfoFlow(storeId)
    }

    override suspend fun increaseStock(storeId: Long, productId: Long, amount: Int) {
        try {
            val existing = inventoryItemDao.findByStoreAndProduct(storeId, productId)
            if (existing == null) {
                inventoryItemDao.insert(InventoryItem(storeId = storeId, productId = productId, quantity = amount, lastUpdatedAt = System.currentTimeMillis()))
                Log.d("InventoryRepo", "New inventory item created for product ID $productId with quantity $amount.")
            } else {
                val updatedItem = existing.copy(
                    quantity = existing.quantity + amount,
                    lastUpdatedAt = System.currentTimeMillis()
                )
                inventoryItemDao.update(updatedItem)
                Log.d("InventoryRepo", "Increased stock for product ID $productId. New quantity: ${updatedItem.quantity}")
            }
        } catch (e: Exception) {
            Log.e("InventoryRepo", "Error increasing stock for product ID $productId", e)
            // 在 Repository 中，我们可以只记录错误，或者向上抛出，或者返回 Result
            // 这里我们假设它是一个后台操作，记录错误即可
        }
    }

    override suspend fun decreaseStock(storeId: Long, productId: Long, amount: Int): Result<Unit> {
        return try {
            val existing = inventoryItemDao.findByStoreAndProduct(storeId, productId)
            if (existing != null && existing.quantity >= amount) {
                val updatedItem = existing.copy(
                    quantity = existing.quantity - amount,
                    lastUpdatedAt = System.currentTimeMillis()
                )
                inventoryItemDao.update(updatedItem)
                Log.d("InventoryRepo", "Decreased stock for product ID $productId. New quantity: ${updatedItem.quantity}")
                Result.success(Unit) // 操作成功
            } else {
                // 库存不足或库存项不存在
                val reason = if (existing == null) "库存项不存在" else "库存不足 (需要 $amount, 仅有 ${existing.quantity})"
                Log.w("InventoryRepo", "Failed to decrease stock for product ID $productId. Reason: $reason")
                Result.failure(IllegalStateException("库存不足或库存项不存在")) // 操作失败
            }
        } catch (e: Exception) {
            Log.e("InventoryRepo", "Error decreasing stock for product ID $productId", e)
            Result.failure(e)
        }
    }
}