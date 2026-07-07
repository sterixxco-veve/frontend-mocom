package com.example.myapplication.data.sources.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.sources.local.entities.AnnouncementEntity
import com.example.myapplication.data.sources.local.entities.ScheduleEntity
import com.example.myapplication.data.sources.local.entities.UserEntity
import com.example.myapplication.data.sources.models.User

@Dao
interface AnnouncementDao{

    @Query("SELECT * FROM announcements")
    suspend fun getAllAnnouncement(): List<AnnouncementEntity>

    @Query("SELECT * FROM announcements WHERE id = :id")
    suspend fun getAnnouncementById(id: Int): AnnouncementEntity?

//    @Update
//    suspend fun updateUser(user: UserEntity)
    @Insert
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)
}