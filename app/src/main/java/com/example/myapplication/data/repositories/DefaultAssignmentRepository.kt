package com.example.myapplication.data.repositories

import android.util.Log
import com.example.myapplication.data.sources.local.LocalDataSource
import com.example.myapplication.data.sources.models.Assignment
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.RemoteDataSource

class DefaultAssignmentRepository(
    val localDataSource: LocalDataSource,
    val remoteDataSource: RemoteDataSource
) : AssignmentRepository {

    override suspend fun getAssignmentByUserId(user_id: Int): List<Assignment> {
        return try {
            val remoteData = remoteDataSource.fetchAssignmentByUserId(user_id)
            remoteData
        } catch (e: Exception) {
            Log.e("REPOSITORY_GET_COMP", "⚠️ Server offline, memuat data lokal terfilter untuk User ID ${user_id}")
            localDataSource.getAssignmentByUserId(user_id)
        }
    }
}