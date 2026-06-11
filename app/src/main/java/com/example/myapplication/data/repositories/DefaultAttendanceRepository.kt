package com.example.myapplication.data.repositories

import android.util.Log
import com.example.myapplication.data.sources.local.LocalDataSource
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.RemoteDataSource

class DefaultAttendanceRepository(
    val localDataSource: LocalDataSource,
    val remoteDataSource: RemoteDataSource
) : AttendanceRepository {

    override suspend fun checkIn(
        assignmentId: Int,
        checkInTime: String,
        status: String
    ): Attendance {
        TODO("Not yet implemented")
    }

    override suspend fun checkOut(attendanceId: Int, checkOutTime: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getAttendanceByCompanyId(company_id: Int): List<Attendance> {
        return try {
            val remoteData = remoteDataSource.fetchAttendanceByCompanyId(company_id)
            remoteData
        } catch (e: Exception) {
            Log.e("REPOSITORY_GET_COMP", "⚠️ Server offline, memuat data lokal terfilter untuk Company ID ${company_id}")
            localDataSource.getAttendanceByCompanyId(company_id)
        }
    }
}