package com.example.myapplication.data.local

import com.example.myapplication.domain.models.Schedule

interface LocalDataSource {

    suspend fun getAll(): List<Schedule>

    suspend fun getById(id: Int): Schedule?

    suspend fun getUnsynced(): List<Schedule>

    // Parameter disesuaikan dengan kebutuhan pembuatan Schedule baru
    suspend fun insert(
        createdBy: Int,
        title: String,
        description: String?,
        location: String?,
        startTime: Long,
        endTime: Long
    ): Schedule

    suspend fun sync(schedules: List<Schedule>)
}