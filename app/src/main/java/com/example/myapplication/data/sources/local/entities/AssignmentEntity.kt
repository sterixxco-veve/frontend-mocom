package com.example.myapplication.data.sources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.sources.models.Assignment
import com.example.myapplication.data.sources.models.Attendance
import java.util.Date
import com.example.myapplication.data.sources.models.Schedule

@Entity(tableName = "assignments")
class AssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val schedule_id: Int,
    val user_id: Int,
    val role_in_event: String = "",
    val job_desc: String = "",
    val status: String= "pending",
    val assigned_at: Long = Date().time,
) {
    fun toRawModel(): Assignment {
        return Assignment(
            id = id,
            schedule_id = schedule_id,
            user_id = user_id,
            role_in_event = role_in_event,
            job_desc = job_desc,
            status = status,
            assigned_at = assigned_at
        )
    }

    companion object {
        fun fromRawModel(assignment: Assignment): AssignmentEntity {
            return AssignmentEntity(
                id = assignment.id,
                schedule_id = assignment.schedule_id,
                user_id = assignment.user_id,
                role_in_event = assignment.role_in_event,
                job_desc = assignment.job_desc,
                status = assignment.status,
                assigned_at = assignment.assigned_at
            )
        }
    }
}