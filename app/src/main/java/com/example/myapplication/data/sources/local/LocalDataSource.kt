package com.example.myapplication.data.sources.local

import com.example.myapplication.data.sources.models.Schedule

interface LocalDataSource {

    suspend fun getAllSchedule(): List<Schedule>
    suspend fun getScheduleByCompanyId(company_id: Int): List<Schedule>

    suspend fun getById(id: Int): Schedule?

    suspend fun getUnsynced(): List<Schedule>

    // Parameter disesuaikan dengan kebutuhan pembuatan Schedule baru
    suspend fun insert(
        createdBy: Int,
        title: String,
        company_id: Int,
        description: String?,
        location: String?,
        startTime: Long,
        endTime: Long
    ): Schedule

    suspend fun sync(schedules: List<Schedule>)
}