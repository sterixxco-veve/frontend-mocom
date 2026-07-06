package com.example.myapplication.ui.staff

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.UserRepository
import kotlinx.coroutines.launch

class ChangePasswordViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean>
        get() = _isSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    fun changePassword(
        userId: Int,
        newPassword: String
    ) {

        _isLoading.value = true

        viewModelScope.launch {

            try {

                userRepository.updatePassword(
                    userId,
                    newPassword
                )

                _isSuccess.postValue(true)

            } catch (e: Exception) {

                Log.e(
                    "CHANGE_PASSWORD",
                    e.message ?: "Unknown Error"
                )

                _errorMessage.postValue(
                    e.message ?: "Gagal mengganti password."
                )

                _isSuccess.postValue(false)

            } finally {

                _isLoading.postValue(false)

            }

        }

    }

}