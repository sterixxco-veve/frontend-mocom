package com.example.myapplication.data.sources.remote

import android.util.Log
import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.models.Assignment
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.remote.json.ScheduleJson
import com.example.myapplication.data.sources.remote.json.UserJson
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.request.CheckInRequest
import com.example.myapplication.data.sources.remote.request.ScheduleRequest
import com.example.myapplication.data.sources.remote.request.UserRequest
import java.text.SimpleDateFormat
import java.util.*

// Untuk PARSE response dari server (ISO 8601)
private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

// Untuk SEND request ke server (MySQL format)
private val mysqlFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

// =========================================================================
// 🔀 MAPPER COMPONENT UNTUK SCHEDULE
// =========================================================================
fun ScheduleJson.toSchedule(): Schedule {
    return Schedule(
        id = this.id,
        created_by = this.created_by,
        company_id = this.company_id,
        title = this.title,
        description = this.description,
        start_time = mysqlFormat.parse(this.start_time)?.time ?: 0L,
        end_time = mysqlFormat.parse(this.end_time)?.time ?: 0L,
        location = this.location,
        created_at = this.created_at?.let { mysqlFormat.parse(it)?.time }
            ?: System.currentTimeMillis()
    )
}

fun Schedule.toScheduleRequest(): ScheduleRequest {
    return ScheduleRequest(
        created_by = this.created_by,
        company_id = this.company_id,
        title = this.title,
        description = this.description,
        start_time = mysqlFormat.format(Date(this.start_time)),
        end_time = mysqlFormat.format(Date(this.end_time)),
        location = this.location
    )
}

fun Schedule.toScheduleJsonForSync(): ScheduleJson {
    return ScheduleJson(
        id = this.id,
        created_by = this.created_by,
        company_id = this.company_id,
        title = this.title,
        description = this.description,
        start_time = mysqlFormat.format(Date(this.start_time)),
        end_time = mysqlFormat.format(Date(this.end_time)),
        location = this.location,
        created_at = mysqlFormat.format(Date(this.created_at))
    )
}

// =========================================================================
// 💡 MAPPER COMPONENT UNTUK USER (FIXED TYPO)
// =========================================================================
fun User.toUserRequest(): UserRequest {
    return UserRequest(
        role_id = this.role_id,
        company_id = this.company_id,
        full_name = this.full_name,
        username = this.username,
        email = this.email,
        password = this.password,
        is_active = this.is_active,
        created_at = mysqlFormat.format(Date(this.created_at)),
        updated_at = mysqlFormat.format(Date(this.updated_at))
    )
}

fun UserJson.toUser(): User {
    return User(
        id = this.id,
        company_id = this.company_id,
        role_id = this.role_id,
        full_name = this.full_name,
        username = this.username,
        email = this.email,
        password = this.password,
        is_active = this.is_active
    )
}

// =========================================================================
// 🚀 CLASS RETROFIT DATA SOURCE CORE
// =========================================================================
class RetrofitDataSource(private val webService: WebService) : RemoteDataSource {

