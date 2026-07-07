package com.example.myapplication.ui.staff

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ApiService
import com.example.myapplication.RetrofitClient
import com.example.myapplication.data.repositories.AnnouncementRepository
import com.example.myapplication.data.repositories.UserRepository
import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.remote.request.NfcCheckInRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StaffHomeViewModel(private val apiService: ApiService, private val userRepository: UserRepository) : ViewModel() {

    // 1. TAMBAHKAN WADAH STATE INI DI BAGIAN ATAS VIEWMODEL
    private val _todayAttendance = MutableStateFlow<Attendance?>(null)
    val todayAttendance: StateFlow<Attendance?> = _todayAttendance

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage

    // 2. SINKRONISASIKAN DI DALAM FUNGSI LOAD DATA DASHBOARD
    fun loadStaffHomeData(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 🟢 PERBAIKAN: Langsung ambil List-nya tanpa perlu .isSuccessful atau .body()
                val listLogs = apiService.getAttendancesByUserId(userId)

                // Ambil data log absensi paling terakhir jika list tidak kosong
                val lastLog = listLogs.lastOrNull()

                // Set ke StateFlow untuk di-consume oleh Fragment
                _todayAttendance.value = lastLog

            } catch (e: Exception) {
                // Jika terjadi eror koneksi atau parsing, tangkap di sini
                _toastMessage.emit("Gagal menyinkronkan status: ${e.localizedMessage}")
                _todayAttendance.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAnnouncements(companyId: Int) {

        viewModelScope.launch {

            try {

                val users = userRepository.getAllUser()

                val userMap = users.associateBy { it.id }

                val response =
                    RetrofitClient.apiService.getAllAnnouncements()

                val responseList =
                    response.body()

                if (responseList != null && responseList.isNotEmpty()) {

                    val result = responseList.mapNotNull { announcement ->

                        val author =
                            userMap[announcement.created_by]
                                ?: return@mapNotNull null

                        if (author.company_id != companyId)
                            return@mapNotNull null

                        announcement.apply {
                            authorName = author.full_name
                        }

                    }

                    _announcements.value = result

                } else {

                    _announcements.value = emptyList()

                }

            } catch (e: Exception) {

                Log.e("LOAD_ANNOUNCEMENT", e.toString())

                _announcements.value = emptyList()

            }

        }

    }
    fun checkInWithNfc(userId: Int, nfcUid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.checkInWithNfc(NfcCheckInRequest(nfcUid))
                if (response.isSuccessful && response.body() != null) {
                    val message = response.body()?.get("message")?.toString() ?: "Transaksi Berhasil"
                    _toastMessage.emit(message)

                    // Otomatis sinkronisasi ulang data dashboard Home (Status kehadiran, progress bar, dll)
                    loadStaffHomeData(userId)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Gagal memproses kartu NFC"
                    // Memunculkan pesan kustom keguguran validasi (misal: "Anda tidak memiliki jadwal hari ini!")
                    if (errorMsg.contains("message")) {
                        _toastMessage.emit("Gagal: Absen tidak sesuai jadwal")
                    } else {
                        _toastMessage.emit("Gagal Absen NFC")
                    }
                }
            } catch (e: Exception) {
                _toastMessage.emit("Masalah Koneksi: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}