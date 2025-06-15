package com.example.manager.data.repository
import com.example.manager.data.dao.SupplierDao
import com.example.manager.data.model.entity.Supplier
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierRepositoryImpl @Inject constructor(
    private val supplierDao: SupplierDao
) : SupplierRepository {
    override suspend fun insertSupplier(supplier: Supplier): Result<Long> = try {
        Result.success(supplierDao.insertSupplier(supplier))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateSupplier(supplier: Supplier): Result<Int> = try {
        Result.success(supplierDao.updateSupplier(supplier))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteSupplier(supplier: Supplier): Result<Int> = try {
        Result.success(supplierDao.deleteSupplier(supplier))
    } catch (e: Exception) { Result.failure(e) }

    override fun getAllSuppliersByStoreIdFlow(storeId: Long): Flow<List<Supplier>> {
        return supplierDao.getAllSuppliersByStoreIdFlow(storeId)
    }
}