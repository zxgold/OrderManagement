package com.example.manager.data.dao

import androidx.room.*
import com.example.manager.data.model.entity.LedgerEntry
import com.example.manager.data.model.enums.LedgerEntryType

// 用于接收聚合查询结果
data class LedgerSummary(
    val entryType: LedgerEntryType,
    val totalAmount: Double
)

@Dao
interface LedgerEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT) // 账目记录通常不应替换或轻易修改
    suspend fun insertLedgerEntry(ledgerEntry: LedgerEntry): Long

    // Update 和 Delete 通常不提供，或需要特殊权限控制
    // @Update
    // suspend fun updateLedgerEntry(ledgerEntry: LedgerEntry): Int
    // @Delete
    // suspend fun deleteLedgerEntry(ledgerEntry: LedgerEntry): Int

    @Query("SELECT * FROM ledger_entries WHERE id = :id")
    suspend fun getLedgerEntryById(id: Long): LedgerEntry?

    @Query("SELECT * FROM ledger_entries ORDER BY entry_date DESC, id DESC")
    suspend fun getAllLedgerEntries(): List<LedgerEntry>

    @Query("SELECT * FROM ledger_entries WHERE entry_date BETWEEN :startDate AND :endDate ORDER BY entry_date DESC, id DESC")
    suspend fun getLedgerEntriesByDateRange(startDate: Long, endDate: Long): List<LedgerEntry>

    @Query("SELECT * FROM ledger_entries WHERE entry_type = :entryType AND entry_date BETWEEN :startDate AND :endDate ORDER BY entry_date DESC, id DESC")
    suspend fun getLedgerEntriesByTypeAndDateRange(entryType: LedgerEntryType, startDate: Long, endDate: Long): List<LedgerEntry>

    @Query("SELECT * FROM ledger_entries WHERE related_order_id = :orderId ORDER BY entry_date ASC")
    suspend fun getLedgerEntriesByOrderId(orderId: Long): List<LedgerEntry>

    // 计算日期范围内的总收入和总支出
    @Query("SELECT entry_type as entryType, SUM(amount) as totalAmount FROM ledger_entries WHERE entry_date BETWEEN :startDate AND :endDate GROUP BY entry_type")
    suspend fun getLedgerSummaryByDateRange(startDate: Long, endDate: Long): List<LedgerSummary> // 返回包含收支总额的对象列表

    // 计算总余额 (需要查询所有记录，可能在 Repository 中做更合适)
    // @Query("SELECT SUM(CASE WHEN entry_type = 'INCOME' THEN amount ELSE -amount END) FROM ledger_entries")
    // suspend fun getTotalBalance(): Double?
}