package com.example.myapplication.data.sources.remote.request

data class ScheduleRequest(
    val created_by: Int,
    val company_id: Int,
    val title: String,
    val description: String?,
    val start_time: String,
    val end_time: String,
    val location: String?
)