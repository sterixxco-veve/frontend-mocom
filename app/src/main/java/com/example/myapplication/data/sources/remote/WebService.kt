package com.example.myapplication.data.sources.remote

import com.example.myapplication.data.sources.remote.json.ScheduleJson
import com.example.myapplication.data.sources.remote.request.ScheduleRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface WebService {
    @GET("api/getAllSchedules")
    suspend fun getAllSchedules(): List<ScheduleJson>

    @POST("api/insertSchedules")
    suspend fun insertSchedule(@Body schedule: ScheduleRequest): ScheduleJson  // ganti ScheduleJson -> ScheduleRequest

    @POST("schedule/sync")
    suspend fun syncSchedule(@Body body: List<ScheduleJson>): List<ScheduleJson>
}