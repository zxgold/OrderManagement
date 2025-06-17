package com.example.manager.data.repository

import com.example.manager.data.model.entity.Staff
import kotlinx.coroutines.flow.Flow


/*
 * 员工数据的Repository接口
 * 这是一个用于数据访问的仓库层接口，遵循仓库模式
 * 仓库模式：一种设计模式，作为数据源和应用程序其余部分之间的中介。它提供了一个统一的接口来访问数据
 *      封装了数据检索、存储和管理的逻辑
 *
 * suspend fun：接口中的所有方法都使用了suspend关键字
 * 这是异步操作：数据操作（数据库查询、网络请求等）通常是耗时的，并且不应该再主线程上执行，以避免应用无响应
 * suspend关键字表明这些函数是挂起函数，它们可以在不阻塞线程的情况下暂停执行，等待耗时操作完成
 *
 * 协程支持：挂起函数是kotlin协程的核心，它们只能从其他挂起函数或协程作用域中调用，
 *      这使得编写异步代码更加简洁和直观，避免了回调地狱
 *
 * kotlin协程：一种轻量级的并发设计模式，用于简化异步编程
 *
 * 挂起函数：可以在不阻塞线程的情况下暂停和恢复执行的特殊函数
 *
 *
 */
interface StaffRepository {
    suspend fun getStaffByUsername(username: String): Staff?

    suspend fun insertOrUpdateStaff(staff: Staff): Result<Long> // <-- 建议返回 Result(why？)

    suspend fun updateStaff(staff: Staff): Result<Int> // <-- 建议返回 Result

    suspend fun getStaffById(id: Long): Staff?

    suspend fun getStaffByIds(ids: List<Long>): List<Staff>

    suspend fun isInitialSetupNeeded(): Boolean

    // --- **用 Flow 方法替换旧的列表方法** ---
    fun getAllStaffsByStoreIdFlow(storeId: Long): Flow<List<Staff>>

}