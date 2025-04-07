package com.example.manager.data.repository

import com.example.manager.data.dao.CustomerDao // 导入 CustomerDao
import com.example.manager.data.model.entity.Customer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CustomerRepository 的具体实现类
 * @Inject constructor(...) 告诉 Hilt 如何创建这个类的实例
 * 它告诉 Hilt：
 * 1. Hilt 负责创建 CustomerRepositoryImpl 的实例。
 * 2. 要创建实例，需要一个CustomerDao，Hilt会自动查找它在
 *    DatabaseModule中定义的provideCustomerDao方法来获取CustomerDao实例，并传入构造函数
 */
@Singleton // 通常 Repository 也是单例
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao // Hilt 会自动注入 CustomerDao 实例
) : CustomerRepository { // 实现接口

    override suspend fun insertOrUpdateCustomer(customer: Customer): Long {
        // 直接调用 DAO 方法
        return customerDao.insertOrUpdateCustomer(customer)
    }

    override suspend fun deleteCustomer(customer: Customer): Int {
        // 直接调用 DAO 方法
        return customerDao.deleteCustomer(customer)
    }

    override suspend fun getCustomerById(id: Long): Customer? {
        // 直接调用 DAO 方法
        return customerDao.getCustomerById(id)
    }

    override suspend fun searchCustomers(query: String): List<Customer> {
        // 直接调用 DAO 方法
        return customerDao.searchCustomers(query)
    }

    override suspend fun getAllCustomers(): List<Customer> {
        // 直接调用 DAO 方法
        return customerDao.getAllCustomers()
    }
}
