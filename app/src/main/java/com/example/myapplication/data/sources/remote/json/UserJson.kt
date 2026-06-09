package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.User

data class UserJson(
    val id: Int,
    val role_id: Int,
    val full_name: String,
    val username: String,
    val email: String,
    val password: String,
    val is_active: Int,
    val company_id: Int,
    val updated_at: String,
    val created_at: String,
) {
    fun toUser(): User {
        return User(
            id = this.id,
            role_id = this.role_id,
            full_name = this.full_name,
            username = this.username,
            email = this.email,
            password = "",
            company_id = this.company_id,
            is_active = this.is_active,
        )
    }
}