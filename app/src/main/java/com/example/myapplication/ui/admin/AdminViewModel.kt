package com.example.myapplication.ui.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.AttendanceRepository
import com.example.myapplication.data.repositories.ScheduleRepository
import com.example.myapplication.data.repositories.UserRepository
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.google.ai.client.generativeai.GenerativeModel
import com.example.myapplication.RetrofitClient
import com.example.myapplication.data.sources.models.Assignment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminViewModel (
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository
): ViewModel() {
    private val _scheduleList = ArrayList<Schedule>()
    private val _userList = ArrayList<User>()
    private val _attendanceList = ArrayList<Attendance>()
    private val _schedules = MutableLiveData(_scheduleList.toList())
    val schedules: LiveData<List<Schedule>>
        get() = _schedules
    private val _users = MutableLiveData(_userList.toList())
    val users: LiveData<List<User>>
        get() = _users
    private val _attendances = MutableLiveData(_attendanceList.toList())
    val attendances: LiveData<List<Attendance>>
        get() = _attendances

    private val _burnoutAnalysis = MutableLiveData<String>()
    val burnoutAnalysis: LiveData<String>
        get() = _burnoutAnalysis

    private val _companyName = MutableLiveData<String>()
    val companyName: LiveData<String>
        get() = _companyName

    fun loadCompanyName(companyId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCompanyDetail(companyId)
                if (response.isSuccessful && response.body() != null) {
                    _companyName.postValue(response.body()!!.company_name)
                } else {
                    _companyName.postValue("Perusahaan #$companyId")
                }
            } catch (e: Exception) {
                _companyName.postValue("Perusahaan #$companyId")
            }
        }
    }

    fun generateBurnoutAnalysis(prompt: String) {
        _burnoutAnalysis.value = "Sedang memuat data dan menganalisis..."
        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                _burnoutAnalysis.postValue(response.text ?: "Gagal memproses rekomendasi AI.")
            } catch (e: Exception) {
                android.util.Log.e("GEMINI_REPORT", "Error: ${e.message}", e)
                _burnoutAnalysis.postValue("Analisis AI gagal dijalankan: ${e.message}\n\n(Catatan: Pastikan koneksi internet tersedia dan API Key Gemini valid.)")
            }
        }
    }


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
    fun loadAttendanceByCompanyId(companyId: Int) {
        Log.d("TRACK_ATTENDANCE", "========================================")
        Log.d("TRACK_ATTENDANCE", "🔄 loadAttendanceByCompanyId() dipicu untuk Company ID: $companyId")

        viewModelScope.launch {
            try {
                val result = attendanceRepository.getAttendanceByCompanyId(companyId)

                Log.d("TRACK_ATTENDANCE", "✅ BERHASIL! Mendapatkan ${result?.size ?: 0} data dari repositori.")
                if (result != null && result.isNotEmpty()) {
                    result.forEachIndexed { index, assignment ->
                        Log.d("TRACK_USER", "   [Data ke-$index] assignment_id: ${assignment.assignment_id} | check_in: ${assignment.check_in} | check_out: ${assignment.check_out}")
                    }
                } else {
                    Log.w("TRACK_ATTENDANCE", "⚠️ Data kosong [] - Periksa apakah company_id $companyId memiliki jadwal di DB MySQL.")
                }

                _attendances.postValue(result ?: emptyList())
            } catch (e: Exception) {
                android.util.Log.e("TRACK_ATTENDANCE", "❌ GAGAL mengambil jadwal karena Error: ${e.javaClass.simpleName} -> ${e.message}")
                e.printStackTrace()
                _attendances.postValue(emptyList())
            }
            Log.d("TRACK_ATTENDANCE", "========================================")
        }
    }


    //ADD
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
    fun addUser(user: User, companyId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.insertUser(user)
                onResult(true)
            } catch (e: Exception) {
                Log.e("TRACK_SCHEDULE", "❌ Gagal menambahkan: ${e.message}")
                onResult(false)
            }
        }
    }


    //UPDATE
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
    fun updateUser(user: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.updateUser(user)
                onResult(true)
            } catch (e: Exception) {
                android.util.Log.e("VIEWMODEL_UPDATE", "❌ Gagal update: ${e.message}")
                onResult(false)
            }
        }
    }


    //DELETE
    fun deleteSchedule(id: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                scheduleRepository.delete(id)
                onResult(true) // Kirim sinyal sukses ke Fragment
            } catch (e: Exception) {
                android.util.Log.e("VIEWMODEL_DELETE", "Gagal hapus: ${e.message}")
                onResult(false) // Kirim sinyal gagal
            }
        }
    }
    fun deleteUser(id: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.deleteUser(id)
                onResult(true)
            } catch (e: Exception) {
                android.util.Log.e("VIEWMODEL_DELETE", "Gagal hapus: ${e.message}")
                onResult(false)
            }
        }
    }

    //Assign Staff
    // =========================================================================
// 🛠️ TAMBAHKAN FUNGSI INI DI DALAM ADMINVIEWMODEL
// =========================================================================
    fun assignStaffToSchedule(scheduleId: Int, staffId: Int, onResult: (Boolean) -> Unit) {
        Log.d("TRACK_ASSIGNMENT", "➕ Mencoba menugaskan Staff ID $staffId ke Schedule ID $scheduleId")

        viewModelScope.launch {
            try {
                // Buat objek Assignment sesuai model yang dibutuhkan API Anda
                // Sesuaikan nama parameter (misal: schedule_id atau user_id) dengan constructor class Assignment Anda
                val newAssignment = Assignment(
                    id = 0, // Biasanya 0 atau null untuk auto-increment di MySQL
                    schedule_id = scheduleId,
                    user_id = staffId
                )

                // Panggil API lewat repository penugasan Anda
                // Catatan: Jika Anda belum melakukan inject AssignmentRepository ke ViewModel,
                // Anda bisa menembaknya langsung via RetrofitClient.apiService.insertAssignments(newAssignment)
                val response = RetrofitClient.apiService.insertAssignments(newAssignment)

                if (response.isSuccessful && response.body() != null) {
                    Log.d("TRACK_ASSIGNMENT", "✅ Berhasil menyimpan penugasan ke database server.")
                    onResult(true)
                } else {
                    Log.e("TRACK_ASSIGNMENT", "❌ Server menolak menyimpan: ${response.errorBody()?.string()}")
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("TRACK_ASSIGNMENT", "❌ Gagal menyimpan penugasan karena Error: ${e.message}")
                e.printStackTrace()
                onResult(false)
            }
        }
    }
}