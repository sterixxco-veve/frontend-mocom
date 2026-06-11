package com.example.myapplication.data.sources.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.time.LocalDateTime
import java.util.Date

@Parcelize
@Entity(tableName = "assignments")
class Assignment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val schedule_id: Int,
    val user_id: Int,
    val role_in_event: String = "",
    val job_desc: String = "",
    val status: String= "pending",
    val assigned_at: Long = Date().time,
) : Parcelable {
    companion object {
        fun create(
            id: Int = 0,
            schedule_id: Int,
            user_id: Int,
            role_in_event: String,
            job_desc: String,
            status: String,
            assigned_at: Long,
        ): Assignment {
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
    }

    fun copy(
        id: Int = this.id,
        schedule_id: Int = this.schedule_id,
        user_id: Int = this.user_id,
        role_in_event: String = this.role_in_event,
        job_desc: String = this.job_desc,
        status: String = this.status,
        assigned_at: Long = this.assigned_at,
    ): Assignment{
        return Assignment(
            id,
            schedule_id,
            user_id,
            role_in_event,
            job_desc,
            status,
            assigned_at
        )
    }
}