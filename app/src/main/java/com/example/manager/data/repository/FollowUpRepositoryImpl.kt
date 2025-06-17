package com.example.manager.data.repository


import com.example.manager.data.dao.FollowUpDao
import com.example.manager.data.model.entity.FollowUp
import com.example.manager.data.model.uimodel.FollowUpWithCustomerName
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowUpRepositoryImpl @Inject constructor(
    private val followUpDao: FollowUpDao
) : FollowUpRepository {
    override fun getAllFollowUpsByStoreIdFlow(storeId: Long): Flow<List<FollowUpWithCustomerName>> {
        return followUpDao.getAllFollowUpsByStoreIdFlow(storeId)
    }
    override suspend fun insertFollowUp(followUp: FollowUp): Result<Long> {
        return try {
            Result.success(followUpDao.insertFollowUp(followUp))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}