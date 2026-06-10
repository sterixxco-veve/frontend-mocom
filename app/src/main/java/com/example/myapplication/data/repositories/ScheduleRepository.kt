package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.models.Schedule

interface ScheduleRepository {
    suspend fun getAll(): List<Schedule>
    suspend fun getByCompanyId(id: Int): List<Schedule>
    suspend fun getById(id: Int): Schedule?
    suspend fun insert(schedule: Schedule): Schedule
    suspend fun delete(id: Int)
    suspend fun sync()
    suspend fun update(schedule: Schedule)
}