package com.example.myapplication.data.repositories

import android.util.Log
import com.example.myapplication.data.sources.local.LocalDataSource
import com.example.myapplication.data.sources.models.Assignment
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.MySchedule
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.RemoteDataSource

class DefaultAssignmentRepository(
    val localDataSource: LocalDataSource,
    val remoteDataSource: RemoteDataSource
) : AssignmentRepository {

    override suspend fun getMySchedule(user_id: Int): List<MySchedule> {
        return try {
            val remoteData = remoteDataSource.getMySchedule(user_id)
            remoteData
        } catch (e: Exception) {
            Log.e("REPOSITORY_GET_COMP", "⚠️ Server offline, memuat data lokal terfilter untuk User ID ${user_id}")
            emptyList()
        }
    }

    // PERBAIKAN: Tambahkan fungsi override yang diminta oleh interface di bawah ini
    override suspend fun getAssignmentByUserId(user_id: Int): List<MySchedule> {
        return try {
            val remoteData = remoteDataSource.getMySchedule(user_id)
            remoteData
        } catch (e: Exception) {
            Log.e("REPOSITORY_GET_ASSIGN", "⚠️ Gagal mengambil data assignment untuk User ID ${user_id}: ${e.message}")
            emptyList()
        }
    }

    override suspend fun confirmAssignmentStatus(assignmentId: Int, status: String): Boolean {
        return try {
            val response = remoteDataSource.updateAssignmentStatus(assignmentId, mapOf("status" to status))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("REPOSITORY_CONFIRM", "Gagal memperbarui status assignment: ${e.message}")
            false
        }
    }
}