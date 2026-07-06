package com.example.myapplication.data.repositories

import android.util.Log
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
            localDataSource.getUserByCompanyId(id)
        }
    }

    override suspend fun getUserById(id: Int): User? {
        return try {
            val remoteData = remoteDataSource.fetchUserById(id)
            remoteData
        } catch (e: Exception) {
            e.printStackTrace()
            localDataSource.getUserById(id)
        }
    }

    override suspend fun insertUser(user: User): User {
        val localUser = localDataSource.insertUser(
            company_id = user.company_id,
            role_id = user.role_id,
            full_name = user.full_name,
            username = user.username,
            email = user.email,
            password = user.password,
            is_active = user.is_active
        )

        try {
            remoteDataSource.insertUser(localUser)
            Log.d("REPOSITORY_INSERT", "🚀 Sukses mengunggah user baru ke database server.")
        } catch (e: Exception) {
            Log.e("REPOSITORY_INSERT", "⚠️ Server offline! user tertahan di database lokal HP.")
        }

        return localUser
    }

    override suspend fun deleteUser(id: Int) {
        try {
            localDataSource.deleteUserLocalById(id)
            remoteDataSource.deleteUser(id)
            Log.d("REPOSITORY_DELETE", "🚀 Sukses menghapus user di lokal dan server cloud.")
        } catch (e: Exception) {
            Log.e("REPOSITORY_DELETE", "⚠️ Gagal menghapus di server (Offline), penghapusan tertahan di lokal: ${e.message}")
        }
    }

    override suspend fun updateUser(user: User) {
        try {
            // 1. Perbarui data lokal di Room terlebih dahulu agar UI langsung berubah seketika
            localDataSource.updateUserLocal(user)

            // 2. Coba kirim perubahan data ke database MySQL Server Cloud
            remoteDataSource.updateUser(user)
            Log.d("REPOSITORY_UPDATE", "🚀 Sukses memperbarui jadwal di lokal dan server cloud.")
        } catch (e: Exception) {
            Log.e("REPOSITORY_UPDATE", "⚠️ Gagal update ke server (Offline), data tersimpan di lokal saja: ${e.message}")
            // Catatan: Jika ingin diproses sync nanti, kamu bisa mengubah flag status is_synced = 0 di tabel lokalmu di sini
        }
    }

    override suspend fun updatePassword(
        id: Int,
        password: String
    ) {
        remoteDataSource.updatePassword(
            id,
            password
        )
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