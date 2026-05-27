package com.example.myapplication.data.remote.api

import com.example.myapplication.domain.models.Schedule
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ScheduleApiService {
    @GET("api/schedules")
    suspend fun getSchedules(): List<Schedule>

    @POST("api/schedules")
    suspend fun addSchedule(@Body schedule: Schedule): Map<String, Any>
}