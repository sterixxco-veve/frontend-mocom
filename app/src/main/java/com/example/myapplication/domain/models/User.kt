package com.example.myapplication.domain.models

import java.io.Serializable
import java.util.Date

data class User(
    val id: Int = 0,
    val username: String = "",
    val fullname: String = "",
    val email: String = "",
    val password: String = "",
    val isActive: Int,
    val role_id: Int = 2,
    val created_at: Date,
    val updated_at: Date
) : Serializable