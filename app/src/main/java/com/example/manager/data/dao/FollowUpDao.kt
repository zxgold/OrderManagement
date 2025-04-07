package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.FollowUp
import com.example.manager.data.model.enums.FollowUpStatus

@Dao
interface FollowUpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // 计划可以更新
    suspend fun insertOrUpdateFollowUp(followUp: FollowUp): Long

    @Update
    suspend fun updateFollowUp(followUp: FollowUp): Int

    @Delete
    suspend fun deleteFollowUp(followUp: FollowUp): Int

    @Query("SELECT * FROM follow_ups WHERE id = :id")
    suspend fun getFollowUpById(id: Long): FollowUp?

    @Query("SELECT * FROM follow_ups WHERE order_id = :orderId ORDER BY sequence ASC")
    suspend fun getFollowUpsByOrderId(orderId: Long): List<FollowUp>

    @Query("SELECT * FROM follow_ups WHERE customer_id = :customerId ORDER BY scheduled_date DESC")
    suspend fun getFollowUpsByCustomerId(customerId: Long): List<FollowUp>

    // 查询待处理的回访 (今天及之前到期的)
    @Query("SELECT * FROM follow_ups WHERE status = :pendingStatus AND scheduled_date <= :today ORDER BY scheduled_date ASC")
    suspend fun getPendingFollowUps(today: Long = System.currentTimeMillis(), pendingStatus: FollowUpStatus = FollowUpStatus.PENDING): List<FollowUp>

    // 获取某个订单最新的已完成回访
    @Query("SELECT * FROM follow_ups WHERE order_id = :orderId AND status = :completedStatus ORDER BY actual_follow_up_date DESC LIMIT 1")
    suspend fun getLastCompletedFollowUpForOrder(orderId: Long, completedStatus: FollowUpStatus = FollowUpStatus.COMPLETED): FollowUp?

    // 获取某个订单下一个待处理的回访
    @Query("SELECT * FROM follow_ups WHERE order_id = :orderId AND status = :pendingStatus ORDER BY sequence ASC LIMIT 1")
    suspend fun getNextPendingFollowUpForOrder(orderId: Long, pendingStatus: FollowUpStatus = FollowUpStatus.PENDING): FollowUp?

    // 更新回访状态 (完成)
    // 这里有一个典型的
    @Query("UPDATE follow_ups SET status = :status, actual_follow_up_date = :actualDate, staff_id = :staffId, notes = :notes, updated_at = :updateTime WHERE id = :followUpId")
    suspend fun completeFollowUp(followUpId: Long, status: FollowUpStatus = FollowUpStatus.COMPLETED, actualDate: Long, staffId: Long, notes: String?, updateTime: Long = System.currentTimeMillis()): Int

    // 更新回访状态 (跳过)
    @Query("UPDATE follow_ups SET status = :status, updated_at = :updateTime WHERE id = :followUpId")
    suspend fun skipFollowUp(followUpId: Long, status: FollowUpStatus = FollowUpStatus.SKIPPED, updateTime: Long = System.currentTimeMillis()): Int
}