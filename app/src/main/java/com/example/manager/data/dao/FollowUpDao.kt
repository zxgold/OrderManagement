package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.FollowUp
import com.example.manager.data.model.uimodel.FollowUpWithCustomerName
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {
    @Insert
    suspend fun insertFollowUp(followUp: FollowUp): Long

    // **获取特定店铺的所有跟进记录，并连接客户姓名**
    @Query("""
        SELECT f.*, c.name as customerName 
        FROM follow_ups AS f
        INNER JOIN customers AS c ON f.customer_id = c.id
        WHERE c.store_id = :storeId
        ORDER BY f.follow_up_date DESC
    """)
    fun getAllFollowUpsByStoreIdFlow(storeId: Long): Flow<List<FollowUpWithCustomerName>>

    // **获取特定客户的所有跟进记录**
    @Query("SELECT * FROM follow_ups WHERE customer_id = :customerId ORDER BY follow_up_date DESC")
    fun getFollowUpsByCustomerIdFlow(customerId: Long): Flow<List<FollowUp>>
}