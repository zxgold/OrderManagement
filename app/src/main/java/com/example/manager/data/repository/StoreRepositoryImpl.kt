package com.example.manager.data.repository

import android.util.Log
import com.example.manager.data.dao.StoreDao
import com.example.manager.data.model.entity.Store
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val storeDao: StoreDao
) : StoreRepository {
    override suspend fun insertStore(store: Store): Long {
        return storeDao.insertStore(store)
    }

    override suspend fun getStoreById(id: Long): Store? {
        return storeDao.getStoreById(id)
    }

    override suspend fun getStoreByName(name: String): Store? {
        return storeDao.getStoreByName(name)
    }

    override suspend fun updateStore(store: Store): Int {
        // Room 的 @Update 会根据主键更新，所以直接调用即可
        // 如果 StoreDao 没有 @Update 方法，你需要添加一个
        // 或者你也可以在这里写更复杂的更新逻辑，但通常 DAO 的 @Update 就够了
        // 假设 StoreDao 有 updateStore 方法:
        // return storeDao.updateStore(store)
        // 如果没有，可以考虑直接 insertOrUpdate，但需要 StoreDao 支持
        // 暂时我们可能不需要这个方法，先保留接口定义
        // 或者在 StoreDao 中添加 @Update suspend fun updateStore(store: Store): Int
        // 简单起见，如果StoreDao支持，可以复用 insert, Room的 OnConflictStrategy.REPLACE会处理更新
        // 但我们StoreDao用的是ABORT，所以需要显式Update或重新设计。
        // **为了简单，我们先假设StoreDao有updateStore，或者注册时先不回填ownerStaffId**
        // 或者更简单的是，如果 StoreDao 有 @Insert(onConflict = OnConflictStrategy.REPLACE)
        // 就可以用 storeDao.insertStore(store) 来更新
        // **目前，我们先不实现这个 updateStore 的具体逻辑，用到时再完善 StoreDao**
        Log.w("StoreRepositoryImpl", "updateStore not fully implemented yet if StoreDao lacks @Update")
        return 0 // 占位符
    }
}
