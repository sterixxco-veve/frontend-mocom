package com.example.myapplication.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repositories.UserRepository
import com.example.myapplication.ui.staff.ChangePasswordViewModel

class ChangePasswordViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(ChangePasswordViewModel::class.java)) {

            return ChangePasswordViewModel(
                userRepository
            ) as T

        }

        throw IllegalArgumentException("Unknown ViewModel")

    }

}