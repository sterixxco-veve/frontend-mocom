package com.example.myapplication.data.sources.models

import java.io.Serializable
import java.util.Date

data class Company(
    val id: Int = 0,
    val company_name: String = "",
    val email: String = "",
    val password: String = "",
    val phone_number: String = "",
    val address: String = "",
    val isActive: Int,
    val created_at: Date,
    val updated_at: Date
) : Serializable