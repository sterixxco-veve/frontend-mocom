package com.example.myapplication.data.repositories

import com.example.myapplication.domain.models.Schedule

interface ScheduleRepository {
    suspend fun getAll(): List<Schedule>
    suspend fun getById(id: Int): Schedule?
    suspend fun insert(schedule: Schedule): Schedule
    suspend fun sync()
}