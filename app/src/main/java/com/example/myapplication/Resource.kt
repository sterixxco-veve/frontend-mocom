package com.example.myapplication

import java.io.Serializable
import java.time.LocalDateTime

data class Resource(
    val id: Int = 0,
    val schedule_id: Int,
    val title: String = "",
    val content: String = "",
    val file_url: String = "",
    val created_at: LocalDateTime
) : Serializable