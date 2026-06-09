package com.example.myapplication.data.repositories

import com.example.myapplication.data.sources.local.LocalDataSource
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.RemoteDataSource

class DefaultUserRepository(
    val localDataSource: LocalDataSource,
    val remoteDataSource: RemoteDataSource
) : UserRepository {

    override suspend fun getAllUser(): List<User> {
        return try {
            val remoteData = remoteDataSource.fetchAllUsers()
            remoteData
        } catch (e: Exception) {
            e.printStackTrace()
            localDataSource.getAllUser()
        }
    }

    override suspend fun getUserByCompanyId(id: Int): List<User> {
        return try {
            val remoteData = remoteDataSource.fetchUserByCompanyId(id)
            remoteData
        } catch (e: Exception) {
            e.printStackTrace()
            // 💡 OPTIMALISASI: Jika server offline, ambil data lokal yang terfilter company_id juga
            localDataSource.getUserByCompanyId(id)
        }
    }

    override suspend fun getUserById(id: Int): User? {
        return try {
            val remoteData = remoteDataSource.fetchUserById(id)
            remoteData
        } catch (e: Exception) {
            e.printStackTrace()
            // 💡 OPTIMALISASI: Jika server offline, ambil data lokal yang terfilter company_id juga
            localDataSource.getUserById(id)
        }
    }

    override suspend fun insertUser(user: User): User {
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(user: User) {
        TODO("Not yet implemented")
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