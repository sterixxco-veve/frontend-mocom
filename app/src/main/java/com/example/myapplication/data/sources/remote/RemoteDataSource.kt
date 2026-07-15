package com.example.myapplication.data.sources.remote

import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.models.Assignment
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.MySchedule
import com.example.myapplication.data.sources.models.NotificationReplacement
import com.example.myapplication.data.sources.models.ReplacementDetail
import com.example.myapplication.data.sources.models.ReplacementItem
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.request.CheckInRequest

interface RemoteDataSource {
    //FETCH ALL
    suspend fun fetchAllSchedules(): List<Schedule>
    suspend fun fetchAllUsers(): List<User>
    suspend fun fetchAllAnnouncement(): List<Announcement>
    suspend fun fetchAssignmentByUserId(user_id: Int): List<Assignment>

    //FETCH BY COMPANY ID
    suspend fun fetchScheduleByCompanyId(company_id: Int): List<Schedule>
    suspend fun fetchUserByCompanyId(company_id: Int): List<User>
    suspend fun fetchAttendanceByCompanyId(company_id: Int): List<Attendance>

    suspend fun fetchAttendanceByUserId(user_id: Int): List<Attendance>

    //FETCH BY ID
    suspend fun fetchUserById(id: Int): User?

    //INSERT
    suspend fun insertSchedule(schedule: Schedule): Schedule
    suspend fun insertUser(user: User): User
    suspend fun insertAnnouncement(announcement: Announcement): Announcement

    //UPDATE
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun updateUser(user: User)

    //DELETE
    suspend fun deleteSchedule(id: Int)
    suspend fun deleteUser(id: Int)

    //
    suspend fun syncSchedule(schedule: List<Schedule>): List<Schedule>

    suspend fun checkIn(request: CheckInRequest): Attendance

    suspend fun checkOut(attendanceId: Int): Attendance

    suspend fun fetchAnnouncements(): List<Announcement>

    suspend fun getMySchedule(
        userId: Int
    ): List<MySchedule>

    suspend fun updatePassword(
        id: Int,
        password: String
    )

    suspend fun fetchReplacementRequests(
        companyId:Int
    ): List<ReplacementItem>

    suspend fun fetchReplacementDetail(
        id:Int
    ): ReplacementDetail

    suspend fun approveReplacement(
        replacementId:Int,
        approvedBy:Int
    )

    suspend fun rejectReplacement(
        replacementId:Int
    )

    suspend fun fetchReplacementNotifications(

        userId:Int

    ):List<NotificationReplacement>

    suspend fun updateAssignmentStatus(
        assignmentId: Int,
        body: Map<String, String>
    ): retrofit2.Response<Map<String, Any>>
}