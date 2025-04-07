package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Product

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product): Int

    @Delete
    suspend fun deleteProduct(product: Product): Int // 关联的 OrderItem.productId 会变 NULL

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY category ASC, name ASC")
    suspend fun getAllActiveProducts(): List<Product>

    @Query("SELECT * FROM products ORDER BY category ASC, name ASC")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM products WHERE is_active = 1 AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR model LIKE '%' || :query || '%') ORDER BY category ASC, name ASC")
    suspend fun searchActiveProducts(query: String): List<Product>

    @Query("SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != '' ORDER BY category ASC")
    suspend fun getAllCategories(): List<String> // 获取所有产品种类用于筛选
}