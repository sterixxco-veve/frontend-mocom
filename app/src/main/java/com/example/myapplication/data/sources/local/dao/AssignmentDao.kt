package com.example.myapplication.data.sources.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.example.myapplication.data.sources.local.entities.AssignmentEntity
import com.example.myapplication.data.sources.local.entities.AttendanceEntity
import com.example.myapplication.data.sources.local.entities.ScheduleEntity
import com.example.myapplication.data.sources.local.entities.UserEntity
import com.example.myapplication.data.sources.models.User

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments WHERE user_id = :id")
    suspend fun getAssignmentByUserId(id: Int): List<AssignmentEntity>

    @Query("SELECT * FROM assignments")
    suspend fun getAllAssignments(): List<AssignmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAssignments(assignments: List<AssignmentEntity>)
}