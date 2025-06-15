package com.example.manager.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.manager.data.dao.ProductDao
import com.example.manager.data.model.entity.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override suspend fun insertProduct(product: Product): Result<Long> {
        return try {
            val newId = productDao.insertProduct(product)
            if (newId > 0) Result.success(newId)
            else Result.failure(Exception("插入产品失败，DAO返回ID无效"))
        } catch (e: SQLiteConstraintException) {
            Result.failure(e) // 将唯一性约束冲突的异常传递出去
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Int> {
        return try {
            Result.success(productDao.updateProduct(product))
        } catch (e: SQLiteConstraintException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(product: Product): Int {
        return productDao.deleteProduct(product)
    }

    override fun getAllActiveProductsBySupplierIdFlow(supplierId: Long): Flow<List<Product>> {
        return productDao.getAllActiveProductsBySupplierIdFlow(supplierId)
    }



}