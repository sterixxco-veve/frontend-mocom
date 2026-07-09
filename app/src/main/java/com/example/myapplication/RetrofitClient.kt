package com.example.myapplication

import com.example.myapplication.data.sources.remote.WebService
import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // Pastikan pakai GSON murni
import retrofit2.converter.moshi.MoshiConverterFactory
import com.example.myapplication.BuildConfig

import android.os.Build

fun isEmulator(): Boolean {
    return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk_gphone")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("sdk_x86")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator")
}

object RetrofitClient {
    val BASE_URL = if (isEmulator()) {
        "http://backend-mocom.vercel.app"
    } else {
        "http://backend-mocom.vercel.app"
    }

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    val webService: WebService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL + "/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(WebService::class.java)
    }
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL + "/")
            .addConverterFactory(GsonConverterFactory.create(gson)) // Memakai gson date format
            .build()
            .create(ApiService::class.java)
    }
}