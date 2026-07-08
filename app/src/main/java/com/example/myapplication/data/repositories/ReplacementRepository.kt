package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.models.NotificationReplacement
import com.example.myapplication.data.sources.models.ReplacementDetail
import com.example.myapplication.data.sources.models.ReplacementItem

interface ReplacementRepository {
    suspend fun getReplacementRequests(
        companyId:Int
    ): List<ReplacementItem>

    suspend fun getReplacementDetail(
        id:Int
    ): ReplacementDetail

    suspend fun approveReplacement(
        replacementId:Int,
        approvedBy:Int
    )

    suspend fun rejectReplacement(
        replacementId:Int
    )

    suspend fun getReplacementNotifications(
        userId:Int
    ):List<NotificationReplacement>
}