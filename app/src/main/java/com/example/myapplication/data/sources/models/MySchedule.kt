package com.example.myapplication.data.sources.models

data class MySchedule(

    val assignment_id: Int,
    val schedule_id: Int,
    val user_id: Int,

    val role_in_event: String?,
    val job_desc: String?,
    val status: String,

    val title: String,
    val description: String?,

    val start_time: Long,
    val end_time: Long,

    val location: String?

)