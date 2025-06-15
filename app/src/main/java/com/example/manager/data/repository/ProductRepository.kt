package com.example.manager.data.repository

import com.example.manager.data.model.entity.Product

interface ProductRepository {
    // Product 对象应已包含正确的 storeId
    suspend fun insertProduct(product: Product): Result<Long>
    suspend fun updateProduct(product: Product): Result<Int>
    suspend fun deleteProduct(product: Product): Int // 或 deleteProduct(productId: Long, storeId: Long)
    suspend fun getProductByIdAndStoreId(id: Long, storeId: Long): Product?
    suspend fun getAllActiveProductsByStoreId(storeId: Long): List<Product>
    suspend fun searchActiveProductsByStoreId(query: String, storeId: Long): List<Product>
    suspend fun getAllCategoriesByStoreId(storeId: Long): List<String>
    suspend fun getProductByNameAndModel(storeId: Long, name: String, model: String?): Product?
}