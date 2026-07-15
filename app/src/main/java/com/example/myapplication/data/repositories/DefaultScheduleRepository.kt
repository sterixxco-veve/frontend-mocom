package com.example.myapplication.data.repositories

import android.util.Log
import com.example.myapplication.data.sources.local.LocalDataSource
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.RemoteDataSource

class DefaultScheduleRepository(
    val localDataSource: LocalDataSource,
    val remoteDataSource: RemoteDataSource
) : ScheduleRepository {

    override suspend fun getAll(): List<Schedule> {
        return try {
            val remoteData = remoteDataSource.fetchAllSchedules()
            localDataSource.sync(remoteData)
            remoteData
        } catch (e: Exception) {
            Log.e("REPOSITORY_GET", "⚠️ Server offline, mengambil data dari Room lokal: ${e.message}")
            localDataSource.getAllSchedule()
        }
    }

    override suspend fun getByCompanyId(id: Int): List<Schedule> {
        return try {
            val remoteData = remoteDataSource.fetchScheduleByCompanyId(id)
            try {
                localDataSource.sync(remoteData)
            } catch (syncEx: Exception) {
                Log.e("REPOSITORY_GET_COMP", "⚠️ Gagal melakukan sinkronisasi lokal (sync): ${syncEx.message}")
            }
            remoteData
        } catch (e: Exception) {
            Log.e("REPOSITORY_GET_COMP", "⚠️ Server offline, memuat data lokal terfilter untuk Company ID $id: ${e.message}")
            localDataSource.getScheduleByCompanyId(id)
        }
    }

    override suspend fun getById(id: Int): Schedule? {
        // Ambil langsung dari lokal karena data lokal dipastikan sudah merepresentasikan server
        return localDataSource.getById(id)
    }

    // =========================================================================
    // 💡 UPDATE OFFLINE-FIRST
    // =========================================================================
    override suspend fun update(schedule: Schedule) {
        try {
            // 1. Perbarui data lokal di Room terlebih dahulu agar UI langsung berubah seketika
            localDataSource.updateScheduleLocal(schedule)

            // 2. Coba kirim perubahan data ke database MySQL Server Cloud
            remoteDataSource.updateSchedule(schedule)
            Log.d("REPOSITORY_UPDATE", "🚀 Sukses memperbarui jadwal di lokal dan server cloud.")
        } catch (e: Exception) {
            Log.e("REPOSITORY_UPDATE", "⚠️ Gagal update ke server (Offline), data tersimpan di lokal saja: ${e.message}")
            // Catatan: Jika ingin diproses sync nanti, kamu bisa mengubah flag status is_synced = 0 di tabel lokalmu di sini
        }
    }

    // =========================================================================
    // 💡 INSERT OFFLINE-FIRST
    // =========================================================================
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
            Log.d("REPOSITORY_INSERT", "🚀 Sukses mengunggah jadwal baru ke database server.")
        } catch (e: Exception) {
            Log.e("REPOSITORY_INSERT", "⚠️ Server offline! Jadwal tertahan di database lokal HP.")
        }

        return localSchedule
    }

    // =========================================================================
    // 💡 DELETE OFFLINE-FIRST
    // =========================================================================
    override suspend fun delete(id: Int) {
        try {
            // 1. Hapus entitas di database Room lokal terlebih dahulu agar menghilang dari RecyclerView
            localDataSource.deleteScheduleLocalById(id)

            // 2. Coba kirim instruksi hapus ke server cloud via remote data source
            remoteDataSource.deleteSchedule(id)
            Log.d("REPOSITORY_DELETE", "🚀 Sukses menghapus jadwal di lokal dan server cloud.")
        } catch (e: Exception) {
            Log.e("REPOSITORY_DELETE", "⚠️ Gagal menghapus di server (Offline), penghapusan tertahan di lokal: ${e.message}")
        }
    }

    override suspend fun sync() {
        try {
            // Mengambil semua jadwal lokal yang belum tersinkronisasi (flag is_synced = 0)
            val clientSchedules = localDataSource.getUnsynced()

            if (clientSchedules.isNotEmpty()) {
                Log.d("REPOSITORY_SYNC", "🔄 Menemukan ${clientSchedules.size} data offline baru. Memulai sinkronisasi...")
                val serverSchedules = remoteDataSource.syncSchedule(clientSchedules)
                localDataSource.sync(serverSchedules)
                Log.d("REPOSITORY_SYNC", "✅ Sinkronisasi cloud berhasil tuntas!")
            }
        } catch (e: Exception) {
            Log.e("REPOSITORY_SYNC", "❌ Gagal menjalankan sinkronisasi: ${e.message}")
        }
    }
}