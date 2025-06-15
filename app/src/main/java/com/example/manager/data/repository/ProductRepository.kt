package com.example.manager.data.repository

import com.example.manager.data.model.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun insertProduct(product: Product): Result<Long>
    suspend fun updateProduct(product: Product): Result<Int>
    suspend fun deleteProduct(product: Product): Int // 或 deleteProduct(productId: Long, storeId: Long)
    fun getAllActiveProductsBySupplierIdFlow(supplierId: Long): Flow<List<Product>>
}