package com.example.myapplication.data.sources.local

import com.example.myapplication.data.sources.local.database.AppDatabase
import com.example.myapplication.data.sources.local.entities.ScheduleEntity
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import java.text.SimpleDateFormat
import java.util.Locale

class RoomDataSource(private val database: AppDatabase) : LocalDataSource {
    private val scheduleDao = database.scheduleDao()
    private val userDao = database.userDao()
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    //GET ALL
    override suspend fun getAllSchedule(): List<Schedule> {
        return scheduleDao.getAll().map { it.toRawModel() }
    }
    override suspend fun getAllUser(): List<User> {
        return userDao.getAllUser().map { it.toRawModel() }
    }

    //GET BY COMPANY
    override suspend fun getScheduleByCompanyId(company_id: Int): List<Schedule> {
        return scheduleDao.getByCompanyId(company_id).map { it.toRawModel() }
    }

    override suspend fun getUserByCompanyId(company_id: Int): List<User> {
        return userDao.getUsersByCompanyId(company_id).map { it.toRawModel() }
    }

//GET BY ID
    override suspend fun getById(id: Int): Schedule? {
        return scheduleDao.getById(id)?.toRawModel()
    }

    override suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)?.toRawModel()
    }




    override suspend fun getUnsynced(): List<Schedule> {
        return scheduleDao.getAll().filter { it.id == 0 }.map { it.toRawModel() }
    }

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
            id = 0, // 0 memicu auto-increment lokal Room
            created_by = createdBy,
            company_id = company_id,
            title = title,
            description = description,
            start_time = startTime,
            end_time = endTime,
            location = location,
//            created_at = sdf.format(Date())
        )
        scheduleDao.insert(entity)
        return entity.toRawModel()
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