package com.example.manager.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.manager.data.model.entity.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {
    // 获取某个店铺的所有库存项（可以与产品信息连接查询）
    @Query("""
        SELECT ii.*, p.name as productName, p.model as productModel 
        FROM inventory_items AS ii
        INNER JOIN products AS p ON ii.product_id = p.id
        WHERE ii.store_id = :storeId
    """)
    fun getInventoryItemsWithProductInfoFlow(storeId: Long): Flow<List<InventoryItemWithProductInfo>> // 需要定义这个数据类

    // 查找特定产品的库存项
    @Query("SELECT * FROM inventory_items WHERE store_id = :storeId AND product_id = :productId")
    suspend fun findByStoreAndProduct(storeId: Long, productId: Long): InventoryItem?

    @Insert
    suspend fun insert(inventoryItem: InventoryItem)

    @Update
    suspend fun update(inventoryItem: InventoryItem)

    @Transaction
    suspend fun upsert(inventoryItem: InventoryItem) { // 插入或更新
        val existing = findByStoreAndProduct(inventoryItem.storeId, inventoryItem.productId)
        if (existing == null) {
            insert(inventoryItem)
        } else {
            update(inventoryItem.copy(id = existing.id))
        }
    }

    @Transaction
    suspend fun increaseStock(storeId: Long, productId: Long, amount: Int) { // 增加库存
        val existing = findByStoreAndProduct(storeId, productId)
        if (existing == null) {
            insert(InventoryItem(storeId = storeId, productId = productId, quantity = amount))
        } else {
            update(existing.copy(quantity = existing.quantity + amount))
        }
    }

    @Transaction
    suspend fun decreaseStock(storeId: Long, productId: Long, amount: Int): Boolean { // 减少库存
        val existing = findByStoreAndProduct(storeId, productId)
        if (existing != null && existing.quantity >= amount) {
            update(existing.copy(quantity = existing.quantity - amount))
            return true // 成功
        }
        return false // 失败（库存不足）
    }
}

// 用于连接查询的数据类
data class InventoryItemWithProductInfo(
    @Embedded
    val inventoryItem: InventoryItem,
    val productName: String,
    val productModel: String?
)