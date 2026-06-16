package com.example.myapplication.data.sources.local

import com.example.myapplication.data.sources.models.Assignment
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User

interface LocalDataSource {
//GET ALL
    suspend fun getAllSchedule(): List<Schedule>
    suspend fun getAllUser(): List<User>

    suspend fun getAssignmentByUserId(user_id: Int): List<Assignment>

    //GET BY COMPANY ID
    suspend fun getScheduleByCompanyId(company_id: Int): List<Schedule>
    suspend fun getUserByCompanyId(company_id: Int): List<User>
    suspend fun getAttendanceByCompanyId(company_id: Int): List<Attendance>

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

    suspend fun insertUser(
        role_id: Int,
        company_id: Int,
        full_name: String,
        username: String,
        email: String,
        password: String,
        is_active: Int
    ): User

    suspend fun deleteScheduleLocalById(id: Int)
    suspend fun deleteUserLocalById(id: Int)

    suspend fun updateScheduleLocal(schedule: Schedule)
    suspend fun updateUserLocal(user: User)

    suspend fun sync(schedules: List<Schedule>)

    suspend fun getAttendanceByUserId(user_id: Int): List<Attendance>
}