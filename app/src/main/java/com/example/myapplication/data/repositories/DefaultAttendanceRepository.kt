package com.example.myapplication.data.repositories

import android.util.Log
import com.example.myapplication.data.sources.local.LocalDataSource
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.RemoteDataSource
import com.example.myapplication.data.sources.remote.request.CheckInRequest // 1. PASTIKAN IMPORT INI ADA

class DefaultAttendanceRepository(
    val localDataSource: LocalDataSource,
    val remoteDataSource: RemoteDataSource
) : AttendanceRepository {

    // PERBAIKAN: Mengubah signature agar menerima CheckInRequest dan return Unit sesuai perintah error
    override suspend fun checkIn(request: CheckInRequest) {
        try {
            // Panggil ke remote data source
            // CATATAN: Jika remoteDataSource.checkIn kamu membutuhkan objek 'request', biarkan seperti ini.
            // Namun, jika remoteDataSource butuh angka ID saja, ubah menjadi: remoteDataSource.checkIn(request.assignmentId)
            remoteDataSource.checkIn(request)
        } catch (e: Exception) {
            Log.e("REPOSITORY_CHECKIN", "⚠️ Gagal melakukan check-in: ${e.message}")
            throw e // Lemparkan error agar bisa ditangkap oleh catch blok di ViewModel
        }
    }

    override suspend fun checkOut(attendanceId: Int): Attendance {
        return remoteDataSource.checkOut(attendanceId)
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

    override suspend fun getAttendanceByUserId(
        user_id: Int
    ): List<Attendance> {
        return try {
            remoteDataSource.fetchAttendanceByUserId(user_id)
        } catch (e: Exception) {
            localDataSource.getAttendanceByUserId(user_id)
        }
    }
}