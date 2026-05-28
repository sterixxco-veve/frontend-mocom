package com.example.myapplication.data.sources.remote

import com.example.myapplication.data.sources.remote.json.ScheduleJson
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface WebService {
    @PUT("/schedule/{id}")
    suspend fun insertSchedule(@Path("id") id: Int, @Body schedule: ScheduleJson): ScheduleJson
    @POST("schedule/sync")
    suspend fun syncSchedule(@Body body: List<ScheduleJson>): List<ScheduleJson>
}