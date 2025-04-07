package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.Customer

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer): Int

    @Delete
    suspend fun deleteCustomer(customer: Customer): Int // 注意：有关联订单时可能因外键约束失败

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    // 使用 LIKE 进行模糊搜索，注意 '%' 的拼接
    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchCustomers(query: String): List<Customer>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomers(): List<Customer>
}