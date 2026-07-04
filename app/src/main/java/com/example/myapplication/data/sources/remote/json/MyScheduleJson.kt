package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.MySchedule
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class MyScheduleJson(

    val assignment_id: Int,
    val schedule_id: Int,
    val user_id: Int,

    val role_in_event: String? = null,
    val job_desc: String? = null,
    val status: String? = null,

    val title: String,
    val description: String? = null,

    val start_time: String,
    val end_time: String,

    val location: String? = null

) {

    fun toMySchedule(): MySchedule {

        return MySchedule(

            assignment_id = assignment_id,
            schedule_id = schedule_id,
            user_id = user_id,

            role_in_event = role_in_event,
            job_desc = job_desc,

            status = status,

            title = title,
            description = description,

            start_time = start_time,
            end_time = end_time,

            location = location

        )

    }

}