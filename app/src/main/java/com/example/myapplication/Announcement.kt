package com.example.myapplication

import java.io.Serializable
import java.time.LocalDateTime

data class Announcement(
    val id: Int = 0,
    val title: String = "",
    val message: String = "",
    val created_by: Int,
    val created_at: LocalDateTime
) : Serializable