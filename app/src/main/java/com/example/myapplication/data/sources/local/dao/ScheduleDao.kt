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

    @Insert
    suspend fun insert(schedule: ScheduleEntity)

    @Insert
    suspend fun bulkInsert(schedules: List<ScheduleEntity>)

    @Query("SELECT * FROM schedules")
    suspend fun getUnsynced(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE created_by = :userId ORDER BY start_time ASC")
    suspend fun getByUserId(userId: Int): List<ScheduleEntity>
}