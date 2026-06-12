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

    fun generateBurnoutAnalysis(companyId: Int) {
        _burnoutAnalysis.value = "Sedang memuat data dan menganalisis..."
        viewModelScope.launch {
            try {
                val staffList = userRepository.getUserByCompanyId(companyId) ?: emptyList()
                val attendanceList = attendanceRepository.getAttendanceByCompanyId(companyId) ?: emptyList()

                if (attendanceList.isEmpty()) {
                    _burnoutAnalysis.postValue("Tidak ada data absensi untuk dianalisis.")
                    return@launch
                }

                val promptBuilder = StringBuilder()
                promptBuilder.append("Analisis tingkat burnout asisten berikut:\n\n")
                promptBuilder.append("Daftar Asisten Aktif:\n")
                
                val assistants = staffList.filter { it.role_id == 2 }
                if (assistants.isEmpty()) {
                    promptBuilder.append("- Tidak ada asisten aktif terdaftar.\n")
                } else {
                    assistants.forEach {
                        promptBuilder.append("- ID #${it.id}: ${it.full_name} (Status: ${if (it.is_active == 1) "Aktif" else "Nonaktif"})\n")
                    }
                }
                
                promptBuilder.append("\nRiwayat Kehadiran:\n")
                
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
                    timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")
                }
                
                attendanceList.forEach { att ->
                    val checkInStr = if (att.check_in > 0) sdf.format(Date(att.check_in)) else "--"
                    val checkOutStr = if (att.check_out > 0) sdf.format(Date(att.check_out)) else "--"
                    promptBuilder.append("- Penugasan #${att.assignment_id}: Status=${att.status}, CheckIn=$checkInStr, CheckOut=$checkOutStr\n")
                }
                
                promptBuilder.append("\nHarap berikan analisis dalam Bahasa Indonesia yang mencakup:\n")
                promptBuilder.append("1. Ringkasan singkat statistik kehadiran (berapa persen Present, Late, Absent).\n")
                promptBuilder.append("2. Deteksi asisten yang memiliki indikasi burnout (terlalu sering terlambat/absen atau pola tidak sehat).\n")
                promptBuilder.append("3. Rekomendasi konkret bagi koordinator untuk meningkatkan kebugaran kerja asisten.\n")
                promptBuilder.append("Berikan hasil dengan format dokumen resmi yang rapi tanpa menyertakan kode markdown seperti asteriks tebal berlebih, tapi gunakan spasi paragraf yang bagus.")

                val response = generativeModel.generateContent(promptBuilder.toString())
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
}