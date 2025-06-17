package com.example.manager.data.repository

import com.example.manager.data.model.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun insertProduct(product: Product): Result<Long>
    suspend fun updateProduct(product: Product): Result<Int>
    suspend fun deleteProduct(product: Product): Result<Int> // <-- **修改返回类型为 Result<Int>**
    fun getAllActiveProductsBySupplierIdFlow(supplierId: Long): Flow<List<Product>>
    fun getAllActiveProductsByStoreIdFlow(storeId: Long): Flow<List<Product>> // <-- 新增或修改为此
    suspend fun getProductById(productId: Long): Product? // <-- 新增
    fun searchActiveProductsByStoreIdFlow(query: String, storeId: Long): Flow<List<Product>>


}