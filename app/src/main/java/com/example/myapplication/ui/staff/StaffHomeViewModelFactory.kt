package com.example.myapplication.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ApiService
import com.example.myapplication.data.repositories.AnnouncementRepository
import com.example.myapplication.data.repositories.UserRepository

class StaffHomeViewModelFactory(private val apiService: ApiService, private val userRepository: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StaffHomeViewModel::class.java)) {
            return StaffHomeViewModel(apiService,userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}