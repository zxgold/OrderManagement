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

}