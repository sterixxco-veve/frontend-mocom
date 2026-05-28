package com.example.myapplication.data.sources.remote

import android.util.Log
import com.example.myapplication.data.sources.remote.json.ScheduleJson
import com.example.myapplication.data.sources.models.Schedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val mysqlFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

// 1. EKSTENSI: Mengubah Schedule (Lokal Long) -> Ke ScheduleJson (Remote String)
fun Schedule.toScheduleJson(): ScheduleJson {
    return ScheduleJson(
        id = this.id,
        created_by = this.created_by,
        title = this.title,
        description = this.description,
        start_time = Date(this.start_time), // Konversi Long ke Date
        end_time = Date(this.end_time),     // Konversi Long ke Date
        location = this.location,
        created_at = Date(this.created_at)  // Konversi Long ke Date
    )
}

// Ekstensi: Jaringan (Date) -> Ke Model Lokal (Long)
fun ScheduleJson.toSchedule(): Schedule {
    return Schedule(
        id = this.id,
        created_by = this.created_by,
        title = this.title,
        description = this.description,
        start_time = this.start_time.time, // Ambil angka milidetik Long dari Date
        end_time = this.end_time.time,     // Ambil angka milidetik Long dari Date
        location = this.location,
        created_at = this.created_at.time  // Ambil angka milidetik Long dari Date
    )
}
class RetrofitDataSource(private val webService: WebService) : RemoteDataSource {

    override suspend fun insertSchedule(schedule: Schedule): Schedule {
        try {
            // Gunakan fungsi ekstensi .toScheduleJson() yang sudah kita perbaiki di atas
            val requestBody = schedule.toScheduleJson()

            // Kirim ke server via Retrofit webService
            //  KODE YANG BENAR (Baru):
            val response: ScheduleJson = webService.insertSchedule(requestBody)

            // Gunakan fungsi copy manual milik kelas Schedule kamu dengan named argument
            return schedule.copy(id = response.id)

        } catch (e: Exception) {
            e.printStackTrace()
            return schedule
        }
    }

    override suspend fun fetchAllSchedules(): List<Schedule> {
        return try {
            val responseList = webService.getAllSchedules()
            responseList.map { it.toSchedule() }
        } catch (e: Exception) {
            Log.e("MOCOM_ERROR", "Terjadi kegagalan saat fetch data dari Node.js!", e)
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun syncSchedule(schedule: List<Schedule>): List<Schedule> {
        try {
            val requestBody: List<ScheduleJson> = schedule.map { clientData ->
                clientData.toScheduleJson()
            }

            // 2. Kirim data ke server
            val responseList: List<ScheduleJson> = webService.syncSchedule(requestBody)

            // 3. Konversi kembali List<ScheduleJson> menjadi List<Schedule> menggunakan fungsi ekstelinesi
            return responseList.map { serverData ->
                serverData.toSchedule()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return schedule // Jika sync gagal, kembalikan data asal agar tidak crash
        }
    }
}