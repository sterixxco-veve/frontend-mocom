package com.example.myapplication.data.sources.remote

import com.example.myapplication.data.sources.local.entities.AttendanceEntity
import com.example.myapplication.data.sources.remote.json.AssignmentJson
import com.example.myapplication.data.sources.remote.json.AttendanceJson
import com.example.myapplication.data.sources.remote.json.ScheduleJson
import com.example.myapplication.data.sources.remote.json.UserJson
import com.example.myapplication.data.sources.remote.request.ScheduleRequest
import com.example.myapplication.data.sources.remote.request.UserRequest
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
}