package com.example.myapplication.data.sources.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "schedules")
class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val created_by: Int,
    val title: String,
    val description: String?, // Dibuat nullable (?) karena di DB tidak ada "NOT NULL"
    val start_time: Long = Date().time,
    val end_time: Long = Date().time,
    val location: String?, // Dibuat nullable (?) karena di DB tidak ada "NOT NULL"
    var created_at: Long = Date().time

    // Variabel 'content' DIHAPUS karena tidak ada di tabel database kamu
) : Parcelable {

    init {
        // Validasi disesuaikan dengan kolom database
        if (title.isBlank()) {
            throw IllegalArgumentException("Title tidak boleh kosong")
        }
    }

    companion object {
        // Fungsi create disesuaikan (menghapus content)
        fun create(
            id: Int = 0,
            created_by: Int,
            title: String,
            description: String?,
            location: String?,
            start_time: Long,
            end_time: Long
        ): Schedule {
            return Schedule(
                id = id,
                created_by = created_by,
                title = title,
                description = description,
                location = location,
                start_time = start_time,
                end_time = end_time
            )
        }
    }

    fun copy(
        id: Int = this.id,
        created_by: Int = this.created_by,
        title: String = this.title,
        description: String? = this.description,
        start_time: Long = this.start_time,
        end_time: Long = this.end_time,
        location: String? = this.location,
        created_at: Long = this.created_at
    ): Schedule{
        return Schedule(
            id,
            created_by,
            title,
            description,
            start_time,
            end_time,
            location,
            created_at
        )
    }
}