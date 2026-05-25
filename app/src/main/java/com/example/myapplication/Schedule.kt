package com.example.myapplication

import java.io.Serializable
import java.time.LocalDateTime

data class Schedule(
    val id: Int = 0,
    val created_by: Int = 0,
    val title: String = "",
    val description: String = "",
    val start_time: LocalDateTime,
    val end_time: LocalDateTime,
    val location: String = "",
    val created_at: LocalDateTime
) : Serializable