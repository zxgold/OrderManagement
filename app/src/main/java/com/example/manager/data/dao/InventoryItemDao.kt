package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.InventoryItem
import com.example.manager.data.model.uimodel.InventoryItemWithProductInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {

    @Query("""
        SELECT ii.*, p.name as productName, p.model as productModel
        FROM inventory_items AS ii
        INNER JOIN products AS p ON ii.product_id = p.id
        WHERE ii.store_id = :storeId
    """)
    fun getInventoryItemsWithProductInfoFlow(storeId: Long): Flow<List<InventoryItemWithProductInfo>>

    @Query("SELECT * FROM inventory_items WHERE store_id = :storeId AND product_id = :productId")
    suspend fun findByStoreAndProduct(storeId: Long, productId: Long): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE) // 使用 REPLACE 可以简化 upsert
    suspend fun upsert(inventoryItem: InventoryItem): Long // 改为返回 Long

    @Insert
    suspend fun insert(inventoryItem: InventoryItem): Long

    @Update
    suspend fun update(inventoryItem: InventoryItem): Int


}