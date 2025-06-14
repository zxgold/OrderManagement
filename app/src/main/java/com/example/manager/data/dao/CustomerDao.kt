package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Customer

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer): Int

    @Delete
    suspend fun deleteCustomer(customer: Customer): Int // 注意：有关联订单时可能因外键约束失败

    @Query("SELECT * FROM customers WHERE id = :id AND store_id = :storeId") // 获取客户时也校验店铺ID
    suspend fun getCustomerByIdAndStoreId(id: Long, storeId: Long): Customer?

    // 根据电话和店铺ID查找客户，用于唯一性校验
    @Query("SELECT * FROM customers WHERE phone = :phone AND store_id = :storeId LIMIT 1")
    suspend fun getCustomerByPhoneAndStoreId(phone: String, storeId: Long): Customer?

    // 根据店铺ID获取所有客户
    @Query("SELECT * FROM customers WHERE store_id = :storeId ORDER BY name ASC")
    suspend fun getAllCustomersByStoreId(storeId: Long): List<Customer>

    // 根据店铺ID和查询条件搜索客户
    @Query("SELECT * FROM customers WHERE store_id = :storeId AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name ASC")
    suspend fun searchCustomersByStoreId(query: String, storeId: Long): List<Customer>


    @Query("DELETE FROM customers WHERE id = :customerId AND store_id = :storeId")
    suspend fun deleteCustomerByIdAndStoreId(customerId: Long, storeId: Long): Int // 返回删除的行数


}