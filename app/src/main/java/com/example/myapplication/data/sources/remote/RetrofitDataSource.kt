package com.example.myapplication.data.sources.remote

import android.util.Log
import com.example.myapplication.data.sources.remote.json.ScheduleJson
import com.example.myapplication.data.sources.models.Schedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val mysqlFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

fun Schedule.toScheduleJson(): ScheduleJson {
    return ScheduleJson(
        id = this.id,
        created_by = this.created_by,
        title = this.title,
        description = this.description,
        start_time = Date(this.start_time),
        end_time = Date(this.end_time),
        location = this.location,
        created_at = Date(this.created_at)
    )
}

fun ScheduleJson.toSchedule(): Schedule {
    return Schedule(
        id = this.id,
        created_by = this.created_by,
        title = this.title,
        description = this.description,
        start_time = this.start_time.time,
        end_time = this.end_time.time,
        location = this.location,
        created_at = this.created_at.time
    )
}
class RetrofitDataSource(private val webService: WebService) : RemoteDataSource {
    override suspend fun insertSchedule(schedule: Schedule): Schedule {
        try {
            val requestBody = schedule.toScheduleJson()
            val response: ScheduleJson = webService.insertSchedule(requestBody)
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
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun syncSchedule(schedule: List<Schedule>): List<Schedule> {
        try {
            val requestBody: List<ScheduleJson> = schedule.map { clientData ->
                clientData.toScheduleJson()
            }
            val responseList: List<ScheduleJson> = webService.syncSchedule(requestBody)
            return responseList.map { serverData ->
                serverData.toSchedule()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return schedule
        }
    }
}