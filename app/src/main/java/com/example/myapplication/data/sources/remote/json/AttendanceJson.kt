package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.Attendance

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
        return Attendance(
            id = this.id,
            assignment_id = this.assignment_id,
            check_in = this.check_in,
            check_out = this.check_out,
            status = this.status,
            sync_status = this.sync_status,
            created_at = this.created_at
        )
    }
}