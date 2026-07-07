package com.example.myapplication.data.repositories

import android.util.Log
import com.example.myapplication.data.sources.local.LocalDataSource
import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.remote.RemoteDataSource

class DefaultAnnouncementRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) : AnnouncementRepository {

    override suspend fun getAnnouncements(): List<Announcement> {
        return try {
            val remoteData = remoteDataSource.fetchAnnouncements()
            remoteData
        } catch (e: Exception) {
            Log.e("REPOSITORY_GET", "⚠️ Server offline, mengambil data dari Room lokal: ${e.message}")
            localDataSource.getAllAnnouncement()
        }

    }

    override suspend fun insert(announcement: Announcement): Announcement {
        val localAnnouncement = localDataSource.insertAnnouncement(
            title = announcement.title,
            message = announcement.message,
            createdBy = announcement.created_by,
        )

        try {
            remoteDataSource.insertAnnouncement(localAnnouncement)
            Log.d("REPOSITORY_INSERT", "🚀 Sukses mengunggah jadwal baru ke database server.")
        } catch (e: Exception) {
            Log.e("REPOSITORY_INSERT", "⚠️ Server offline! Jadwal tertahan di database lokal HP.")
        }

        return localAnnouncement
    }
}