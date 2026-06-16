package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.local.entities.AttendanceEntity
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.User

interface AttendanceRepository {
//    suspend fun getAllAttendance(): List<Attendance>
//    suspend fun getAttendanceById(id: Int): Attendance?
    suspend fun getAttendanceByCompanyId(company_id: Int): List<Attendance>
//    suspend fun checkIn(assignmentId: Int, checkInTime: String, status: String): Attendance
    suspend fun checkOut(
        attendanceId: Int
    ): Boolean
    suspend fun checkIn(
        assignmentId: Int
    ): Attendance
//    suspend fun checkOut(attendanceId: Int, checkOutTime: String): Boolean
    suspend fun getAttendanceByUserId(user_id: Int): List<Attendance>
//    suspend fun deleteAttendance(id: Int)
//    suspend fun sync()
//    suspend fun updateAttendance(attendance: Attendance)
}