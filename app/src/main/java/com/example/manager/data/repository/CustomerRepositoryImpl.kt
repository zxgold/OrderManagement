package com.example.manager.data.repository

import android.database.sqlite.SQLiteConstraintException
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
@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override suspend fun insertCustomer(customer: Customer): Result<Long> {
        return try {
            // 确保传入的 Customer 对象已经设置了正确的 storeId
            val newId = customerDao.insertCustomer(customer)
            if (newId > 0) {
                Result.success(newId)
            } else {
                Result.failure(Exception("插入客户失败，DAO返回ID无效"))
            }
        } catch (e: SQLiteConstraintException) {
            // 特别处理唯一性约束冲突 (例如 (store_id, phone) 重复)
            Result.failure(e) // 将原始异常传递出去，ViewModel 可以据此判断
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCustomer(customer: Customer): Result<Int> {
        return try {
            // 确保传入的 Customer 对象已经设置了正确的 storeId 和 id
            val updatedRows = customerDao.updateCustomer(customer)
            // 检查电话号码唯一性 (如果电话有变动) - 这部分逻辑也可以放在 ViewModel
            // 如果在 update 时也可能触发唯一性冲突，也需要 try-catch SQLiteConstraintException
            Result.success(updatedRows)
        } catch (e: SQLiteConstraintException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomer(customer: Customer): Int {
        return customerDao.deleteCustomer(customer)
    }

    override suspend fun deleteCustomerByIdAndStoreId(customerId: Long, storeId: Long): Int {
        return customerDao.deleteCustomerByIdAndStoreId(customerId, storeId)
    }

    override suspend fun getCustomerByIdAndStoreId(id: Long, storeId: Long): Customer? {
        return customerDao.getCustomerByIdAndStoreId(id, storeId)
    }

    override suspend fun getCustomerByPhoneAndStoreId(phone: String, storeId: Long): Customer? {
        return customerDao.getCustomerByPhoneAndStoreId(phone, storeId)
    }

    override suspend fun getAllCustomersByStoreId(storeId: Long): List<Customer> {
        return customerDao.getAllCustomersByStoreId(storeId)
    }

    override suspend fun searchCustomers(query: String, storeId: Long): List<Customer> {
        return customerDao.searchCustomersByStoreId(query, storeId)
    }
}