package com.example.myapplication.data.sources.models

import java.io.Serializable
import java.time.LocalDateTime

data class Notification(
    val id: Int = 0,
    val user_id: Int,
    val title: String = "",
    val message: String = "",
    val type: String,
    val is_read: Int,
    val created_at: LocalDateTime
) : Serializable