package com.example.myapplication

import com.example.myapplication.data.sources.models.AiRecommendation
import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.models.Assignment
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Company
import com.example.myapplication.data.sources.models.MySchedule
import com.example.myapplication.data.sources.models.Notification
import com.example.myapplication.data.sources.models.Replacement
import com.example.myapplication.data.sources.models.Resource
import com.example.myapplication.data.sources.models.Role
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.json.AssignmentJson
import com.example.myapplication.data.sources.remote.json.GeneralResponse
import com.example.myapplication.data.sources.remote.json.MyScheduleJson
import com.example.myapplication.data.sources.remote.json.ReplacementRequest
import com.example.myapplication.data.sources.remote.json.UserJson
import com.example.myapplication.data.sources.remote.request.CheckInRequest
import com.example.myapplication.data.sources.remote.request.NfcCheckInRequest
import com.example.myapplication.data.sources.remote.request.NfcRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    /* =========================
       AUTH & USERS
    ========================= */

    @POST("api/register")
    suspend fun register(
        @Body user: User
    ): Response<User>

    @POST("api/login")
    suspend fun login(
        @Body user: User
    ): Response<User>

    @POST("api/registerCompany")
    suspend fun registerCompany(
        @Body company: Company
    ): Response<Company>

    @POST("api/loginCompany")
    suspend fun loginCompany(
        @Body company: Company
    ): Response<Company>

    @GET("api/getCompanyDetail/{id}")
    suspend fun getCompanyDetail(
        @Path("id") id: Int
    ): Response<Company>

    @GET("api/getAllStaffCompany/{company_id}")
    suspend fun getAllStaffCompany(
        @Path("company_id") companyId: Int
    ): Response<List<User>>

    @GET("api/getUserProfile/{id}")
    suspend fun getUserProfile(
        @Path("id") id: Int
    ): Response<User>

    /* =========================
       ROLES
    ========================= */

    @POST("api/insertRoles")
    suspend fun insertRoles(
        @Body role: Role
    ): Response<Role>

    @GET("api/getAllRoles")
    suspend fun getAllRoles(): Response<List<Role>>

    /* =========================
       SCHEDULES
    ========================= */

    @POST("api/insertSchedules")
    suspend fun insertSchedules(
        @Body schedule: Schedule
    ): Response<Schedule>

    @GET("api/getAllSchedules")
    suspend fun getAllSchedules(): Response<List<Schedule>>

    /* =========================
       ASSIGNMENTS
    ========================= */

    @POST("api/insertAssignments")
    suspend fun insertAssignments(
        @Body assignment: Assignment
    ): Response<Assignment>



    @GET("api/getAssignmentsByUserId/{user_id}")
    suspend fun getAssignmentsByUserId(
        @Path("user_id") userId: Int
    ): Response<List<MyScheduleJson>>

    /* =========================
       ATTENDANCES
    ========================= */

    @POST("api/insertAttendance/checkin")
    suspend fun checkInAttendance(
        @Body attendance: Attendance
    ): Response<Attendance>

    @POST("api/updateAttendance/checkout")
    suspend fun checkOutAttendance(
        @Body attendance: Attendance
    ): Response<Attendance>

    /* =========================
       REPLACEMENTS
    ========================= */

    @POST("api/insertReplacements")
    suspend fun insertReplacements(
        @Body replacement: Replacement
    ): Response<Replacement>

    /* =========================
       AI RECOMMENDATIONS
    ========================= */

    @POST("api/insertAi-recommendations")
    suspend fun insertAiRecommendations(
        @Body aiRecommendation: AiRecommendation
    ): Response<AiRecommendation>

    @GET("api/getAi-recommendations/{schedule_id}")
    suspend fun getAiRecommendations(
        @Path("schedule_id") scheduleId: Int
    ): Response<List<AiRecommendation>>

    /* =========================
       NOTIFICATIONS
    ========================= */

    @GET("api/getNotificationsByUserId/{user_id}")
    suspend fun getNotificationsByUserId(
        @Path("user_id") userId: Int
    ): Response<List<Notification>>

    @PUT("api/updateNotifications/{id}/read")
    suspend fun updateNotificationRead(
        @Path("id") id: Int
    ): Response<Notification>

    /* =========================
       RESOURCES
    ========================= */

    @POST("api/insertResources")
    suspend fun insertResources(
        @Body resource: Resource
    ): Response<Resource>

    @GET("api/getResources/{schedule_id}")
    suspend fun getResources(
        @Path("schedule_id") scheduleId: Int
    ): Response<List<Resource>>

    /* =========================
       ANNOUNCEMENTS
    ========================= */

    @POST("api/insertAnnouncements")
    suspend fun insertAnnouncements(
        @Body announcement: Announcement
    ): Response<Announcement>

    @GET("api/getAllAnnouncements")
    suspend fun getAllAnnouncements(): Response<List<Announcement>>

//  staff
    @GET("api/getAttendancesByUserId/{user_id}")
    suspend fun getAttendancesByUserId(
        @Path("user_id") userId: Int
    ): List<Attendance>

    @POST("api/checkIn")
    suspend fun checkIn(
        @Body request: CheckInRequest
    ): Attendance

    @PUT("api/checkOut/{attendance_id}")
    suspend fun checkOut(
        @Path("attendance_id") attendanceId: Int
    ): Attendance

    @POST("api/assignNfc")
    suspend fun assignNfc(
        @Body request: NfcRequest
    ): Response<Map<String, Any>>

    @POST("api/checkInNfc")
    suspend fun checkInWithNfc(
        @Body request: NfcCheckInRequest
    ): retrofit2.Response<Map<String, Any>>

    // Ambil rekan kerja satu perusahaan untuk calon pengganti
    @GET("api/getUsersByCompanyId/{company_id}")
    suspend fun getUsersByCompanyId(@Path("company_id") companyId: Int): List<UserJson>

    // Kirim permohonan pelimpahan izin baru
    @POST("api/insertReplacements")
    suspend fun insertReplacements(@Body request: ReplacementRequest): Response<GeneralResponse>
}