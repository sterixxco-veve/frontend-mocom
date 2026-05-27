package com.example.myapplication.data.remote

import com.example.myapplication.domain.models.Schedule

interface RemoteDataSource {
    suspend fun insertSchedule(schedule: Schedule): Schedule
    suspend fun syncSchedule(schedule: List<Schedule>): List<Schedule>
}