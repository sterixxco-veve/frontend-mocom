package com.example.myapplication

import java.io.Serializable
import java.time.LocalDateTime
import java.util.Date

data class Replacement(
    val id: Int = 0,
    val assignment_id: Int,
    val requested_by: Int,
    val replacement_user_id: Int,
    val reason: String = "",
    val status: String = "",
    val approved_by: Int,
    val created_at: LocalDateTime
) : Serializable