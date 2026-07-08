package com.example.myapplication.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ApiService
import com.example.myapplication.data.repositories.AnnouncementRepository
import com.example.myapplication.data.repositories.AssignmentRepository
import com.example.myapplication.data.repositories.AttendanceRepository
import com.example.myapplication.data.repositories.ReplacementRepository
import com.example.myapplication.data.repositories.ScheduleRepository
import com.example.myapplication.data.repositories.UserRepository
import com.example.myapplication.ui.profile.ProfileViewModel

class NotificationReplacementViewModelFactory(private val apiService: ApiService, private val scheduleRepository: ScheduleRepository, private val attendanceRepository: AttendanceRepository, private val assignmentRepository: AssignmentRepository, private val announcementRepository: AnnouncementRepository, private val replacementRepository: ReplacementRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationReplacementViewModel::class.java)) {
            return NotificationReplacementViewModel(replacementRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}