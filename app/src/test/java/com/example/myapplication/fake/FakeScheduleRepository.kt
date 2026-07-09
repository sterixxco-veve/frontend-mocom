package com.example.myapplication.fake

import com.example.myapplication.data.repositories.ScheduleRepository
import com.example.myapplication.data.sources.models.Schedule

class FakeScheduleRepository : ScheduleRepository {

    private val schedules = mutableListOf<Schedule>()

    override suspend fun getAll(): List<Schedule> {
        return schedules
    }

    override suspend fun getById(id: Int): Schedule? {
        return schedules.find { it.id == id }
    }

    override suspend fun getByCompanyId(id: Int): List<Schedule> {
        return schedules.filter { it.company_id == id }
    }

    override suspend fun insert(schedule: Schedule): Schedule {
        schedules.add(schedule)
        return schedule
    }

    override suspend fun update(schedule: Schedule) {
        val index = schedules.indexOfFirst { it.id == schedule.id }
        if (index != -1) {
            schedules[index] = schedule
        }
    }

    override suspend fun delete(id: Int) {
        schedules.removeIf { it.id == id }
    }

    override suspend fun sync() {
        // Tidak melakukan apa-apa (Fake Repository)
    }
}