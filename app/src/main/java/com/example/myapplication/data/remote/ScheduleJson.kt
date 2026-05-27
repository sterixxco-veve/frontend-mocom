package com.example.myapplication.data.remote

import java.util.Date

class ScheduleJson (
    val id: Int,
    val created_by: Int,
    var content: String,
    val title: String,
    val description: String,
    val location: String,
    val start_time: Long = Date().time,
    val end_time: Long = Date().time,
    var created_at: Long = Date().time
)