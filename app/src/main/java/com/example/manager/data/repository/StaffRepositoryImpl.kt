package com.example.manager.data.repository

import com.example.manager.data.dao.StaffDao
import com.example.manager.data.model.entity.Staff
import javax.inject.Inject
/*
 * Inject是Java依赖注入规范中的注解，通常与Hilt或Dagger等依赖注入框架一起使用。
 *
 * Singleton同样是JSR-330中的注解，用于标记单例对象，知识依赖注入框架将这个类的实例创建为单例
 */
import javax.inject.Singleton

/*
 *
 * 员工数据的Repository实现
 * 这是一个用于数据访问的仓库层实现，实现了StaffRepository接口
 * 它展现了在现代Android应用中如何结合依赖注入、协程和仓库模式来构建数据层
 */

@Singleton // Repository的单例，这告诉依赖注入框架，在整个应用程序的声明周期中，StaffRepository的实例只应该存在一个
class StaffRepositoryImpl @Inject constructor(
    private val staffDao: StaffDao
): StaffRepository {
    override suspend fun getStaffByUsername(username: String): Staff? {
        return staffDao.getStaffByUsername(username)
    }
    override suspend fun insertOrUpdateStaff(staff: Staff): Long {
        // 为了演示，我们可能需要一个简单的哈希逻辑，或者先用明文密码
        // 真实项目中，密码应该在存入数据库前进行安全的哈希处理
        // val hashedPassword = bcryptHash(staff.passwordHash) // 假设有哈希函数
        // val staffToInsert = staff.copy(passwordHash = hashedPassword)
        // return staffDao.insertOrUpdateStaff(staffToInsert)

        // ---- 开发初期简化：直接存储 ----
        // 假设 staff.passwordHash 已经是我们期望存入的值（可能是明文或预哈希）
        return staffDao.insertOrUpdateStaff(staff)
    }
    override suspend fun getStaffById(id: Long): Staff? {
        return staffDao.getStaffById(id)
    }
    override suspend fun getAllStaffs(): List<Staff> {
        return staffDao.getAllStaff()
    }
    override suspend fun hasAnyStaff(): Boolean {
        return staffDao.countStaff() == 0 // 如果员工数为0，则表示没有任何员工
    }
    // 相比上面这种方法，下面这样更清晰：如果 isInitialSetupNeeded() 返回 true，我们就去老板注册界面。
    override suspend fun isInitialSetupNeeded(): Boolean {
        return staffDao.countStaff() == 0 // 如果员工数为0，则需要初始设置
    }

    override suspend fun getStaffByIds(ids: List<Long>): List<Staff> {
        return staffDao.getStaffByIds(ids)
    }

}