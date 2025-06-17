package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Staff
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStaff(staff: Staff): Long

    @Update
    suspend fun updateStaff(staff: Staff): Int

    @Delete
    suspend fun deleteStaff(staff: Staff): Int

    @Query("SELECT * FROM staff WHERE id = :id")
    suspend fun getStaffById(id: Long): Staff?

    @Query("SELECT * FROM staff WHERE username = :username")
    suspend fun getStaffByUsername(username: String): Staff?

    @Query("SELECT COUNT(*) FROM staff")
    suspend fun countStaff(): Int // 返回员工总数

    // StaffDao.kt
    @Query("SELECT * FROM staff WHERE id IN (:ids)")
    suspend fun getStaffByIds(ids: List<Long>): List<Staff>

    // --- 新增：按店铺ID获取员工列表的 Flow** ---
    @Query("SELECT * FROM staff WHERE store_id = :storeId ORDER BY role ASC, name ASC")
    fun getAllStaffsByStoreIdFlow(storeId: Long): Flow<List<Staff>>
}