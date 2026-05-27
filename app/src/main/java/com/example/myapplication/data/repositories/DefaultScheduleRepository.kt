package com.example.myapplication.data.repositories

import com.example.myapplication.data.local.LocalDataSource
import com.example.myapplication.data.local.dao.ScheduleDao
import com.example.myapplication.data.local.entities.ScheduleEntity
import com.example.myapplication.data.remote.RemoteDataSource
import com.example.myapplication.domain.models.Schedule

class DefaultScheduleRepository(
    val localDataSource: LocalDataSource,       // Bertindak sebagai LocalDataSource Anda (Room DAO)
    val remoteDataSource: RemoteDataSource // Bertindak sebagai RemoteDataSource Anda (Retrofit API)
) : ScheduleRepository {

    override suspend fun getAll(): List<Schedule> {
        return localDataSource.getAll()
    }

    override suspend fun getById(id: Int): Schedule? {
        // Mengasumsikan Anda memiliki fungsi getById(id) di ScheduleDao Anda
        return localDataSource.getById(id)
    }

    override suspend fun insert(schedule: Schedule): Schedule {
        // Memecah objek schedule menjadi parameter satuan sesuai yang diminta localDataSource
        val localSchedule = localDataSource.insert(
            createdBy = schedule.created_by,
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
            // 1. Ambil data jadwal lokal yang dibuat saat offline (ID masih 0)
            val clientSchedules = localDataSource.getUnsynced()

            // 2. Kirim data offline ke server, dan server mengembalikan daftar jadwal terbaru & tervalid
            val serverSchedules = remoteDataSource.syncSchedule(clientSchedules)

            // 3. Simpan/perbarui semua data dari server tersebut ke database lokal Room
            localDataSource.sync(serverSchedules)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}