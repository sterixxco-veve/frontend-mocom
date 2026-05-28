package com.example.myapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

//    fun login(user: User, onResult: (User?) -> Unit) {
//        isLoading.value = true
//
//        viewModelScope.launch {
//            try {
//                val response = RetrofitClient.instance.login(user)
//
//                if (response.isSuccessful) {
//                    onResult(response.body())
//                } else {
//                    onResult(null)
//                }
//
//            } catch (e: Exception) {
//                onResult(null)
//            } finally {
//                isLoading.value = false
//            }
//        }
//    }
//
//    fun register(user: User, onResult: (Boolean) -> Unit) {
//        isLoading.value = true
//
//        viewModelScope.launch {
//            try {
//                val response = RetrofitClient.instance.register(user)
//
//                if (response.isSuccessful) {
//                    onResult(true)
//                } else {
//                    onResult(false)
//                }
//
//            } catch (e: Exception) {
//                onResult(false)
//            } finally {
//                isLoading.value = false
//            }
//        }
//    }
}