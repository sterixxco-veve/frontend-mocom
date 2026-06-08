package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.local.LocalDataSource
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.remote.RemoteDataSource

class DefaultScheduleRepository(
    val localDataSource: LocalDataSource,
    val remoteDataSource: RemoteDataSource
) : ScheduleRepository {

    override suspend fun getAll(): List<Schedule> {
        return try {
            val remoteData = remoteDataSource.fetchAllSchedules()
            remoteData
        } catch (e: Exception) {
            e.printStackTrace()
            localDataSource.getAllSchedule()
        }
    }

    override suspend fun getByCompanyId(id: Int): List<Schedule> {
        return try {
            val remoteData = remoteDataSource.fetchScheduleByCompanyId(id)
            remoteData
        } catch (e: Exception) {
            e.printStackTrace()
            // 💡 OPTIMALISASI: Jika server offline, ambil data lokal yang terfilter company_id juga
            localDataSource.getScheduleByCompanyId(id)
        }
    }

    // =========================================================================
    // 💡 PERBAIKAN UTAMA: Implementasi fungsi update untuk HTTP PUT
    // =========================================================================
    override suspend fun update(schedule: Schedule) {
        try {
            // 1. Tembak perubahan data ke server MySQL lewat remote data source
            remoteDataSource.updateSchedule(schedule)

            // 2. (Opsional) Jika kamu pakai Room untuk offline-first, kamu bisa selipkan update lokal di sini:
            // val entity = ScheduleEntity.fromRawModel(schedule)
            // localDataSource.updateLocal(entity)

        } catch (e: Exception) {
            e.printStackTrace()
            // Lempar kembali error ke atas agar ditangkap oleh Try-Catch milik ViewModel
            throw e
        }
    }

    override suspend fun getById(id: Int): Schedule? {
        return localDataSource.getById(id)
    }

    override suspend fun insert(schedule: Schedule): Schedule {
        val localSchedule = localDataSource.insert(
            createdBy = schedule.created_by,
            company_id = schedule.company_id,
            title = schedule.title,
            description = schedule.description,
            location = schedule.location,
            startTime = schedule.start_time,
            endTime = schedule.end_time
        )

        try {
            remoteDataSource.insertSchedule(localSchedule)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return localSchedule
    }

    override suspend fun sync() {
        try {
            val clientSchedules = localDataSource.getUnsynced()

            val serverSchedules = remoteDataSource.syncSchedule(clientSchedules)

            localDataSource.sync(serverSchedules)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}