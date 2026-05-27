package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.local.LocalDataSource
import com.example.myapplication.data.local.RoomDataSource
import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.data.remote.RemoteDataSource
import com.example.myapplication.data.remote.RetrofitDataSource
import com.example.myapplication.data.remote.WebService
import com.example.myapplication.data.repositories.ScheduleRepository
import com.example.myapplication.data.repositories.DefaultScheduleRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class App : Application() {

    lateinit var scheduleRepository: ScheduleRepository

//    companion object {
//        lateinit var instance: App
//            private set
//    }

    override fun onCreate() {
        super.onCreate()
//        instance = this

        // 1. Konfigurasi Moshi sebagai pengganti GSON untuk JSON Parsing
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        // 2. Konfigurasi Instansiasi Retrofit dengan Moshi Converter
        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("http://192.168.103.61:3000/") // Menggunakan IP Local Server Node.js Anda
            .build()

        // 3. Membuat implementasi dari interface WebService API Anda
        val retrofitService = retrofit.create(WebService::class.java)

        // 4. Inisialisasi DefaultScheduleRepository menggunakan DataSource terpisah
        // FIX: Mengganti baseContext menjadi 'this' agar context Application benar-benar aman
        scheduleRepository = DefaultScheduleRepository(
            RoomDataSource(AppDatabase.getInstance(baseContext)),
            RetrofitDataSource(retrofitService)
        )
    }
}