package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Product

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) // 基于 (storeId, name, model) 唯一
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product): Int

    @Delete
    suspend fun deleteProduct(product: Product): Int

    @Query("SELECT * FROM products WHERE id = :id AND store_id = :storeId")
    suspend fun getProductByIdAndStoreId(id: Long, storeId: Long): Product?

    // 根据店铺ID获取所有产品
    @Query("SELECT * FROM products WHERE store_id = :storeId AND is_active = 1 ORDER BY category ASC, name ASC")
    suspend fun getAllActiveProductsByStoreId(storeId: Long): List<Product>

    @Query("SELECT * FROM products WHERE store_id = :storeId ORDER BY category ASC, name ASC")
    suspend fun getAllProductsByStoreId(storeId: Long): List<Product>

    // 根据店铺ID和查询条件搜索产品
    @Query("SELECT * FROM products WHERE store_id = :storeId AND is_active = 1 AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR model LIKE '%' || :query || '%') ORDER BY category ASC, name ASC")
    suspend fun searchActiveProductsByStoreId(query: String, storeId: Long): List<Product>

    // 获取特定店铺的所有产品种类
    @Query("SELECT DISTINCT category FROM products WHERE store_id = :storeId AND category IS NOT NULL AND category != '' ORDER BY category ASC")
    suspend fun getAllCategoriesByStoreId(storeId: Long): List<String>
}