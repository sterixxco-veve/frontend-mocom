package com.example.myapplication

import android.app.Application

class App : Application() {
    companion object {
        lateinit var api: ApiService
    }
    override fun onCreate() {
        super.onCreate()
        api = RetrofitClient.instance
    }
}