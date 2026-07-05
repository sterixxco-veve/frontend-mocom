package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ApiService
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.data.sources.remote.request.NfcRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val apiService: ApiService) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage

    fun loadUserProfile(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    _userProfile.value = response.body()
                } else {
                    _toastMessage.emit("Gagal memuat profil dari server")
                }
            } catch (e: Exception) {
                _toastMessage.emit("Error Koneksi: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerNfcCard(userId: Int, nfcUid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Pastikan ApiService kamu mengembalikan Response<Map<String, Any>> atau Response<JsonObject>
                val response = apiService.assignNfc(NfcRequest(userId, nfcUid))

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body() as? Map<*, *>
                    // Kita parsing status 'success' secara aman baik berupa Boolean asli maupun String
                    val isSuccess = body?.get("success")?.toString()?.toBoolean() ?: false

                    if (isSuccess) {
                        _toastMessage.emit("Sukses! ID NFC Berhasil Didaftarkan.")
                        loadUserProfile(userId) // Refresh data profil
                    } else {
                        val serverMsg = body?.get("message")?.toString() ?: "Gagal konfigurasi"
                        _toastMessage.emit("Server Gagal: $serverMsg")
                    }
                } else if (response.code() == 409) {
                    _toastMessage.emit("Gagal: Kartu NFC ini sudah digunakan oleh user lain!")
                } else {
                    // JIKA ERROR (400, 500, dll): Ambil baris teks error asli dari Node.js
                    val errorBodyString = response.errorBody()?.string() ?: "Unknown Error"
                    _toastMessage.emit("HTTP Error ${response.code()}: $errorBodyString")
                }
            } catch (e: Exception) {
                _toastMessage.emit("Gagal menyambung ke API: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}