package com.example.manager.data.repository

// 定义需要暴露给ViewModel的客户相关操作方法
import com.example.manager.data.model.entity.Customer

/**
 * 客户数据的repository接口
 *
 * 这是一个标准的Kotlin接口
 * 方法签名通常与对应的DAO方法类似，但Repository可以根据需要组合或修改DAO调用（？这里存疑）
 * 我们这里先保持简单，直接映射DAO的主要功能
 * 所有涉及数据库操作的方法都标记为suspend（为什么？）
 */
interface CustomerRepository {
    // 添加客户时，Customer 对象应已包含正确的 storeId
    suspend fun insertCustomer(customer: Customer): Result<Long> // 返回 Result 以便处理唯一性冲突

    suspend fun updateCustomer(customer: Customer): Result<Int> // 返回 Result

    suspend fun deleteCustomer(customer: Customer): Int

    suspend fun deleteCustomerByIdAndStoreId(customerId: Long, storeId: Long): Int

    suspend fun getCustomerByIdAndStoreId(id: Long, storeId: Long): Customer?

    suspend fun getCustomerByPhoneAndStoreId(phone: String, storeId: Long): Customer?

    suspend fun getAllCustomersByStoreId(storeId: Long): List<Customer>

    suspend fun searchCustomers(query: String, storeId: Long): List<Customer>
}