package com.example.myapplication.data.sources.remote

import android.util.Log
import com.example.myapplication.data.sources.remote.json.ScheduleJson
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.request.ScheduleRequest
import com.example.myapplication.data.sources.remote.request.UserRequest
import java.text.SimpleDateFormat
import java.util.*

// Untuk PARSE response dari server (ISO 8601)
private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

// Untuk SEND request ke server (MySQL format)
private val mysqlFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun ScheduleJson.toSchedule(): Schedule {
    return Schedule(
        id = this.id,
        created_by = this.created_by,
        title = this.title,
        description = this.description,
        start_time = isoFormat.parse(this.start_time)?.time ?: 0L,
        end_time = isoFormat.parse(this.end_time)?.time ?: 0L,
        location = this.location,
        created_at = this.created_at?.let { isoFormat.parse(it)?.time }
            ?: System.currentTimeMillis()
    )
}

// Schedule (Long) -> ScheduleRequest (String) untuk INSERT
fun Schedule.toScheduleRequest(): ScheduleRequest {
    return ScheduleRequest(
        created_by = this.created_by,
        company_id = this.company_id,
        title = this.title,
        description = this.description,
        start_time = mysqlFormat.format(Date(this.start_time)),
        end_time = mysqlFormat.format(Date(this.end_time)),
        location = this.location
    )
}

// Schedule (Long) -> ScheduleJson (Date) untuk SYNC
fun Schedule.toScheduleJsonForSync(): ScheduleJson {
    return ScheduleJson(
        id = this.id,
        created_by = this.created_by,
        company_id = this.company_id,
        title = this.title,
        description = this.description,
        start_time = mysqlFormat.format(Date(this.start_time)),   // Long → String
        end_time = mysqlFormat.format(Date(this.end_time)),       // Long → String
        location = this.location,
        created_at = mysqlFormat.format(Date(this.created_at))    // Long → String
    )
}

class RetrofitDataSource(private val webService: WebService) : RemoteDataSource {
//GET ALL
    override suspend fun fetchAllSchedules(): List<Schedule> {
        return try {
            val responseList = webService.getAllSchedules()
            Log.d("DEBUG_FETCH", "Raw response size: ${responseList.size}")
            Log.d("DEBUG_FETCH", "Raw response: $responseList")

            val mapped = responseList.map { it.toSchedule() }
            Log.d("DEBUG_FETCH", "Mapped size: ${mapped.size}")
            mapped
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.javaClass.simpleName} → ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    override suspend fun fetchAllUsers(): List<User> {
        return try {
            val responseList = webService.getAllUsers()
            Log.d("DEBUG_FETCH", "Raw response size: ${responseList.size}")
            Log.d("DEBUG_FETCH", "Raw response: $responseList")

            val mapped = responseList.map { it.toUser() }
            Log.d("DEBUG_FETCH", "Mapped size: ${mapped.size}")
            mapped
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.javaClass.simpleName} → ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    //GET BY COMPANY ID
    override suspend fun fetchScheduleByCompanyId(company_id: Int): List<Schedule> {
        return try {
            val responseList = webService.getSchedulesByCompanyId(company_id)
            Log.d("DEBUG_FETCH", "Raw response size: ${responseList.size}")
            Log.d("DEBUG_FETCH", "Raw response: $responseList")

            val mapped = responseList.map { it.toSchedule() }
            Log.d("DEBUG_FETCH", "Mapped size: ${mapped.size}")
            mapped
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.javaClass.simpleName} → ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    override suspend fun fetchUserByCompanyId(company_id: Int): List<User> {
        return try {
            // 💡 Di sini response bermutasi menjadi retrofit2.Response pembungkus
            val response = webService.getUsersByCompanyId(company_id)

            // Periksa apakah HTTP request sukses dan body tidak kosong
            if (response.isSuccessful && response.body() != null) {
                val responseList = response.body()!! // 💡 Ambil list aslinya di sini

                Log.d("DEBUG_FETCH", "Raw response size: ${responseList.size}")
                Log.d("DEBUG_FETCH", "Raw response: $responseList")

                val mapped = responseList.map { it.toUser() }
                Log.d("DEBUG_FETCH", "Mapped size: ${mapped.size}")
                mapped
            } else {
                Log.e("DEBUG_FETCH", "⚠️ Gagal! Server merespon dengan Code: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.javaClass.simpleName} → ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun fetchUserById(id: Int): User? {
        return try {
TODO("makan")
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.javaClass.simpleName} → ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // INSERT
    override suspend fun insertSchedule(schedule: Schedule): Schedule {
        try {
            val requestBody = ScheduleRequest.fromModel(schedule)

            val responseJson = webService.insertSchedule(requestBody)

            return responseJson.toSchedule()
        } catch (e: Exception) {
            Log.e("RETROFIT_INSERT", "❌ Gagal insert: ${e.message}")
            throw e
        }
    }

    override suspend fun insertUser(user: User): User {
        try {
            TODO("Fungsi insertUser belum diimplementasikan")
        } catch (e: Exception) {
            Log.e("RETROFIT_USER_INSERT", "❌ Gagal insert: ${e.message}")
            throw e
        }
    }


    override suspend fun updateSchedule(schedule: Schedule) {
        try {
            // 💡 Mengubah objek Schedule murni (Long) menjadi format teks JSON Request untuk MySQL
            val requestBody = schedule.toScheduleRequest()

            // Tembak endpoint PUT api/updateSchedule/{id} via Retrofit WebService
            val response = webService.updateSchedule(schedule.id, requestBody)

            if (response.isSuccessful) {
                android.util.Log.d("RETROFIT_UPDATE", "✅ Berhasil mengupdate jadwal ID: ${schedule.id} di server backend.")
            } else {
                android.util.Log.e("RETROFIT_UPDATE", "⚠️ Server menolak update. Code: ${response.code()} -> ${response.errorBody()?.string()}")
                throw Exception("Gagal update di server dengan HTTP Code: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("RETROFIT_UPDATE", "❌ Error koneksi saat updateSchedule: ${e.message}")
            throw e // Lempar error ke atas agar ditangkap oleh try-catch milik AdminViewModel
        }
    }
    override suspend fun updateUser(user: User) {
        try {
            val requestBody = UserRequest.fromModel(user)
        } catch (e: Exception) {
            Log.e("RETROFIT_INSERT", "❌ Gagal insert: ${e.message}")
            throw e
        }
    }

    override suspend fun syncSchedule(schedule: List<Schedule>): List<Schedule> {
        try {
            val requestBody = schedule.map { it.toScheduleJsonForSync() }
            val responseList = webService.syncSchedule(requestBody)
            return responseList.map { it.toSchedule() }
        } catch (e: Exception) {
            e.printStackTrace()
            return schedule
        }
    }
}