package com.example.myapplication.fake

import com.example.myapplication.data.repositories.UserRepository
import com.example.myapplication.data.sources.models.User

class FakeUserRepository : UserRepository {

    private val users = mutableListOf<User>()

    override suspend fun getAllUser(): List<User> {
        return users
    }

    override suspend fun getUserById(id: Int): User? {
        return users.find { it.id == id }
    }

    override suspend fun getUserByCompanyId(company_id: Int): List<User> {
        return users.filter { it.company_id == company_id }
    }

    override suspend fun insertUser(user: User): User {
        users.add(user)
        return user
    }

    override suspend fun deleteUser(id: Int) {
        users.removeIf { it.id == id }
    }

    override suspend fun updateUser(user: User) {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
        }
    }

    override suspend fun updatePassword(id: Int, password: String) {
        val user = users.find { it.id == id }
        user?.password = password
    }

    override suspend fun sync() {
        // kosong karena fake repository
    }
}