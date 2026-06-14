package com.example.myapplication.data.sources.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.sources.local.dao.AssignmentDao
import com.example.myapplication.data.sources.local.dao.AttendanceDao
import com.example.myapplication.data.sources.local.dao.ScheduleDao
import com.example.myapplication.data.sources.local.dao.UserDao
import com.example.myapplication.data.sources.local.entities.AssignmentEntity
import com.example.myapplication.data.sources.local.entities.AttendanceEntity
import com.example.myapplication.data.sources.local.entities.ScheduleEntity
import com.example.myapplication.data.sources.local.entities.UserEntity

@Database(
    entities = [ScheduleEntity::class, UserEntity::class, AttendanceEntity::class, AssignmentEntity::class],
    version = 4,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao
    abstract fun userDao(): UserDao
    abstract fun attendanceDao(): AttendanceDao

    abstract fun assignmentDao(): AssignmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "proyek_mocom"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}