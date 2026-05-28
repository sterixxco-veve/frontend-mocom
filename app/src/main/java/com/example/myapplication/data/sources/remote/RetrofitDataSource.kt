package com.example.myapplication.data.sources.remote

import com.example.myapplication.data.sources.remote.json.ScheduleJson
import com.example.myapplication.data.sources.models.Schedule
import java.util.Date

fun Schedule.toScheduleJson(): ScheduleJson{
    return ScheduleJson(id, created_by, title, description, start_time, end_time,location,created_at)
}
fun ScheduleJson.toSchedule(): Schedule{
    return Schedule(id, created_by, title, description, start_time, end_time, location, created_at)
}

class RetrofitDataSource(private val webService: WebService) : RemoteDataSource {

    override suspend fun insertSchedule(schedule: Schedule): Schedule {
        try {
            val scheduleId = schedule.id ?: 0

            val requestBody = ScheduleJson(
                id = scheduleId,
                created_by = schedule.created_by,
                title = schedule.title,
                description = schedule.description ?: "",
                start_time = schedule.start_time,
                end_time = schedule.end_time,
                location = schedule.location ?: "",
                created_at = schedule.created_at
            )

            // 3. FIX: Kirim DUA argumen (id untuk @Path dan requestBody untuk @Body)
            val response: ScheduleJson = webService.insertSchedule(scheduleId, requestBody)

            // 4. Ambil ID hasil respons server dan kembalikan dalam bentuk objek 'Schedule' lokal
            return schedule.copy(id = response.id)

        } catch (e: Exception) {
            e.printStackTrace()
            // Jika gagal koneksi/offline, kembalikan data asli (ID tetap 0) agar tersimpan di lokal
            return schedule
        }
    }

    override suspend fun syncSchedule(schedule: List<Schedule>): List<Schedule> {
        // 1. Konversi List<Schedule> menjadi List<ScheduleJson>
        val requestBody: List<ScheduleJson> = schedule.map { clientData ->
            ScheduleJson(
                id = clientData.id ?: 0,
                created_by = clientData.created_by,
                title = clientData.title,
                description = clientData.description ?: "",
                start_time = clientData.start_time,
                end_time = clientData.end_time,
                location = clientData.location ?: "",
                created_at = clientData.created_at
            )
        }

        // 2. Kirim data ke server
        val responseList: List<ScheduleJson> = webService.syncSchedule(requestBody)

        // 3. Konversi kembali List<ScheduleJson> menjadi List<Schedule> untuk kebutuhan lokal Repository
        return responseList.map { serverData ->
            Schedule(
                id = serverData.id,
                created_by = serverData.created_by,
                title = serverData.title,
                description = serverData.description,

                // Kembalikan dalam format String agar diterima oleh model Schedule Anda
                start_time = serverData.start_time,
                end_time = serverData.end_time,
                location = serverData.location,
                created_at = serverData.created_at
            )
        }
    }
}