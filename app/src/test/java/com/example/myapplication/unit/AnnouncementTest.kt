package com.example.myapplication.data.sources.models

import org.junit.Assert.*
import org.junit.Test

class AnnouncementTest {

    @Test
    fun createAnnouncement_success() {

        val announcement = Announcement.create(
            id = 1,
            title = "Pengumuman",
            message = "Libur Nasional",
            created_by = 10,
            created_at = 100L
        )

        assertEquals(1, announcement.id)
        assertEquals("Pengumuman", announcement.title)
        assertEquals("Libur Nasional", announcement.message)
        assertEquals(10, announcement.created_by)
        assertEquals(100L, announcement.created_at)
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankTitle_shouldThrowException() {

        Announcement(
            id = 1,
            title = "",
            message = "Isi",
            created_by = 1,
            created_at = 100L
        )
    }

    @Test
    fun create_shouldUseGivenCreatedAt() {

        val announcement = Announcement.create(
            title = "Info",
            message = "Testing",
            created_by = 2,
            created_at = 999L
        )

        assertEquals(999L, announcement.created_at)
    }

    @Test
    fun create_shouldReturnDifferentObject() {

        val announcement1 = Announcement.create(
            title = "Info",
            message = "Testing",
            created_by = 1,
            created_at = 100L
        )

        val announcement2 = Announcement.create(
            title = "Info",
            message = "Testing",
            created_by = 1,
            created_at = 100L
        )

        assertNotSame(announcement1, announcement2)
    }
}