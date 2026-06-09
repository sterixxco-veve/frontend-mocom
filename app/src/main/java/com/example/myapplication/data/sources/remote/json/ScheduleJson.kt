package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.Schedule
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class ScheduleJson(
    val id: Int,
    val created_by: Int,
    val company_id: Int,
    val title: String,
    val description: String? = null,
    val start_time: String,
    val end_time: String,
    val location: String? = null,
    val created_at: String? = null
) {
    fun toSchedule(): Schedule {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

        val startTimeLong = try { sdf.parse(this.start_time)?.time ?: 0L } catch (e: Exception) { 0L }
        val endTimeLong = try { sdf.parse(this.end_time)?.time ?: 0L } catch (e: Exception) { 0L }
        val createdAtLong = try {
            if (this.created_at != null) sdf.parse(this.created_at)?.time ?: 0L else 0L
        } catch (e: Exception) { 0L }

        return Schedule(
            id = this.id,
            created_by = this.created_by,
            company_id = this.company_id,
            title = this.title,
            description = this.description,
            start_time = startTimeLong,
            end_time = endTimeLong,
            location = this.location,
            created_at = createdAtLong
        )
    }
}