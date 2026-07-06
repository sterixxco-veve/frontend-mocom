package com.example.myapplication.data.sources.remote.request

import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.models.Schedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class AnnouncementRequest(
    val title: String,
    val message: String,
    val created_by: Int,
) {
    companion object {
        fun fromModel(announcement: Announcement): AnnouncementRequest {
            return AnnouncementRequest(
                title = announcement.title,
                message = announcement.message,
                created_by = announcement.created_by,
            )
        }
    }
}