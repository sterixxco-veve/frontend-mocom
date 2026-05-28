package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.local.LocalDataSource
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.remote.RemoteDataSource

class DefaultScheduleRepository(
    val localDataSource: LocalDataSource,
    val remoteDataSource: RemoteDataSource
) : ScheduleRepository {

    override suspend fun getAll(): List<Schedule> {
        return try {
            val remoteData = remoteDataSource.fetchAllSchedules()
            remoteData
        } catch (e: Exception) {
            e.printStackTrace()
            localDataSource.getAllSchedule()
        }
    }

    override suspend fun getById(id: Int): Schedule? {
        return localDataSource.getById(id)
    }

    override suspend fun insert(schedule: Schedule): Schedule {
        val localSchedule = localDataSource.insert(
            createdBy = schedule.created_by,
            title = schedule.title,
            description = schedule.description,
            location = schedule.location,
            startTime = schedule.start_time,
            endTime = schedule.end_time
        )

        try {
            remoteDataSource.insertSchedule(localSchedule)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return localSchedule
    }

    override suspend fun sync() {
        try {
            val clientSchedules = localDataSource.getUnsynced()

            val serverSchedules = remoteDataSource.syncSchedule(clientSchedules)

            localDataSource.sync(serverSchedules)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}