package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.Announcement
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class AnnouncementJson(
    val id: Int,
    val title: String,
    val message: String,
    val created_by: Int,
    val created_at: String
) {
    fun toAnnouncement(): Announcement {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC") // 'Z' berarti UTC, bukan Asia/Jakarta
        }

        val createdAtLong = try {
            isoFormat.parse(this.created_at)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }

        return Announcement(
            id = this.id,
            title = this.title,
            message = this.message,
            created_by = this.created_by,
            created_at = createdAtLong
        )
    }
}