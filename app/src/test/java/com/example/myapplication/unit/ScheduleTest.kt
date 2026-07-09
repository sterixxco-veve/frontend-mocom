package com.example.myapplication.data.sources.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class ScheduleTest {

    @Test
    fun createSchedule_success() {

        val schedule = Schedule.create(
            id = 1,
            created_by = 10,
            title = "Praktikum Mobile",
            description = "Belajar Room",
            location = "Lab 1",
            start_time = 1000L,
            end_time = 2000L
        )

        assertEquals(1, schedule.id)
        assertEquals(10, schedule.created_by)
        assertEquals("Praktikum Mobile", schedule.title)
        assertEquals("Belajar Room", schedule.description)
        assertEquals("Lab 1", schedule.location)
        assertEquals(1000L, schedule.start_time)
        assertEquals(2000L, schedule.end_time)
        assertEquals(1, schedule.company_id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankTitle_shouldThrowException() {

        Schedule(
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

    @Test
    fun copy_shouldUpdateTitle() {

        val schedule = Schedule(
            id = 1,
            created_by = 1,
            company_id = 1,
            title = "Old Title",
            description = "Desc",
            start_time = 1L,
            end_time = 2L,
            location = "Lab",
            created_at = 3L
        )

        val copied = schedule.copy(
            title = "New Title"
        )

        assertEquals("New Title", copied.title)
        assertEquals(schedule.id, copied.id)
        assertEquals(schedule.created_by, copied.created_by)
    }

    @Test
    fun copy_shouldKeepOldValues() {

        val schedule = Schedule(
            id = 5,
            created_by = 7,
            company_id = 1,
            title = "Meeting",
            description = "Weekly",
            start_time = 100L,
            end_time = 200L,
            location = "Office",
            created_at = 300L
        )

        val copied = schedule.copy()

        assertEquals(schedule.id, copied.id)
        assertEquals(schedule.title, copied.title)
        assertEquals(schedule.description, copied.description)
        assertEquals(schedule.location, copied.location)
        assertEquals(schedule.start_time, copied.start_time)
        assertEquals(schedule.end_time, copied.end_time)
        assertEquals(schedule.created_at, copied.created_at)
    }

    @Test
    fun copy_shouldReturnDifferentObject() {

        val schedule = Schedule(
            id = 1,
            created_by = 1,
            company_id = 1,
            title = "Schedule",
            description = "",
            start_time = 1L,
            end_time = 2L,
            location = "",
            created_at = 3L
        )

        val copied = schedule.copy()

        assertNotSame(schedule, copied)
    }

    @Test
    fun create_shouldUseGivenTime() {

        val schedule = Schedule.create(
            created_by = 2,
            title = "Testing",
            description = "",
            location = "",
            start_time = 123456L,
            end_time = 654321L
        )

        assertEquals(123456L, schedule.start_time)
        assertEquals(654321L, schedule.end_time)
    }
}