package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.remote.RemoteDataSource

class DefaultReplacementRepository(val remoteDataSource: RemoteDataSource): ReplacementRepository {
    override suspend fun getReplacementRequests(
        companyId:Int
    )= remoteDataSource.fetchReplacementRequests(companyId)
    override suspend fun getReplacementDetail(id:Int)
            = remoteDataSource.fetchReplacementDetail(id)
    override suspend fun approveReplacement(
        replacementId:Int,
        approvedBy:Int
    ){
        remoteDataSource.approveReplacement(
            replacementId,
            approvedBy
        )
    }
    override suspend fun rejectReplacement(
        replacementId:Int
    ){
        remoteDataSource.rejectReplacement(
            replacementId
        )
    }

    override suspend fun getReplacementNotifications(
        userId:Int
    )=remoteDataSource
        .fetchReplacementNotifications(userId)
}