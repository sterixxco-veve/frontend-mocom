package com.example.myapplication.repository

import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.fake.FakeScheduleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InsertScheduleRepositoryTest {

    private lateinit var repository: FakeScheduleRepository

    @Before
    fun setup() {
        repository = FakeScheduleRepository()
    }

    @Test
    fun insert_success() = runTest {

        val schedule = Schedule(
            id = 1,
            created_by = 1,
            company_id = 1,
            title = "Praktikum Web",
            description = "Belajar Retrofit",
            start_time = 1L,
            end_time = 2L,
            location = "Lab Komputer",
            created_at = 3L
        )

        repository.insert(schedule)

        val result = repository.getAll()

        assertEquals(1, result.size)
        assertEquals("Praktikum Web", result[0].title)
    }

    @Test
    fun getById_success() = runTest {

        val schedule = Schedule(
            id = 10,
            created_by = 1,
            company_id = 1,
            title = "Mobile Programming",
            description = "Jetpack Compose",
            start_time = 1L,
            end_time = 2L,
            location = "Lab Mobile",
            created_at = 3L
        )

        repository.insert(schedule)

        val result = repository.getById(10)

        assertNotNull(result)
        assertEquals("Mobile Programming", result?.title)
    }

    @Test
    fun getByCompanyId_success() = runTest {

        repository.insert(
            Schedule(
                id = 1,
                created_by = 1,
                company_id = 1,
                title = "Web",
                description = "",
                start_time = 1L,
                end_time = 2L,
                location = "",
                created_at = 3L
            )
        )

        repository.insert(
            Schedule(
                id = 2,
                created_by = 1,
                company_id = 2,
                title = "Android",
                description = "",
                start_time = 1L,
                end_time = 2L,
                location = "",
                created_at = 3L
            )
        )

        val result = repository.getByCompanyId(1)

        assertEquals(1, result.size)
        assertEquals(1, result[0].company_id)
    }

    @Test
    fun update_success() = runTest {

        val schedule = Schedule(
            id = 5,
            created_by = 1,
            company_id = 1,
            title = "Old Title",
            description = "",
            start_time = 1L,
            end_time = 2L,
            location = "",
            created_at = 3L
        )

        repository.insert(schedule)

        repository.update(
            schedule.copy(title = "New Title")
        )

        val updated = repository.getById(5)

        assertNotNull(updated)
        assertEquals("New Title", updated?.title)
    }

    @Test
    fun delete_success() = runTest {

        val schedule = Schedule(
            id = 7,
            created_by = 1,
            company_id = 1,
            title = "To Delete",
            description = "",
            start_time = 1L,
            end_time = 2L,
            location = "",
            created_at = 3L
        )

        repository.insert(schedule)

        repository.delete(7)

        val result = repository.getById(7)

        assertNull(result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun insert_emptyTitle_throwException() {

        Schedule(
            id = 1,
            created_by = 1,
            company_id = 1,
            title = "",
            description = "",
            start_time = 1L,
            end_time = 2L,
            location = "",
            created_at = 3L
        )
    }
}