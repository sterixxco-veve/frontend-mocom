package com.example.myapplication.data.sources.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.Date

@Parcelize
@Entity(tableName = "users")
class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 1,
    val username: String,
    val full_name: String,
    val email: String,
    val password: String,
    val is_active: Int,
    val role_id: Int,
    val company_id: Int,
    val created_at: Long = Date().time,
    val updated_at: Long = Date().time,
) : Parcelable {
    companion object {
        fun create(
            id: Int = 0,
            username: String,
            full_name: String,
            email: String,
            password: String,
            is_active: Int,
            role_id: Int,
            company_id: Int,
            created_at: Long,
            updated_at: Long
        ): User {
            return User(
                id = id,
                username = username,
                full_name = full_name,
                email = email,
                password = password,
                is_active = is_active,
                role_id = role_id,
                company_id = company_id,
                created_at = created_at,
                updated_at = updated_at
            )
        }
    }

    fun copy(
        id: Int = this.id,
        username: String = this.username,
        full_name: String = this.full_name,
        email: String = this.email,
        password: String = this.password,
        is_active: Int = this.is_active,
        role_id: Int = this.role_id,
        company_id: Int = this.company_id,
        created_at: Long = this.created_at,
        updated_at: Long = this.updated_at
    ): User{
        return User(
            id,
            username,
            full_name,
            email,
            password,
            is_active,
            role_id,
            company_id,
            created_at,
            updated_at
        )
    }
}