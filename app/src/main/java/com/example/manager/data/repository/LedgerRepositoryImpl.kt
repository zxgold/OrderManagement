package com.example.manager.data.repository

import com.example.manager.data.dao.LedgerEntryDao
import com.example.manager.data.model.entity.LedgerEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


// ... imports ...
@Singleton
class LedgerRepositoryImpl @Inject constructor(
    private val ledgerEntryDao: LedgerEntryDao
) : LedgerRepository {
    override fun getLedgerEntriesByDateRangeFlow(storeId: Long, startDate: Long, endDate: Long): Flow<List<LedgerEntry>> {
        return ledgerEntryDao.getLedgerEntriesByDateRangeFlow(storeId, startDate, endDate)
    }
    override suspend fun insertLedgerEntry(entry: LedgerEntry): Result<Long> {
        return try { Result.success(ledgerEntryDao.insertOrUpdateLedgerEntry(entry)) }
        catch (e: Exception) { Result.failure(e) }
    }
}