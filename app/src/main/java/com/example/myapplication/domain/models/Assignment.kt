package com.example.myapplication.domain.models

import java.io.Serializable
import java.time.LocalDateTime

data class Assignment(
    val id: Int = 0,
    val schedule_id: String = "",
    val user_id: Int,
    val role_in_event: String = "",
    val job_desc: String = "",
    val status: String= "",
    val assigned_at: LocalDateTime
) : Serializable