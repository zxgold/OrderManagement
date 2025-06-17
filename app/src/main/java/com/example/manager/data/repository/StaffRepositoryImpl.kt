package com.example.manager.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.manager.data.dao.StaffDao
import com.example.manager.data.model.entity.Staff
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepositoryImpl @Inject constructor(
    private val staffDao: StaffDao
) : StaffRepository {

    override suspend fun getStaffByUsername(username: String): Staff? {
        return staffDao.getStaffByUsername(username)
    }

    override suspend fun insertOrUpdateStaff(staff: Staff): Result<Long> {
        return try {
            // TODO: 在这里执行密码哈希
            Result.success(staffDao.insertOrUpdateStaff(staff))
        } catch (e: SQLiteConstraintException) {
            Result.failure(e) // 比如用户名唯一性冲突
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStaff(staff: Staff): Result<Int> {
        return try {
            // TODO: 如果允许在这里修改密码，需要先哈希
            Result.success(staffDao.updateStaff(staff))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStaffById(id: Long): Staff? {
        return staffDao.getStaffById(id)
    }

    override suspend fun getStaffByIds(ids: List<Long>): List<Staff> {
        return staffDao.getStaffByIds(ids)
    }

    override suspend fun isInitialSetupNeeded(): Boolean {
        return staffDao.countStaff() == 0
    }

    override fun getAllStaffsByStoreIdFlow(storeId: Long): Flow<List<Staff>> {
        return staffDao.getAllStaffsByStoreIdFlow(storeId)
    }
}