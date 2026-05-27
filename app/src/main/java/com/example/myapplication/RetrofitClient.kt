package com.example.myapplication

import com.example.myapplication.domain.models.Schedule
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EduStaffApiService {
    @GET("api/schedules")
    suspend fun getSchedules(): List<Schedule>

    @POST("api/schedules")
    suspend fun addSchedule(@Body schedule: Schedule): Map<String, Any>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // FIX GSON: Memaksa GSON menggunakan format standard DATETIME MySQL secara global
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    val instance: EduStaffApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)) // Memasukkan konfigurasi GSON fix
            .build()
            .create(EduStaffApiService::class.java)
    }
}