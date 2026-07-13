package com.example.myapplication.data.sources.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.example.myapplication.data.sources.local.entities.AttendanceEntity
import com.example.myapplication.data.sources.local.entities.ScheduleEntity
import com.example.myapplication.data.sources.local.entities.UserEntity
import com.example.myapplication.data.sources.models.User

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendances")
    suspend fun getAllAttendance(): List<AttendanceEntity>

    @Query("SELECT * FROM attendances WHERE id = :id")
    suspend fun getAttendanceById(id: Int): AttendanceEntity?

    @Query("""
        SELECT attendances.* FROM attendances 
        INNER JOIN assignments ON attendances.assignment_id = assignments.id
        INNER JOIN schedules ON assignments.schedule_id = schedules.id
        WHERE schedules.company_id = :companyId
        ORDER BY attendances.check_in DESC
    """)
    suspend fun getAttendanceByCompanyId(companyId: Int): List<AttendanceEntity>

    @Query("DELETE FROM attendances WHERE id = :id")
    suspend fun deleteAttendanceById(id: Int)
    @Update
    suspend fun updateAttendance(attendanceEntity: AttendanceEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendanceEntity: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendances(attendances: List<AttendanceEntity>)
}