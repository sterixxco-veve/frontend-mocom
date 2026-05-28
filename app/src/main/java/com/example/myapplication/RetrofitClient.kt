package com.example.myapplication

import com.example.myapplication.data.sources.remote.WebService
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // Pastikan pakai GSON murni

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // KUNCI UTAMA: GSON akan otomatis mengubah String MySQL Datetime menjadi Objek Date Java secara global
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    val webService: WebService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)) // GSON Converter didaftarkan di sini
            .build()
            .create(WebService::class.java)
    }
}