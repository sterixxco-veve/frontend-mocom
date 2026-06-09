package com.example.myapplication.data.sources.remote

import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User

interface RemoteDataSource {
    suspend fun fetchAllSchedules(): List<Schedule>
    suspend fun fetchAllUsers(): List<User>
    suspend fun fetchScheduleByCompanyId(company_id: Int): List<Schedule>
    suspend fun fetchUserByCompanyId(company_id: Int): List<User>
    suspend fun insertSchedule(schedule: Schedule): Schedule
    suspend fun insertUser(user: User): User
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun updateUser(user: User)
    suspend fun syncSchedule(schedule: List<Schedule>): List<Schedule>
    suspend fun fetchUserById(id: Int): User?
}