package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) // 基于 (storeId, name, model) 唯一
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product): Int

    @Delete
    suspend fun deleteProduct(product: Product): Int

    @Query("SELECT * FROM products WHERE supplier_id = :supplierId AND is_active = 1 ORDER BY name ASC")
    fun getAllActiveProductsBySupplierIdFlow(supplierId: Long): Flow<List<Product>>

    // **新增：根据店铺 ID 获取该店铺所有供应商的所有激活产品**
    @Query("""
        SELECT p.* FROM products AS p 
        INNER JOIN suppliers AS s ON p.supplier_id = s.id 
        WHERE s.store_id = :storeId AND p.is_active = 1
        ORDER BY s.name ASC, p.name ASC
    """)
    fun getAllActiveProductsByStoreIdFlow(storeId: Long): Flow<List<Product>>

    // **新增：根据产品ID获取产品，不再需要 storeId**
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): Product?

    // --- **新增：按店铺和关键词搜索产品** ---
    @Query("""
        SELECT p.* FROM products AS p 
        INNER JOIN suppliers AS s ON p.supplier_id = s.id 
        WHERE s.store_id = :storeId AND p.is_active = 1 
        AND (p.name LIKE '%' || :query || '%' OR p.category LIKE '%' || :query || '%' OR p.model LIKE '%' || :query || '%')
        ORDER BY p.name ASC
    """)
    fun searchActiveProductsByStoreIdFlow(query: String, storeId: Long): Flow<List<Product>>

}