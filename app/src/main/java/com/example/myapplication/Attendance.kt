package com.example.myapplication

import java.io.Serializable
import java.time.LocalDateTime

data class Attendance(
    val id: Int = 0,
    val assignment_id: Int,
    val check_in: LocalDateTime,
    val check_out: LocalDateTime,
    val status: String,
    val sync_status: String = "",
    val created_at: LocalDateTime
) : Serializable