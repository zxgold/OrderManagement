package com.example.manager.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.manager.data.model.entity.Store


@Dao
interface StoreDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) // 如果店铺名唯一，则用 ABORT 防止重复
    suspend fun insertStore(store: Store): Long

    @Query("SELECT * FROM stores WHERE id = :id")
    suspend fun getStoreById(id: Long): Store?

    @Query("SELECT * FROM stores WHERE store_name = :name") // 用于检查店铺名是否已存在
    suspend fun getStoreByName(name: String): Store?

    @Update // <-- 添加 Update 方法，用于回填 ownerStaffId
    suspend fun updateStore(store: Store): Int
}