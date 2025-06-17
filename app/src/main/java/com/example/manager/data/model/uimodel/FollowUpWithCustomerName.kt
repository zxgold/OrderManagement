package com.example.manager.data.model.uimodel

import androidx.room.Embedded
import com.example.manager.data.model.entity.FollowUp

data class FollowUpWithCustomerName(
    @Embedded
    val followUp: FollowUp,
    val customerName: String
)