package com.example.myapplication.data.sources.remote

import com.example.myapplication.data.sources.remote.json.ScheduleJson
import com.example.myapplication.data.sources.remote.json.UserJson
import com.example.myapplication.data.sources.remote.request.ScheduleRequest
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

    //GET BY COMPANY ID//
    @GET("api/getSchedulesByCompanyId/{company_id}")
    suspend fun getSchedulesByCompanyId(
        @Path("company_id") companyId: Int
    ): List<ScheduleJson>
    @GET("api/getUsersByCompanyId/{company_id}")
    suspend fun getUsersByCompanyId(
        @Path("company_id") companyId: Int
    ): retrofit2.Response<List<UserJson>>


// UPDATE
    @PUT("api/updateSchedule/{id}")
    suspend fun updateSchedule(
        @Path("id") id: Int,
        @Body scheduleRequest: ScheduleRequest
    ): retrofit2.Response<Void>


    //INSERT
    @POST("api/insertSchedules")
    suspend fun insertSchedule(@Body request: ScheduleRequest): ScheduleJson

    //DELETE
    @DELETE("api/deleteSchedule/{id}")
    suspend fun deleteSchedule(
        @Path("id") id: Int
    ): retrofit2.Response<Unit>

    @POST("schedule/sync")
    suspend fun syncSchedule(@Body body: List<ScheduleJson>): List<ScheduleJson>



    @GET("api/getStaff")
    suspend fun getStaff(): List<UserJson>

    @GET("api/getMember")
    suspend fun getMember(): List<UserJson>
}