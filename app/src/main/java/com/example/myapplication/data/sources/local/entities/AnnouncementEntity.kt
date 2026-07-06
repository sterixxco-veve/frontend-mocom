package com.example.myapplication.data.sources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.sources.models.Announcement
import java.util.Date
import com.example.myapplication.data.sources.models.Schedule

@Entity(tableName = "announcements")
class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String,
    val created_by: Int,
    var created_at: Long = Date().time
) {
    fun toRawModel(): Announcement {
        return Announcement(
            id = id,
            title = title,
            message = message,
            created_by = created_by,
            created_at = created_at
        )
    }

    companion object {
        fun fromRawModel(announcement: Announcement): AnnouncementEntity {
            return AnnouncementEntity(
                id = announcement.id,
                title = announcement.title,
                message = announcement.message,
                created_by = announcement.created_by,
                created_at = announcement.created_at,
            )
        }
    }
}