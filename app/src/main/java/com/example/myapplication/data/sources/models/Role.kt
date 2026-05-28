package com.example.myapplication.data.sources.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.util.Date

@Parcelize
@Entity(tableName = "roles")
class Role(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val role_name: String = "",
    val created_at: LocalDateTime

) : Parcelable