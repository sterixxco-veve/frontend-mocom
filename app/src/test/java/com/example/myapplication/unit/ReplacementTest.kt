package com.example.myapplication.model

import com.example.myapplication.data.sources.models.Replacement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class ReplacementTest {

    @Test
    fun createReplacement_shouldStoreCorrectValues() {

        val now = LocalDateTime.now()

        val replacement = Replacement(
            id = 1,
            assignment_id = 10,
            requested_by = 2,
            replacement_user_id = 5,
            reason = "Sakit",
            status = "Pending",
            approved_by = 0,
            created_at = now
        )

        assertEquals(1, replacement.id)
        assertEquals(10, replacement.assignment_id)
        assertEquals(2, replacement.requested_by)
        assertEquals(5, replacement.replacement_user_id)
        assertEquals("Sakit", replacement.reason)
        assertEquals("Pending", replacement.status)
        assertEquals(0, replacement.approved_by)
        assertEquals(now, replacement.created_at)
    }

    @Test
    fun replacementReason_shouldNotBeEmpty() {

        val replacement = Replacement(
            assignment_id = 1,
            requested_by = 2,
            replacement_user_id = 3,
            reason = "Keperluan keluarga",
            status = "Pending",
            approved_by = 0,
            created_at = LocalDateTime.now()
        )

        assertTrue(replacement.reason.isNotBlank())
    }

    @Test
    fun replacementStatus_shouldBePending() {

        val replacement = Replacement(
            assignment_id = 1,
            requested_by = 2,
            replacement_user_id = 3,
            reason = "Sakit",
            status = "Pending",
            approved_by = 0,
            created_at = LocalDateTime.now()
        )

        assertEquals("Pending", replacement.status)
    }

    @Test
    fun replacementCreatedAt_shouldMatchGivenDate() {

        val date = LocalDateTime.of(2026, 7, 10, 8, 0)

        val replacement = Replacement(
            assignment_id = 1,
            requested_by = 2,
            replacement_user_id = 3,
            reason = "Izin",
            status = "Approved",
            approved_by = 10,
            created_at = date
        )

        assertEquals(date, replacement.created_at)
    }
}