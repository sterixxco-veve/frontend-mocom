package com.example.myapplication.data.sources.remote.json

data class UserJson(
    val id: Int,
    val role_id: Int,
    val full_name: String,
    val username: String,
    val email: String? = null,
    val password: String,
    val is_active: Int,
    val updatedAt: String? = null,
    val created_by: String? = null,
) {
    override fun toString(): String {
        return full_name
    }
}