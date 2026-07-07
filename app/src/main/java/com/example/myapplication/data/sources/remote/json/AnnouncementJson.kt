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
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

        val createdAtLong = try {
            if (this.created_at != null) sdf.parse(this.created_at)?.time ?: 0L else 0L
        } catch (e: Exception) { 0L }

        return Announcement(
            id = this.id,
            title = this.title,
            message = this.message,
            created_by = this.created_by,
            created_at = createdAtLong
        )
    }
}