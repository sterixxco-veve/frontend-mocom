package com.example.myapplication


import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    // 10.0.2.2 adalah localhost (127.0.0.1) khusus untuk Android Emulator
    // http://10.0.2.2:3000/
    // atau gunakan ipconfig untuk dapatkan ipv4 kalian (kalau menggunakan hp masing-masing), jangan lupa windows defendernya
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val instance: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build().create(ApiService::class.java)
}