package com.example.myapplication.data.sources.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.sources.local.entities.ScheduleEntity

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules")
    suspend fun getAll(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: Int): ScheduleEntity?

    @Query("SELECT * FROM schedules WHERE company_id = :id")
    suspend fun getByCompanyId(id: Int): List<ScheduleEntity>
    @Insert
    suspend fun insert(schedule: ScheduleEntity)
    @Query("DELETE FROM schedules WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: Int)
    @Insert
    suspend fun bulkInsert(schedules: List<ScheduleEntity>)
    @Update
    suspend fun update(schedule: ScheduleEntity)
    @Query("SELECT * FROM schedules")
    suspend fun getUnsynced(): List<ScheduleEntity>

}