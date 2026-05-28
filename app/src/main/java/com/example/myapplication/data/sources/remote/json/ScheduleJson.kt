package com.example.myapplication.data.sources.remote.json

import java.util.Date

data class ScheduleJson(
    val id: Int,
    val created_by: Int,
    val title: String,
    val description: String?,
    val start_time: Date,  // 💡 Ganti dari String menjadi Date!
    val end_time: Date,    // 💡 Ganti dari String menjadi Date!
    val location: String?,
    val created_at: Date   // 💡 Ganti dari String menjadi Date!
)