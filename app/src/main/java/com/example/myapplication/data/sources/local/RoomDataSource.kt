package com.example.myapplication.data.sources.local

import com.example.myapplication.data.sources.local.dao.AnnouncementDao
import com.example.myapplication.data.sources.local.database.AppDatabase
import com.example.myapplication.data.sources.local.entities.AnnouncementEntity
import com.example.myapplication.data.sources.local.entities.ScheduleEntity
import com.example.myapplication.data.sources.local.entities.UserEntity
import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.models.Assignment
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import java.text.SimpleDateFormat
import java.util.Locale

class RoomDataSource(private val database: AppDatabase) : LocalDataSource {
    private val scheduleDao = database.scheduleDao()
    private val userDao = database.userDao()

    private val assignmentDao = database.assignmentDao()
    private val attendanceDao = database.attendanceDao()
    private val AnnouncementDao = database.announcementDao()

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    //GET ALL
    override suspend fun getAllSchedule(): List<Schedule> {
        return scheduleDao.getAll().map { it.toRawModel() }
    }
    override suspend fun getAllUser(): List<User> {
        return userDao.getAllUser().map { it.toRawModel() }
    }

    override suspend fun getAssignmentByUserId(user_id: Int): List<Assignment> {
        return assignmentDao.getAssignmentByUserId(user_id).map { it.toRawModel() }
    }

    //GET BY COMPANY
    override suspend fun getScheduleByCompanyId(company_id: Int): List<Schedule> {
        return scheduleDao.getByCompanyId(company_id).map { it.toRawModel() }
    }

    override suspend fun getUserByCompanyId(company_id: Int): List<User> {
        return userDao.getUsersByCompanyId(company_id).map { it.toRawModel() }
    }

    override suspend fun getAttendanceByCompanyId(company_id: Int): List<Attendance> {
        return attendanceDao.getAttendanceByCompanyId(company_id).map { it.toRawModel() }
    }

//GET BY ID
    override suspend fun getById(id: Int): Schedule? {
        return scheduleDao.getById(id)?.toRawModel()
    }

    override suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)?.toRawModel()
    }

