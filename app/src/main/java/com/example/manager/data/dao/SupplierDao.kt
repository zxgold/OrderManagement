package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSupplier(supplier: Supplier): Long
    @Update
    suspend fun updateSupplier(supplier: Supplier): Int
    @Delete
    suspend fun deleteSupplier(supplier: Supplier): Int
    @Query("SELECT * FROM suppliers WHERE store_id = :storeId ORDER BY name ASC")
    fun getAllSuppliersByStoreIdFlow(storeId: Long): Flow<List<Supplier>>
}
