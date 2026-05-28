package com.example.myapplication.data.sources.remote

import com.example.myapplication.data.sources.models.Schedule

interface RemoteDataSource {
    suspend fun insertSchedule(schedule: Schedule): Schedule
    suspend fun syncSchedule(schedule: List<Schedule>): List<Schedule>
}