    //GET ALL SCHEDULES
    override suspend fun fetchAllSchedules(): List<Schedule> {
        return try {
            val responseList = webService.getAllSchedules()
            responseList.map { it.toSchedule() }
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.message}")
            emptyList()
        }
    }

    //GET ALL USERS
    override suspend fun fetchAllUsers(): List<User> {
        return try {
            val responseList = webService.getAllUsers()
            responseList.map { it.toUser() }
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun fetchAssignmentByUserId(user_id: Int): List<Assignment> {
        return try {
            val responseList = webService.getAssignmentByUserId(user_id)
            responseList.map { it.toAssignment() }
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.message}")
            emptyList()
        }
    }

    //GET BY COMPANY ID
    override suspend fun fetchScheduleByCompanyId(company_id: Int): List<Schedule> {
        return try {
            val responseList = webService.getSchedulesByCompanyId(company_id)
            responseList.map { it.toSchedule() }
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun fetchUserByCompanyId(company_id: Int): List<User> {
        return try {
            val response = webService.getUsersByCompanyId(company_id)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toUser() }
            } else {
                Log.e("DEBUG_FETCH", "⚠️ Gagal! Server merespon dengan Code: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun fetchAttendanceByCompanyId(company_id: Int): List<Attendance> {
        return try {
            val response = webService.getAttendancesByCompanyId(company_id)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toAttendance() }
            } else {
                Log.e("DEBUG_FETCH", "⚠️ Gagal! Server merespon dengan Code: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "Error: ${e.message}")
            emptyList()
        }
    }
    // 💡 IMPLEMENTASI BARU: FETCH USER BY ID
    override suspend fun fetchUserById(id: Int): User? {
        return try {
            // Langsung dapatkan objek UserJson dari server
            val responseJson = webService.getUserById(id)

            // Langsung petakan ke model internal domain Android kamu
            responseJson.toUser()
        } catch (e: Exception) {
            Log.e("DEBUG_FETCH", "❌ Error saat fetchUserById: ${e.message}")
            null
        }
    }

    // INSERT SCHEDULE
    override suspend fun insertSchedule(schedule: Schedule): Schedule {
        try {
            val requestBody = schedule.toScheduleRequest()
            val responseJson = webService.insertSchedule(requestBody)
            return responseJson.toSchedule()
        } catch (e: Exception) {
            Log.e("RETROFIT_INSERT", "❌ Gagal insert schedule: ${e.message}")
            throw e
        }
    }

    // INSERT USER
    override suspend fun insertUser(user: User): User {
        try {
            val requestBody = user.toUserRequest()
            val responseJson = webService.insertUser(requestBody)
            return responseJson.toUser()
        } catch (e: Exception) {
            Log.e("RETROFIT_USER_INSERT", "❌ Gagal insert user: ${e.message}")
            throw e
        }
    }

    //UPDATE SCHEDULE
    override suspend fun updateSchedule(schedule: Schedule) {
        try {
            val requestBody = schedule.toScheduleRequest()
            val response = webService.updateSchedule(schedule.id, requestBody)
            if (!response.isSuccessful) {
                throw Exception("Gagal update jadwal di server dengan HTTP Code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("RETROFIT_UPDATE", "❌ Error koneksi saat updateSchedule: ${e.message}")
            throw e
        }
    }

    // 💡 IMPLEMENTASI BARU: UPDATE USER
    override suspend fun updateUser(user: User) {
        try {
            val requestBody = user.toUserRequest()
            val response = webService.updateUser(user.id, requestBody)
            if (!response.isSuccessful) {
                throw Exception("Gagal update user di server. Code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("RETROFIT_USER_UPDATE", "❌ Gagal update user: ${e.message}")
            throw e
        }
    }

    //DELETE
    override suspend fun deleteSchedule(id: Int) {
        try {
            val response = webService.deleteSchedule(id)
            if (!response.isSuccessful) {
                throw Exception("Gagal menghapus jadwal di server. Code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("RETROFIT_DELETE", "❌ Error saat deleteSchedule: ${e.message}")
            throw e
        }
    }

    override suspend fun deleteUser(id: Int) {
        try {
            val response = webService.deleteUser(id)
            if (!response.isSuccessful) {
                throw Exception("Gagal menghapus user di server. Code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("RETROFIT_DELETE", "❌ Error saat delete user: ${e.message}")
            throw e
        }
    }

    //SYNC SCHEDULE
    override suspend fun syncSchedule(schedule: List<Schedule>): List<Schedule> {
        try {
            val requestBody = schedule.map { it.toScheduleJsonForSync() }
            val responseList = webService.syncSchedule(requestBody)
            return responseList.map { it.toSchedule() }
        } catch (e: Exception) {
            e.printStackTrace()
            return schedule
        }
    }

    override suspend fun fetchAttendanceByUserId(
        user_id: Int
    ): List<Attendance> {
        return try {
            webService.getAttendancesByUserId(user_id)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun checkIn(
        assignmentId: Int
    ): Attendance {

        return webService.checkIn(
            CheckInRequest(
                assignment_id = assignmentId
            )
        )
    }

    override suspend fun checkOut(
        attendanceId: Int
    ): Boolean {

        val response = webService.checkOut(attendanceId)
        return response != null
    }

    override suspend fun fetchAnnouncements(): List<Announcement> {
        return webService.getAnnouncements()
    }

}