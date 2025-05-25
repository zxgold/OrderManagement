package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Staff

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

    @Query("SELECT * FROM staff WHERE is_active = 1 ORDER BY name ASC")
    suspend fun getAllActiveStaff(): List<Staff>

    @Query("SELECT * FROM staff ORDER BY name ASC")
    suspend fun getAllStaff(): List<Staff>

    @Query("SELECT COUNT(*) FROM staff")
    suspend fun countStaff(): Int // 返回员工总数
}