package com.example.myapplication.data.sources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.sources.models.Attendance
import java.util.Date
import com.example.myapplication.data.sources.models.Schedule

@Entity(tableName = "attendances")
class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val assignment_id: Int,
    val check_in: Long = Date().time,
    val check_out: Long = Date().time,
    val status: String,
    val sync_status: String,
    val created_at: Long = Date().time,
) {
    fun toRawModel(): Attendance {
        return Attendance(
            id = id,
            assignment_id = assignment_id,
            check_in = check_in,
            check_out = check_out,
            status = status,
            sync_status = sync_status,
            created_at = created_at,
        )
    }

    companion object {
        fun fromRawModel(attendance: Attendance): AttendanceEntity {
            return AttendanceEntity(
                id = attendance.id,
                assignment_id = attendance.assignment_id,
                check_in = attendance.check_in,
                check_out = attendance.check_out,
                status = attendance.status,
                sync_status = attendance.sync_status,
            )
        }
    }
}