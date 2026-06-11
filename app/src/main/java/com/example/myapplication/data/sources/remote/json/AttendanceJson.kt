package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Schedule
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class AttendanceJson(
    val id: Int,
    val assignment_id: Int,
    val check_in: String?,
    val check_out: String?,
    val status: String,
    val sync_status: String,
    val created_at: String? = null
) {
    fun toAttendance(): Attendance {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

        val checkInLong = try { sdf.parse(this.check_in)?.time ?: 0L } catch (e: Exception) { 0L }
        val checkOutLong = try { sdf.parse(this.check_out)?.time ?: 0L } catch (e: Exception) { 0L }
        val createdAtLong = try {
            if (this.created_at != null) sdf.parse(this.created_at)?.time ?: 0L else 0L
        } catch (e: Exception) { 0L }

        return Attendance(
            id = this.id,
            assignment_id = this.assignment_id,
            check_in = checkInLong,
            check_out = checkOutLong,
            status = this.status,
            sync_status = this.sync_status,
            created_at = createdAtLong
        )
    }
}