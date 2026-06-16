package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.local.entities.AttendanceEntity
import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.User

interface AnnouncementRepository {
    suspend fun getAnnouncements(): List<Announcement>
}