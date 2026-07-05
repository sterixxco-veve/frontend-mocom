package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.local.entities.AttendanceEntity
import com.example.myapplication.data.sources.models.Assignment
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.MySchedule
import com.example.myapplication.data.sources.models.User

interface AssignmentRepository {
    suspend fun getMySchedule(userId: Int): List<MySchedule>
    suspend fun getAssignmentByUserId(user_id: Int): List<MySchedule>
//    suspend fun sync()
}