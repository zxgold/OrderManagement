package com.example.manager.data.repository

import com.example.manager.data.model.entity.Store

interface StoreRepository {
    suspend fun insertStore(store: Store): Long // 返回新店铺的 ID
    suspend fun getStoreById(id: Long): Store?
    suspend fun getStoreByName(name: String): Store? // 用于检查店铺名是否已存在 (可选)
    suspend fun updateStore(store: Store): Int // 用于回填 ownerStaffId
}
