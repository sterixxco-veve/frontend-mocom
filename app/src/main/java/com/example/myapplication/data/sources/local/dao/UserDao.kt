package com.example.myapplication.data.sources.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.sources.local.entities.ScheduleEntity
import com.example.myapplication.data.sources.local.entities.UserEntity
import com.example.myapplication.data.sources.models.User

@Dao
interface UserDao{

    @Query("SELECT * FROM users")
    suspend fun getAllUser(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE company_id = :id")
    suspend fun getUsersByCompanyId(id: Int): List<UserEntity>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)
    @Update
    suspend fun updateUser(user: UserEntity)
    @Insert
    suspend fun insertUser(user: UserEntity): Long
}