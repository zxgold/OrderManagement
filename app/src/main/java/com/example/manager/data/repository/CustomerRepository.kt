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
    suspend fun insertOrUpdateCustomer(customer: Customer): Long

    suspend fun deleteCustomer(customer: Customer): Int

    suspend fun getCustomerById(id: Long): Customer?

    suspend fun searchCustomers(query: String): List<Customer>

    suspend fun getAllCustomers(): List<Customer>

    // 未来可能添加：
    // fun getAllCustomersFlow(): Flow<List<Customer>> // 使用 Flow 实现观察
}