package com.example.myapplication.data.sources.remote.request

import com.example.myapplication.data.sources.models.Schedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ScheduleRequest(
    val company_id: Int,
    val created_by: Int,
    val title: String,
    val description: String?,
    val start_time: String,
    val end_time: String,
    val location: String?
) {
    companion object {
        // 💡 Fungsi ekstensi untuk mengubah objek Schedule (Long) menjadi ScheduleRequest (String)
        fun fromModel(schedule: Schedule): ScheduleRequest {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

            return ScheduleRequest(
                company_id = schedule.company_id,
                created_by = schedule.created_by,
                title = schedule.title,
                description = schedule.description,
                start_time = timeFormat.format(Date(schedule.start_time)),
                end_time = timeFormat.format(Date(schedule.end_time)),
                location = schedule.location
            )
        }
    }
}