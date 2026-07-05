package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.Assignment
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class AssignmentJson(
    // 🟢 Menggunakan alternate agar aman membaca key "id" maupun "assignment_id" dari Node.js
    @SerializedName(value = "id", alternate = ["assignment_id"])
    val id: Int,

    val schedule_id: Int,
    val user_id: Int,
    val role_in_event: String,
    val job_desc: String,
    val status: String,
    val assigned_at: String? = null,

    // =========================================================================
    // 🟢 TAMBAHAN PROPERTI HASIL JOIN UNTUK RECYCLER VIEW SHIFT HARI INI
    // =========================================================================
    val title: String = "",
    val description: String? = null,
    val start_time: String = "",
    val end_time: String = "",
    val location: String = ""
) {
    fun toAssignment(): Assignment {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

        val assignedAtLong = try {
            if (this.assigned_at != null) sdf.parse(this.assigned_at)?.time ?: 0L else 0L
        } catch (e: Exception) { 0L }

        return Assignment(
            id = this.id,
            schedule_id = this.schedule_id,
            user_id = this.user_id,
            role_in_event = this.role_in_event,
            job_desc = this.job_desc,
            status = this.status,
            assigned_at = assignedAtLong
        )
    }
}