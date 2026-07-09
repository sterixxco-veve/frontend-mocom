package com.example.myapplication.data.sources.models

import org.junit.Assert.*
import org.junit.Test

class UserTest {

    @Test
    fun createUser_success() {

        val user = User.create(
            id = 1,
            username = "eric",
            full_name = "Eric Fernando",
            email = "eric@gmail.com",
            password = "123456",
            is_active = 1,
            role_id = 2,
            company_id = 1,
            created_at = 100L,
            updated_at = 200L
        )

        assertEquals(1, user.id)
        assertEquals("eric", user.username)
        assertEquals("Eric Fernando", user.full_name)
        assertEquals("eric@gmail.com", user.email)
        assertEquals("123456", user.password)
        assertEquals(1, user.company_id)
    }

    @Test
    fun copy_shouldUpdateUsername() {

        val user = User.create(
            id = 1,
            username = "eric",
            full_name = "Eric",
            email = "eric@gmail.com",
            password = "123456",
            is_active = 1,
            role_id = 2,
            company_id = 1,
            created_at = 100L,
            updated_at = 200L
        )

        val copied = user.copy(
            username = "eric_new"
        )

        assertEquals("eric_new", copied.username)
        assertEquals(user.full_name, copied.full_name)
    }

    @Test
    fun copy_shouldUpdatePassword() {

        val user = User.create(
            id = 1,
            username = "eric",
            full_name = "Eric",
            email = "eric@gmail.com",
            password = "123456",
            is_active = 1,
            role_id = 2,
            company_id = 1,
            created_at = 100L,
            updated_at = 200L
        )

        val copied = user.copy(
            password = "abcdef"
        )

        assertEquals("abcdef", copied.password)
    }

    @Test
    fun copy_shouldKeepOldValues() {

        val user = User.create(
            id = 1,
            username = "eric",
            full_name = "Eric",
            email = "eric@gmail.com",
            password = "123456",
            is_active = 1,
            role_id = 2,
            company_id = 1,
            created_at = 100L,
            updated_at = 200L
        )

        val copied = user.copy()

        assertEquals(user.id, copied.id)
        assertEquals(user.username, copied.username)
        assertEquals(user.full_name, copied.full_name)
        assertEquals(user.email, copied.email)
        assertEquals(user.password, copied.password)
        assertEquals(user.company_id, copied.company_id)
    }

    @Test
    fun copy_shouldReturnDifferentObject() {

        val user = User.create(
            id = 1,
            username = "eric",
            full_name = "Eric",
            email = "eric@gmail.com",
            password = "123456",
            is_active = 1,
            role_id = 2,
            company_id = 1,
            created_at = 100L,
            updated_at = 200L
        )

        val copied = user.copy()

        assertNotSame(user, copied)
    }

    @Test
    fun toString_shouldContainUsername() {

        val user = User.create(
            id = 1,
            username = "eric",
            full_name = "Eric",
            email = "eric@gmail.com",
            password = "123456",
            is_active = 1,
            role_id = 2,
            company_id = 1,
            created_at = 100L,
            updated_at = 200L
        )

        assertTrue(user.toString().contains("eric"))
    }
}