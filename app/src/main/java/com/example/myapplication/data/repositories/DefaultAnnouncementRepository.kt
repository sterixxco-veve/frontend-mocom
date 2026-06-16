package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.remote.RemoteDataSource

class DefaultAnnouncementRepository(
    private val remoteDataSource: RemoteDataSource
) : AnnouncementRepository {

    override suspend fun getAnnouncements(): List<Announcement> {
        return remoteDataSource.fetchAnnouncements()
    }
}