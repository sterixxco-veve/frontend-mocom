package com.example.myapplication

import com.example.myapplication.data.sources.remote.WebService
import com.example.myapplication.data.sources.remote.json.ScheduleJson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // GSON secara otomatis mengubah String MySQL Datetime menjadi Objek Date Java
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    // Variabel panggil disesuaikan menjadi 'webService' agar klop dengan Fragment-mu kemarin
    val webService: WebService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(WebService::class.java)
    }
}