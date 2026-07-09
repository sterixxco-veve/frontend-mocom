package com.example.myapplication.data.sources.models

import org.junit.Assert.*
import org.junit.Test

class AssignmentTest {

    @Test
    fun createAssignment_success() {

        val assignment = Assignment.create(
            id = 1,
            schedule_id = 10,
            user_id = 20,
            role_in_event = "MC",
            job_desc = "Membuka acara",
            status = "pending",
            assigned_at = 100L
        )

        assertEquals(1, assignment.id)
        assertEquals(10, assignment.schedule_id)
        assertEquals(20, assignment.user_id)
        assertEquals("MC", assignment.role_in_event)
        assertEquals("Membuka acara", assignment.job_desc)
        assertEquals("pending", assignment.status)
        assertEquals(100L, assignment.assigned_at)
    }

    @Test
    fun copy_shouldUpdateStatus() {

        val assignment = Assignment.create(
            id = 1,
            schedule_id = 10,
            user_id = 20,
            role_in_event = "MC",
            job_desc = "Membuka acara",
            status = "pending",
            assigned_at = 100L
        )

        val copied = assignment.copy(
            status = "approved"
        )

        assertEquals("approved", copied.status)
        assertEquals("MC", copied.role_in_event)
    }

    @Test
    fun copy_shouldUpdateRole() {

        val assignment = Assignment.create(
            id = 1,
            schedule_id = 10,
            user_id = 20,
            role_in_event = "MC",
            job_desc = "Membuka acara",
            status = "pending",
            assigned_at = 100L
        )

        val copied = assignment.copy(
            role_in_event = "Koordinator"
        )

        assertEquals("Koordinator", copied.role_in_event)
    }

    @Test
    fun copy_shouldKeepOldValues() {

        val assignment = Assignment.create(
            id = 1,
            schedule_id = 10,
            user_id = 20,
            role_in_event = "MC",
            job_desc = "Membuka acara",
            status = "pending",
            assigned_at = 100L
        )

        val copied = assignment.copy()

        assertEquals(assignment.id, copied.id)
        assertEquals(assignment.schedule_id, copied.schedule_id)
        assertEquals(assignment.user_id, copied.user_id)
        assertEquals(assignment.role_in_event, copied.role_in_event)
        assertEquals(assignment.job_desc, copied.job_desc)
        assertEquals(assignment.status, copied.status)
        assertEquals(assignment.assigned_at, copied.assigned_at)
    }

    @Test
    fun copy_shouldReturnDifferentObject() {

        val assignment = Assignment.create(
            id = 1,
            schedule_id = 10,
            user_id = 20,
            role_in_event = "MC",
            job_desc = "Membuka acara",
            status = "pending",
            assigned_at = 100L
        )

        val copied = assignment.copy()

        assertNotSame(assignment, copied)
    }

    @Test
    fun copy_shouldUpdateAssignedTime() {

        val assignment = Assignment.create(
            id = 1,
            schedule_id = 10,
            user_id = 20,
            role_in_event = "MC",
            job_desc = "Membuka acara",
            status = "pending",
            assigned_at = 100L
        )

        val copied = assignment.copy(
            assigned_at = 999L
        )

        assertEquals(999L, copied.assigned_at)
    }
}