package com.example.myapplication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repositories.AnnouncementRepository
import com.example.myapplication.data.repositories.AttendanceRepository
import com.example.myapplication.data.repositories.ReplacementRepository
import com.example.myapplication.data.repositories.ScheduleRepository
import com.example.myapplication.data.repositories.UserRepository

class AdminViewModelFactory(
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository,
    private val announcementRepository: AnnouncementRepository,
    private val replacementRepository: ReplacementRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            return AdminViewModel(scheduleRepository, userRepository, attendanceRepository, announcementRepository, replacementRepository) as T
        }
        throw IllegalArgumentException("Kelas ViewModel tidak dikenal")
    }
}