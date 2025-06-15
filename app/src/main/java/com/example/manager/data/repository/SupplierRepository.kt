package com.example.manager.data.repository
import com.example.manager.data.model.entity.Supplier
import kotlinx.coroutines.flow.Flow
interface SupplierRepository {
    suspend fun insertSupplier(supplier: Supplier): Result<Long>
    suspend fun updateSupplier(supplier: Supplier): Result<Int>
    suspend fun deleteSupplier(supplier: Supplier): Result<Int>
    fun getAllSuppliersByStoreIdFlow(storeId: Long): Flow<List<Supplier>>
}