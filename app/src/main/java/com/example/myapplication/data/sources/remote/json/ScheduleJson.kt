package com.example.myapplication.data.sources.remote.json

import java.util.Date

class ScheduleJson (
    val id: Int,
    val created_by: Int,
    val title: String,
    val description: String?,
    val start_time: Long = Date().time,
    val end_time: Long = Date().time,
    val location: String?,
    var created_at: Long = Date().time
)