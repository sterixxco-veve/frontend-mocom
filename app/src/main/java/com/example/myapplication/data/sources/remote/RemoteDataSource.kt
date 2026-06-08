package com.example.myapplication.data.sources.remote

import com.example.myapplication.data.sources.models.Schedule

interface RemoteDataSource {
    suspend fun fetchAllSchedules(): List<Schedule>
    suspend fun fetchScheduleByCompanyId(company_id: Int): List<Schedule>
    suspend fun insertSchedule(schedule: Schedule): Schedule
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun syncSchedule(schedule: List<Schedule>): List<Schedule>
}