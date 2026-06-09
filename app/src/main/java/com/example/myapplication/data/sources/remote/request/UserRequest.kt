package com.example.myapplication.data.sources.remote.request

import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class UserRequest(
    val role_id: Int,
    val company_id: Int,
    val full_name: String,
    val username: String,
    val email: String,
    val password: String,
    val is_active: Int,
    val creaated_at: String,
    val updated_at: String
) {
    companion object {
        // 💡 Fungsi ekstensi untuk mengubah objek Schedule (Long) menjadi ScheduleRequest (String)
        fun fromModel(user: User): UserRequest {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

            return UserRequest(
                role_id = user.role_id,
                company_id = user.company_id,
                full_name = user.full_name,
                username = user.username,
                email = user.email,
                password = user.password,
                is_active = user.is_active,
                creaated_at = timeFormat.format(Date(user.created_at)),
                updated_at = timeFormat.format(Date(user.updated_at)),
            )
        }
    }
}