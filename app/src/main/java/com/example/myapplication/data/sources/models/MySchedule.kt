package com.example.myapplication.data.sources.models

data class MySchedule(

    val assignment_id: Int,
    val schedule_id: Int,
    val user_id: Int,

    val role_in_event: String?,
    val job_desc: String?,
    val status: String? = null,

    val title: String,
    val description: String? = null,

    val start_time: String,
    val end_time: String,

    val location: String?

)