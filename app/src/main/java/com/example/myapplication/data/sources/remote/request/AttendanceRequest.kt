package com.example.myapplication.data.sources.remote.request

import com.example.myapplication.data.sources.models.Attendance
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
        // 💡 Fungsi ekstensi untuk mengubah objek model domain menjadi request body API
        fun fromModel(attendance: Attendance): AttendanceRequest {

            // =========================================================================
            // 💡 PERBAIKAN: Gunakan format DATETIME penuh agar MySQL tidak merusak data
            // =========================================================================
            val databaseFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            }

            // Lakukan konversi aman. Jika nilai <= 0 atau null, kirimkan null ke server
            val checkInStr = if (attendance.check_in != null && attendance.check_in.isEmpty()) {
                databaseFormat.format(Date(attendance.check_in))
            } else {
                null
            }

            val checkOutStr = if (attendance.check_out != null && attendance.check_out.isEmpty()) {
                databaseFormat.format(Date(attendance.check_out))
            } else {
                null
            }

            return AttendanceRequest(
                assignment_id = attendance.assignment_id,
                check_in = checkInStr,
                check_out = checkOutStr,
                status = attendance.status,
                sync_status = attendance.sync_status
            )
        }
    }
}