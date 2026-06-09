package com.example.myapplication.ui.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.ScheduleRepository
import com.example.myapplication.data.repositories.UserRepository
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class AdminViewModel (
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository
): ViewModel() {
    private val _scheduleList = ArrayList<Schedule>()
    private val _userList = ArrayList<User>()
    private val _schedules = MutableLiveData(_scheduleList.toList())
    val schedules: LiveData<List<Schedule>>
        get() = _schedules
    private val _users = MutableLiveData(_userList.toList())
    val users: LiveData<List<User>>
        get() = _users


    // Inisialisasi Model Gemini SDK
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "AIzaSyB7v6J3IGCIXn4IAdJ60t1jlOEdcA6D_vI"
    )

    fun init() {
        viewModelScope.launch {
            refreshList()
        }
    }

    private suspend fun refreshList() {
        _scheduleList.clear()
        _scheduleList.addAll(scheduleRepository.getAll())
    }

    fun loadSchedules(companyId: Int) {
        // 💡 LOG 1: Pastikan fungsi ini terpicu saat Fragment dibuka / Swipe-to-Refresh dijalankan
        android.util.Log.d("TRACK_SCHEDULE", "========================================")
        android.util.Log.d("TRACK_SCHEDULE", "🔄 loadSchedules() dipicu untuk Company ID: $companyId")

        viewModelScope.launch {
            try {
                val result = scheduleRepository.getByCompanyId(companyId)

                // 💡 LOG 2: Cek berapa jumlah data yang berhasil ditarik dari repository
                android.util.Log.d("TRACK_SCHEDULE", "✅ BERHASIL! Mendapatkan ${result?.size ?: 0} data dari repositori.")
                if (result != null && result.isNotEmpty()) {
                    result.forEachIndexed { index, schedule ->
                        android.util.Log.d("TRACK_SCHEDULE", "   [Data ke-$index] Title: ${schedule.title} | Location: ${schedule.location}")
                    }
                } else {
                    android.util.Log.w("TRACK_SCHEDULE", "⚠️ Data kosong [] - Periksa apakah company_id $companyId memiliki jadwal di DB MySQL.")
                }

                _schedules.postValue(result ?: emptyList())
            } catch (e: Exception) {
                // 💡 LOG 3: Jika ada crash jaringan, type mismatch, atau parse date error, tangkap di sini!
                android.util.Log.e("TRACK_SCHEDULE", "❌ GAGAL mengambil jadwal karena Error: ${e.javaClass.simpleName} -> ${e.message}")
                e.printStackTrace()
                _schedules.postValue(emptyList())
            }
            android.util.Log.d("TRACK_SCHEDULE", "========================================")
        }
    }
    fun loadStaffByIsActive(companyId: Int) {
        Log.d("TRACK_USER", "========================================")
        Log.d("TRACK_USER", "🔄 loadStaffByCompanyId() dipicu untuk Company ID: $companyId")

        viewModelScope.launch {
            try {
                val result = userRepository.getUserByCompanyId(companyId)

                Log.d("TRACK_USER", "✅ BERHASIL! Mendapatkan ${result?.size ?: 0} data dari repositori.")
                if (result != null && result.isNotEmpty()) {
                    result.forEachIndexed { index, user ->
                        Log.d("TRACK_USER", "   [Data ke-$index] Name: ${user.full_name} | Company_id: ${user.company_id} | Role: ${user.role_id} | is_active: ${user.is_active}")
                    }
                } else {
                    Log.w("TRACK_USER", "⚠️ Data kosong [] - Periksa apakah company_id $companyId memiliki jadwal di DB MySQL.")
                }

                _users.postValue(result ?: emptyList())
            } catch (e: Exception) {
                // 💡 LOG 3: Jika ada crash jaringan, type mismatch, atau parse date error, tangkap di sini!
                android.util.Log.e("TRACK_SCHEDULE", "❌ GAGAL mengambil jadwal karena Error: ${e.javaClass.simpleName} -> ${e.message}")
                e.printStackTrace()
                _users.postValue(emptyList())
            }
            android.util.Log.d("TRACK_SCHEDULE", "========================================")
        }
    }

    fun loadUserByCompanyId(companyId: Int) {
        Log.d("TRACK_USER", "========================================")
        Log.d("TRACK_USER", "🔄 loadUserByCompanyId() dipicu untuk Company ID: $companyId")

        viewModelScope.launch {
            try {
                val result = userRepository.getUserByCompanyId(companyId)

                Log.d("TRACK_USER", "✅ BERHASIL! Mendapatkan ${result?.size ?: 0} data dari repositori.")
                if (result != null && result.isNotEmpty()) {
                    result.forEachIndexed { index, user ->
                        Log.d("TRACK_USER", "   [Data ke-$index] Name: ${user.full_name} | Company_id: ${user.company_id} | Role: ${user.role_id} | is_active: ${user.is_active}")
                    }
                } else {
                    Log.w("TRACK_USER", "⚠️ Data kosong [] - Periksa apakah company_id $companyId memiliki jadwal di DB MySQL.")
                }

                _users.postValue(result ?: emptyList())
            } catch (e: Exception) {
                // 💡 LOG 3: Jika ada crash jaringan, type mismatch, atau parse date error, tangkap di sini!
                android.util.Log.e("TRACK_SCHEDULE", "❌ GAGAL mengambil jadwal karena Error: ${e.javaClass.simpleName} -> ${e.message}")
                e.printStackTrace()
                _users.postValue(emptyList())
            }
            android.util.Log.d("TRACK_SCHEDULE", "========================================")
        }
    }

    fun addSchedule(schedule: Schedule, companyId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                scheduleRepository.insert(schedule)
                onResult(true)
            } catch (e: Exception) {
                Log.e("TRACK_SCHEDULE", "❌ Gagal menambahkan: ${e.message}")
                onResult(false)
            }
        }
    }

    fun updateSchedule(schedule: Schedule, onResult: (Boolean) -> Unit) {
        Log.d("TRACK_SCHEDULE", "✏️ Sedang mencoba memperbarui jadwal ID [${schedule.id}]: ${schedule.title}")
        viewModelScope.launch {
            try {
                // Memicu fungsi update di repository kamu
                scheduleRepository.update(schedule)
                Log.d("TRACK_SCHEDULE", "✅ Sukses memperbarui jadwal di server.")
                onResult(true)
            } catch (e: Exception) {
                Log.e("TRACK_SCHEDULE", "❌ GAGAL memperbarui jadwal: ${e.message}")
                e.printStackTrace()
                onResult(false)
            }
        }
    }
}