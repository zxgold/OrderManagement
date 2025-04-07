package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.ActionLog
import com.example.manager.data.model.enums.ActionLogType

@Dao
interface ActionLogDao {

    @Insert
    suspend fun insertActionLog(actionLog: ActionLog): Long // 日志通常只插入

    // 查询一般按需进行，例如：
    @Query("SELECT * FROM action_logs ORDER BY action_time DESC")
    suspend fun getAllActionLogs(): List<ActionLog>

    @Query("SELECT * FROM action_logs WHERE action_time BETWEEN :startDate AND :endDate ORDER BY action_time DESC")
    suspend fun getActionLogsByDateRange(startDate: Long, endDate: Long): List<ActionLog>

    @Query("SELECT * FROM action_logs WHERE staff_id = :staffId ORDER BY action_time DESC")
    suspend fun getActionLogsByStaffId(staffId: Long): List<ActionLog>

    @Query("SELECT * FROM action_logs WHERE action_type = :actionType ORDER BY action_time DESC")
    suspend fun getActionLogsByType(actionType: ActionLogType): List<ActionLog>

    @Query("SELECT * FROM action_logs WHERE target_entity_type = :entityType AND target_entity_id = :entityId ORDER BY action_time DESC")
    suspend fun getActionLogsForEntity(entityType: String, entityId: Long): List<ActionLog>
}