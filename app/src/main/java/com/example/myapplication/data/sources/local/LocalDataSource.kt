package com.example.myapplication.data.sources.local

import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User

interface LocalDataSource {
//GET ALL
    suspend fun getAllSchedule(): List<Schedule>
    suspend fun getAllUser(): List<User>

    //GET BY COMPANY ID
    suspend fun getScheduleByCompanyId(company_id: Int): List<Schedule>
    suspend fun getUserByCompanyId(company_id: Int): List<User>

    //GET BY ID
    suspend fun getById(id: Int): Schedule?
    suspend fun getUserById(id: Int): User?

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