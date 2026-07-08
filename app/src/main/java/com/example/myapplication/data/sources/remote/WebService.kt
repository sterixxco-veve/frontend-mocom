package com.example.myapplication.data.sources.remote

import com.example.myapplication.data.sources.local.entities.AttendanceEntity
import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.remote.json.AnnouncementJson
import com.example.myapplication.data.sources.remote.json.AssignmentJson
import com.example.myapplication.data.sources.remote.json.AttendanceJson
import com.example.myapplication.data.sources.remote.json.GeneralResponse
import com.example.myapplication.data.sources.remote.json.MyScheduleJson
import com.example.myapplication.data.sources.remote.json.NotificationReplacementJson
import com.example.myapplication.data.sources.remote.json.ReplacementDetailJson
import com.example.myapplication.data.sources.remote.json.ReplacementListJson
import com.example.myapplication.data.sources.remote.json.ScheduleJson
import com.example.myapplication.data.sources.remote.json.UserJson
import com.example.myapplication.data.sources.remote.request.AnnouncementRequest
import com.example.myapplication.data.sources.remote.request.ApproveReplacementRequest
import com.example.myapplication.data.sources.remote.request.ChangePasswordRequest
import com.example.myapplication.data.sources.remote.request.CheckInRequest
import com.example.myapplication.data.sources.remote.request.ScheduleRequest
import com.example.myapplication.data.sources.remote.request.UserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface WebService {
    //GET ALL
    @GET("api/getAllSchedules")
    suspend fun getAllSchedules(): List<ScheduleJson>
    @GET("api/getAllUsers")
    suspend fun getAllUsers(): List<UserJson>
    @GET("api/getAllAnnouncements")
    suspend fun getAllAnnouncements(): List<AnnouncementJson>

    @GET("api/getAssignmentByUserId/{user_id}")
    suspend fun getAssignmentByUserId(
        @Path("user_id") userId: Int
    ): List<AssignmentJson>

    //GET BY COMPANY ID//
    @GET("api/getSchedulesByCompanyId/{company_id}")
    suspend fun getSchedulesByCompanyId(
        @Path("company_id") companyId: Int
    ): List<ScheduleJson>
    @GET("api/getUsersByCompanyId/{company_id}")
    suspend fun getUsersByCompanyId(
        @Path("company_id") companyId: Int
    ): retrofit2.Response<List<UserJson>>
    @GET("api/getAttendancesByCompanyId/{company_id}")
    suspend fun getAttendancesByCompanyId(
        @Path("company_id") companyId: Int
    ): retrofit2.Response<List<AttendanceJson>>

    //GET BY ID
    @GET("api/getScheduleById/{id}")
    suspend fun getScheduleById(
        @Path("id") id: Int
    ): ScheduleJson
    @GET("api/getUserById/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): UserJson

// UPDATE
    @PUT("api/updateSchedule/{id}")
    suspend fun updateSchedule(
        @Path("id") id: Int,
        @Body scheduleRequest: ScheduleRequest
    ): retrofit2.Response<Void>
    @PUT("api/updateUser/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body userRequest: UserRequest
    ): retrofit2.Response<Void>


    //INSERT
    @POST("api/insertSchedules")
    suspend fun insertSchedule(@Body request: ScheduleRequest): ScheduleJson
    @POST("api/insertUser")
    suspend fun insertUser(@Body request: UserRequest): UserJson
    @POST("api/insertAnnouncement")
    suspend fun insertAnnouncement(@Body request: AnnouncementRequest): AnnouncementJson

    //DELETE
    @DELETE("api/deleteSchedule/{id}")
    suspend fun deleteSchedule(
        @Path("id") id: Int
    ): retrofit2.Response<Unit>
    @DELETE("api/deleteUser/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int
    ): retrofit2.Response<Unit>

    @POST("schedule/sync")
    suspend fun syncSchedule(@Body body: List<ScheduleJson>): List<ScheduleJson>



    @GET("api/getStaff")
    suspend fun getStaff(): List<UserJson>

    @GET("api/getMember")
    suspend fun getMember(): List<UserJson>

    @GET("api/getAttendancesByUserId/{user_id}")
    suspend fun getAttendancesByUserId(
        @Path("user_id") userId: Int
    ): List<AttendanceJson>

    @POST("api/checkIn")
    suspend fun checkIn(
        @Body request: CheckInRequest
    ): AttendanceJson // 🟢 BENAR: Mengembalikan objek json hasil record server

    @PUT("api/checkOut/{attendance_id}")
    suspend fun checkOut(
        @Path("attendance_id") attendanceId: Int
    ): AttendanceJson // 🟢 BENAR: Mengembalikan objek json hasil record server

    @GET("api/getAnnouncements")
    suspend fun getAnnouncements(): List<Announcement>

    @GET("api/getAssignmentsByUserId/{user_id}")
    suspend fun getMySchedule(
        @Path("user_id") userId: Int
    ): List<MyScheduleJson>

    @PUT("api/users/{id}/password")
    suspend fun updatePassword(
        @Path("id") id: Int,
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    @GET("api/getReplacementRequests/{company_id}")
    suspend fun getReplacementRequests(
        @Path("company_id") companyId:Int
    ): List<ReplacementListJson>

    @GET("api/replacements/{id}")
    suspend fun getReplacementDetail(
        @Path("id") id:Int
    ): ReplacementDetailJson

    @PUT("api/replacements/{id}/approve")
    suspend fun approveReplacement(
        @Path("id") id:Int,
        @Body body: ApproveReplacementRequest
    ): Response<GeneralResponse>

    @PUT("api/replacements/{id}/reject")
    suspend fun rejectReplacement(
        @Path("id") id:Int
    ): Response<GeneralResponse>

    @GET("api/replacements/user/{user_id}")
    suspend fun getReplacementNotifications(

        @Path("user_id")
        userId:Int

    ):List<NotificationReplacementJson>

}