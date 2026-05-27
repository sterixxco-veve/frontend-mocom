package com.example.myapplication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repositories.ScheduleRepository

class AdminViewModelFactory(
    private val scheduleRepository: ScheduleRepository, // Tambahkan di sini
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            return AdminViewModel(scheduleRepository) as T
        }
        throw IllegalArgumentException("Kelas ViewModel tidak dikenal")
    }
}