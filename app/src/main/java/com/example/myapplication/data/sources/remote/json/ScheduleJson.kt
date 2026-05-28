package com.example.myapplication.data.sources.remote.json

import com.squareup.moshi.JsonClass
import java.util.Date

data class ScheduleJson(
    val id: Int,
    val created_by: Int,
    val title: String,
    val description: String? = null,
    val start_time: String,        // ← String, bukan Date
    val end_time: String,          // ← String, bukan Date
    val location: String? = null,
    val created_at: String? = null // ← String, bukan Date
)