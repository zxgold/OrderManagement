package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.LedgerEntry
import com.example.manager.data.model.enums.LedgerEntryType
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // 允许替换，方便修复错误记录
    suspend fun insertOrUpdateLedgerEntry(entry: LedgerEntry): Long

    @Delete
    suspend fun deleteLedgerEntry(entry: LedgerEntry): Int

    // **核心查询方法：按店铺ID和日期范围获取流水**
    @Query("""
        SELECT * FROM ledger_entries 
        WHERE store_id = :storeId 
        AND entry_date BETWEEN :startDate AND :endDate
        ORDER BY entry_date DESC, id DESC
    """)
    fun getLedgerEntriesByDateRangeFlow(storeId: Long, startDate: Long, endDate: Long): Flow<List<LedgerEntry>>

    // (可选) 用于计算汇总的查询
    @Query("""
        SELECT SUM(amount) FROM ledger_entries 
        WHERE store_id = :storeId AND entry_type = :entryType 
        AND entry_date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalAmountByTypeAndDateRange(storeId: Long, entryType: LedgerEntryType, startDate: Long, endDate: Long): Double?
}