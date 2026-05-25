package com.example.myapplication

import java.io.Serializable
import java.time.LocalDateTime

data class AiRecommendation(

    val id: Int = 0,

    val schedule_id: Int = 0,

    val recommended_user_id: Int = 0,

    val score: Double = 0.0,

    val reason: String = "",

    val created_at: LocalDateTime

) : Serializable