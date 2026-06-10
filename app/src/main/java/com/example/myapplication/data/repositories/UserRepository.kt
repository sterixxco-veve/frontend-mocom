package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.models.User

interface UserRepository {
    suspend fun getAllUser(): List<User>
    suspend fun getUserById(id: Int): User?
    suspend fun getUserByCompanyId(company_id: Int): List<User>
    suspend fun insertUser(user: User): User
    suspend fun deleteUser(id: Int)
    suspend fun sync()
    suspend fun updateUser(user: User)
}