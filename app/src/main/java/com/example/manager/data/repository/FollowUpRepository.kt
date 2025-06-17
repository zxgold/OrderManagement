package com.example.manager.data.repository

import com.example.manager.data.model.entity.FollowUp
import com.example.manager.data.model.uimodel.FollowUpWithCustomerName
import kotlinx.coroutines.flow.Flow

interface FollowUpRepository {
    fun getAllFollowUpsByStoreIdFlow(storeId: Long): Flow<List<FollowUpWithCustomerName>>
    suspend fun insertFollowUp(followUp: FollowUp): Result<Long>
}