//UPDATE
override suspend fun updateScheduleLocal(schedule: Schedule) {
    try {
        // Konversi dari data model domain (Schedule) ke bentuk Room Entity (ScheduleEntity)
        val entity = ScheduleEntity(
            id = schedule.id,
            created_by = schedule.created_by,
            company_id = schedule.company_id,
            title = schedule.title,
            description = schedule.description,
            start_time = schedule.start_time, // Berupa Long milidetik
            end_time = schedule.end_time,     // Berupa Long milidetik
            location = schedule.location,
            created_at = schedule.created_at
        )

        // Eksekusi fungsi update bawaan Room lewat Dao
        scheduleDao.update(entity)
        android.util.Log.d("ROOM_LOCAL_DATA", "✅ Berhasil memperbarui Jadwal ID: ${schedule.id} di Room Lokal")
    } catch (e: Exception) {
        android.util.Log.e("ROOM_LOCAL_DATA", "❌ Gagal memperbarui jadwal di Room: ${e.message}")
        throw e
    }
}

    override suspend fun updateUserLocal(user: User) {
        try {
            // 1. Konversi dari model Domain (User) ke bentuk Room Entity (UserEntity)
            val entity = UserEntity(
                id = user.id,              // Pastikan ID user lama disertakan agar ditimpa di baris yang sama
                role_id = user.role_id,
                company_id = user.company_id,
                full_name = user.full_name,
                username = user.username,
                email = user.email,
                password = user.password,
                is_active = user.is_active,
                created_at = user.created_at, // Pertahankan waktu pembuatan awal
                updated_at = java.util.Date().time // Update waktu edit lokal ke milidetik sekarang
            )

            // 2. Eksekusi fungsi update menggunakan UserDao, bukan scheduleDao lagi!
            userDao.updateUser(entity)

            android.util.Log.d("ROOM_LOCAL_DATA", "✅ Berhasil memperbarui User ID: ${user.id} di Room Lokal")
        } catch (e: Exception) {
            android.util.Log.e("ROOM_LOCAL_DATA", "❌ Gagal memperbarui user di Room: ${e.message}")
            throw e
        }
    }

    // =========================================================================
    // 💡 IMPLEMENTASI HAPUS JADWAL LOKAL BY ID
    // =========================================================================
    override suspend fun deleteScheduleLocalById(id: Int) {
        try {
            // Panggil fungsi query custom yang sudah kita buat di ScheduleDao kemarin
            scheduleDao.deleteScheduleById(id)
            android.util.Log.d("ROOM_LOCAL_DATA", "🗑️ Berhasil menghapus Jadwal ID: $id dari Room Lokal")
        } catch (e: Exception) {
            android.util.Log.e("ROOM_LOCAL_DATA", "❌ Gagal menghapus jadwal di Room: ${e.message}")
            throw e
        }
    }

    override suspend fun deleteUserLocalById(id: Int) {
        try {
            userDao.deleteUserById(id)
            android.util.Log.d("ROOM_LOCAL_DATA", "🗑️ Berhasil menghapus User ID: $id dari Room Lokal")
        } catch (e: Exception) {
            android.util.Log.e("ROOM_LOCAL_DATA", "❌ Gagal menghapus jadwal di Room: ${e.message}")
            throw e
        }
    }


    override suspend fun getUnsynced(): List<Schedule> {
        return scheduleDao.getAll().filter { it.id == 0 }.map { it.toRawModel() }
    }


    //INSERT
    override suspend fun insert(
        createdBy: Int,
        title: String,
        company_id: Int,
        description: String?,
        location: String?,
        startTime: Long,
        endTime: Long
    ): Schedule {
        val entity = ScheduleEntity(
            id = 0,
            created_by = createdBy,
            company_id = company_id,
            title = title,
            description = description,
            start_time = startTime,
            end_time = endTime,
            location = location,
        )
        scheduleDao.insert(entity)
        return entity.toRawModel()
    }

    override suspend fun insertUser(
        role_id: Int,
        company_id: Int,
        full_name: String,
        username: String,
        email: String,
        password: String,
        is_active: Int
    ): User {
        val entity = UserEntity(
            id = 0, // Nilai 0 ini wajib agar memicu autoGenerate di Room SQLite
            role_id = role_id,
            company_id = company_id,
            full_name = full_name,
            username = username,
            email = email,
            password = password,
            is_active = is_active
        )
        val generatedId = userDao.insertUser(entity)
        return entity.toRawModel().copy(id = generatedId.toInt())
    }

    override suspend fun insertAnnouncement(
        title: String,
        message: String,
        createdBy: Int
    ): Announcement {
        val entity = AnnouncementEntity(
            id = 0,
            title = title,
            message = message,
            created_by = createdBy,
        )
        AnnouncementDao.insertAnnouncement(entity)
        return entity.toRawModel()
    }

    override suspend fun getAttendanceByUserId(
        user_id: Int
    ): List<Attendance> {
        throw UnsupportedOperationException(
            "Attendance local belum diimplementasikan"
        )
    }

    override suspend fun sync(schedules: List<Schedule>) {
        // Hapus data dummy lokal yang id-nya masih 0 karena akan digantikan data asli MySQL
        scheduleDao.getAll().filter { it.id == 0 }

        // Masukkan atau update data dari server
        schedules.forEach { remote ->
            val remoteId = remote.id
            val existingEntity = scheduleDao.getById(remoteId)

            val entityToSave = ScheduleEntity(
                id = remoteId,
                created_by = remote.created_by,
                company_id = remote.company_id,
                title = remote.title,
                description = remote.description,
                start_time = remote.start_time,
                end_time = remote.end_time,
                location = remote.location,
                created_at = remote.created_at
            )

            scheduleDao.insert(entityToSave)
        }
    }
}