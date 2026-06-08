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
        // Sesuaikan format pola (pattern) sesuai dengan string yang dikirim oleh Node.js kamu
        // Jika dari Node.js formatnya "2025-01-01 08:00:00", maka polanya adalah "yyyy-MM-dd HH:mm:ss"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Atur timezone ke UTC atau Asia/Jakarta agar perhitungan jamnya tidak meleset
        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

        // Proses mengubah String teks menjadi Long (milidetik) dengan proteksi fallback (0L) jika teksnya corrupt
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
            start_time = startTimeLong,  // 💡 Sekarang sudah aman berwujud Long
            end_time = endTimeLong,      // 💡 Sekarang sudah aman berwujud Long
            location = this.location,
            created_at = createdAtLong   // 💡 Sekarang sudah aman berwujud Long
        )
    }
}