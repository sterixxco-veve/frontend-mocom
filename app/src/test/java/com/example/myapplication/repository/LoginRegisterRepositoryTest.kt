package com.example.myapplication.repository

import com.example.myapplication.data.sources.models.User
import com.example.myapplication.fake.FakeUserRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LoginRegisterRepositoryTest {

    private lateinit var repository: FakeUserRepository

    @Before
    fun setup() {
        repository = FakeUserRepository()
    }

    @Test
    fun registerUser_success() = runBlocking {

        val user = User(
            id = 1,
            company_id = 1,
            role_id = 2,
            full_name = "Budi",
            username = "budi",
            email = "budi@test.com",
            password = "123456",
            is_active = 1
        )

        repository.insertUser(user)

        val result = repository.getAllUser()

        assertEquals(1, result.size)
        assertEquals("Budi", result[0].full_name)
    }

    @Test
    fun loginUser_success() = runBlocking {

        val user = User(
            id = 1,
            company_id = 1,
            role_id = 2,
            full_name = "Budi",
            username = "budi",
            email = "budi@test.com",
            password = "123456",
            is_active = 1
        )

        repository.insertUser(user)

        val loginUser = repository
            .getAllUser()
            .find {
                it.email == "budi@test.com"
                        &&
                        it.password == "123456"
            }

        assertNotNull(loginUser)
    }

    @Test
    fun login_fail_wrongPassword() = runBlocking {

        val user = User(
            id = 1,
            company_id = 1,
            role_id = 2,
            full_name = "Budi",
            username = "budi",
            email = "budi@test.com",
            password = "123456",
            is_active = 1
        )

        repository.insertUser(user)

        val loginUser = repository
            .getAllUser()
            .find {
                it.email == "budi@test.com"
                        &&
                        it.password == "abcdef"
            }

        assertNull(loginUser)
    }
}