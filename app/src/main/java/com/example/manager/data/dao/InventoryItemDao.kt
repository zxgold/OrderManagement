package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.InventoryItem
import com.example.manager.data.model.uimodel.InventoryItemWithProductInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {

    // --- 核心插入和更新 ---
    @Insert
    suspend fun insert(inventoryItem: InventoryItem): Long

    @Update
    suspend fun update(inventoryItem: InventoryItem): Int

    // --- 核心查询 ---

    /**
     * 根据店铺ID和产品ID查找所有相关的库存项。
     * 返回列表是因为定制品可能有多条记录。
     */
    @Query("SELECT * FROM inventory_items WHERE store_id = :storeId AND product_id = :productId")
    suspend fun findByStoreAndProduct(storeId: Long, productId: Long): List<InventoryItem>

    /**
     * 专门用于查找标准化库存（is_standard_stock = 1）。
     * 因为部分唯一索引的存在，这个查询最多只会返回一条记录。
     */
    @Query("SELECT * FROM inventory_items WHERE store_id = :storeId AND product_id = :productId AND is_standard_stock = 1 LIMIT 1")
    suspend fun findStandardStockByStoreAndProduct(storeId: Long, productId: Long): InventoryItem?

    /**
     * 根据预定的订单项ID查找库存项。
     * 因为 reserved_for_order_item_id 是唯一的，所以这个查询最多返回一条记录。
     */
    @Query("SELECT * FROM inventory_items WHERE reserved_for_order_item_id = :orderItemId LIMIT 1")
    suspend fun findByOrderItemId(orderItemId: Long): InventoryItem?

    /**
     * 根据库存项的主键ID获取它。
     */
    @Query("SELECT * FROM inventory_items WHERE id = :inventoryItemId")
    suspend fun getById(inventoryItemId: Long): InventoryItem?


    /**
     * 获取某个店铺的所有库存项，并连接产品信息，以Flow的形式返回。
     * 用于库存管理界面的列表展示。
     */
    @Query("""
        SELECT 
            ii.id, 
            ii.store_id, 
            ii.product_id, 
            ii.quantity, 
            ii.last_updated_at, 
            ii.is_standard_stock, 
            ii.customization_details, 
            ii.reserved_for_order_item_id, 
            ii.status, 
            ii.location_in_warehouse,
            p.name as productName, 
            p.model as productModel 
        FROM inventory_items AS ii
        INNER JOIN products AS p ON ii.product_id = p.id
        WHERE ii.store_id = :storeId
        ORDER BY p.name ASC
    """)
    fun getInventoryItemsWithProductInfoFlow(storeId: Long): Flow<List<InventoryItemWithProductInfo>>

    /**
     * **新增：根据供应商ID获取该供应商所有产品的库存项。**
     * 通过连接 products 表来实现筛选。
     */
    @Query("""
        SELECT 
            ii.id, 
            ii.store_id, 
            ii.product_id, 
            ii.quantity, 
            ii.last_updated_at, 
            ii.is_standard_stock, 
            ii.customization_details, 
            ii.reserved_for_order_item_id, 
            ii.status, 
            ii.location_in_warehouse,
            p.name as productName, 
            p.model as productModel 
        FROM inventory_items AS ii
        INNER JOIN products AS p ON ii.product_id = p.id
        WHERE p.supplier_id = :supplierId 
        ORDER BY p.name ASC
    """)
    fun getInventoryItemsBySupplierFlow(supplierId: Long): Flow<List<InventoryItemWithProductInfo>>
}