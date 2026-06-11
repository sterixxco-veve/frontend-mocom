package com.example.myapplication.data.sources.remote.request

import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Schedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class AttendanceRequest(
    val assignment_id: Int,
    val check_in: String?,
    val check_out: String?,
    val status: String?,
    val sync_status: String?
) {
    companion object {
        // 💡 Fungsi ekstensi untuk mengubah objek Schedule (Long) menjadi ScheduleRequest (String)
        fun fromModel(attendance: Attendance): AttendanceRequest {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

            return AttendanceRequest(
                assignment_id = attendance.assignment_id,
                check_in = timeFormat.format(Date(attendance.check_in)),
                check_out = timeFormat.format(Date(attendance.check_out)),
                status = attendance.status,
                sync_status = attendance.sync_status
            )
        }
    }
}