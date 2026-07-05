package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.User
import com.google.gson.annotations.SerializedName

data class UserJson(
    @SerializedName("id") val id: Int,
    @SerializedName("role_id") val role_id: Int,
    @SerializedName("company_id") val company_id: Int,
    @SerializedName("full_name") val full_name: String, // 🟢 Wajib pakai SerializedName snake_case sesuai MySQL
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("is_active") val is_active: Int,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?,
    @SerializedName("deleted_at") val deleted_at: String?,

) {
    fun toUser(): User {
        return User(
            id = this.id,
            role_id = this.role_id,
            full_name = this.full_name,
            username = this.username,
            email = this.email,
            password = this.password ?: "",
            company_id = this.company_id ?: 0,
            is_active = this.is_active ?: 1,
        )
    }
}