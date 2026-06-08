package com.example.myapplication.data.sources.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class User(
    val id: Int? = null,
    val username: String? = null,

    // PERBAIKAN: Petakan "full_name" dari database ke "fullname" di Kotlin
    @SerializedName("full_name")
    val fullname: String? = null,

    val email: String? = null,
    val password: String? = null,

    @SerializedName("is_active")
    val isActive: Int? = null,

    val role_id: Int? = null,
    val company_id: Int? = null,
    val created_at: Date? = null,
    val updated_at: Date? = null
) : Serializable