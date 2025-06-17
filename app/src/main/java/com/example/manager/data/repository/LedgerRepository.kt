package com.example.manager.data.repository

import com.example.manager.data.model.entity.LedgerEntry
import kotlinx.coroutines.flow.Flow
interface LedgerRepository {
    fun getLedgerEntriesByDateRangeFlow(storeId: Long, startDate: Long, endDate: Long): Flow<List<LedgerEntry>>
    suspend fun insertLedgerEntry(entry: LedgerEntry): Result<Long>
    // ... 其他方法
}