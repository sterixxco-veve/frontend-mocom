package com.example.myapplication.data.sources.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.Date

@Parcelize
@Entity(tableName = "attendances")
class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val assignment_id: Int,
    val check_in: String? = "",
    val check_out: String? = "",
    val status: String,
    val sync_status: String,
    val created_at: String? = "",
) : Parcelable {
    companion object {
        fun create(
            id: Int = 0,
            assignment_id: Int,
            check_in: String,
            check_out: String,
            status: String,
            sync_status: String,
            created_at: String,
        ): Attendance {
            return Attendance(
                id = id,
                assignment_id = assignment_id,
                check_in = check_in,
                check_out = check_out,
                status = status,
                sync_status = sync_status,
                created_at = created_at
            )
        }
    }

    fun copy(
        id: Int = this.id,
        assignment_id: Int = this.assignment_id,
        check_in: String? = this.check_in,
        check_out: String? = this.check_out,
        status: String = this.status,
        sync_status: String = this.sync_status,
        created_at: String? = this.created_at,
    ): Attendance{
        return Attendance(
            id,
            assignment_id,
            check_in,
            check_out,
            status,
            sync_status,
            created_at
        )
    }
}