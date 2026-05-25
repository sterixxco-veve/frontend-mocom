package com.example.myapplication

import java.io.Serializable
import java.time.LocalDateTime

data class Role(
    val id: Int = 0,
    val role_name: String = "",
    val created_at: LocalDateTime
) : Serializable