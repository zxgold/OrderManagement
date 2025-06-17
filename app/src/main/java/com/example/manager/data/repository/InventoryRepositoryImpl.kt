package com.example.manager.data.repository

import android.util.Log
import com.example.manager.data.dao.InventoryItemDao
import com.example.manager.data.model.entity.InventoryItem
import com.example.manager.data.model.enums.InventoryItemStatus
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

    override suspend fun increaseStock(
        storeId: Long,
        productId: Long,
        amount: Int,
        isStandard: Boolean,
        customization: String?,
        reservedForOrderItemId: Long?
    ) {
        try {
            if (isStandard) {
                val existing = inventoryItemDao.findStandardStockByStoreAndProduct(storeId, productId)
                if (existing == null) {
                    inventoryItemDao.insert(InventoryItem(storeId = storeId, productId = productId, quantity = amount, isStandardStock = true, status = InventoryItemStatus.AVAILABLE))
                } else {
                    inventoryItemDao.update(existing.copy(quantity = existing.quantity + amount, lastUpdatedAt = System.currentTimeMillis()))
                }
            } else {
                inventoryItemDao.insert(InventoryItem(
                    storeId = storeId,
                    productId = productId,
                    quantity = 1,
                    isStandardStock = false,
                    customizationDetails = customization,
                    reservedForOrderItemId = reservedForOrderItemId,
                    status = if (reservedForOrderItemId != null) InventoryItemStatus.RESERVED else InventoryItemStatus.AVAILABLE,
                    lastUpdatedAt = System.currentTimeMillis()
                ))
            }
        } catch (e: Exception) {
            Log.e("InventoryRepo", "Error increasing stock", e)
        }
    }

    override suspend fun decreaseStandardStock(storeId: Long, productId: Long, amount: Int): Result<Unit> {
        return try {
            val existing = inventoryItemDao.findStandardStockByStoreAndProduct(storeId, productId)
            if (existing != null && existing.quantity >= amount) {
                inventoryItemDao.update(existing.copy(quantity = existing.quantity - amount, lastUpdatedAt = System.currentTimeMillis()))
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("标准化产品库存不足"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun decreaseCustomizedStockByOrderItemId(orderItemId: Long): Result<Unit> {
        return try {
            val existing = inventoryItemDao.findByOrderItemId(orderItemId)
            if (existing != null && !existing.isStandardStock && existing.status != InventoryItemStatus.SOLD) {
                inventoryItemDao.update(existing.copy(status = InventoryItemStatus.SOLD, lastUpdatedAt = System.currentTimeMillis()))
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("找不到为该订单项预定的库存，或该库存已售出"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun getInventoryItemsBySupplierFlow(supplierId: Long): Flow<List<InventoryItemWithProductInfo>> { // <-- **实现**
        return inventoryItemDao.getInventoryItemsBySupplierFlow(supplierId)
    }
}