package com.example.myapplication.data.sources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import com.example.myapplication.data.sources.models.Schedule

@Entity(tableName = "schedules")
class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val created_by: Int,
    val company_id: Int = 1,
    val title: String,
    val description: String?, // Dibuat nullable (?) karena di DB tidak ada "NOT NULL"
    val start_time: Long = Date().time,
    val end_time: Long = Date().time,
    val location: String?, // Dibuat nullable (?) karena di DB tidak ada "NOT NULL"
    var created_at: Long = Date().time
) {
    fun toRawModel(): Schedule {
        return Schedule(
            id = id,
            created_by = created_by,
            company_id = company_id,
            title = title,
            description = description,
            start_time = start_time,
            end_time = end_time,
            location = location,
            created_at = created_at
        )
    }

    companion object {
        fun fromRawModel(schedule: Schedule): ScheduleEntity {
            return ScheduleEntity(
                id = schedule.id,
                created_by = schedule.created_by,
                company_id = schedule.company_id,
                title = schedule.title,
                description = schedule.description,
                start_time = schedule.start_time,
                end_time = schedule.end_time,
                location = schedule.location,
                created_at = schedule.created_at,
            )
        }
    }
}