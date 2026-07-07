package com.example.myapplication.data.sources.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.time.LocalDateTime
import java.util.Date

@Parcelize
@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val message: String = "",
    val created_by: Int,
    var created_at: Long = Date().time
) : Parcelable{
    @Ignore
    var authorName: String = ""

    init {
        if (title.isBlank()) {
            throw IllegalArgumentException("Title tidak boleh kosong")
        }
    }

    companion object {
        // Fungsi create disesuaikan (menghapus content)
        fun create(
            id: Int = 0,
            title: String,
            message: String,
            created_by: Int,
            created_at: Long
        ): Announcement {
            return Announcement(
                id = id,
                title = title,
                message = message,
                created_by = created_by,
                created_at = created_at
            )
        }
    }
}