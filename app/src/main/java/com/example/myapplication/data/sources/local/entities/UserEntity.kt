package com.example.myapplication.data.sources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User

@Entity(tableName = "users")
class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val role_id: Int,
    val company_id: Int,
    val full_name: String,
    val username: String,
    val email: String,
    val password: String,
    val is_active: Int,
    var created_at: Long = Date().time,
    var updated_at: Long = Date().time
) {
    fun toRawModel(): User {
        return User(
            id = id,
            role_id = role_id,
            company_id = company_id,
            full_name = full_name,
            username = username,
            email = email,
            password = password,
            is_active = is_active,
        )
    }

    companion object {
        fun fromRawModel(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                role_id = user.role_id,
                company_id = user.company_id,
                full_name = user.full_name,
                username = user.username,
                email = user.email,
                password = user.password,
                is_active = user.is_active
            )
        }
    }
}