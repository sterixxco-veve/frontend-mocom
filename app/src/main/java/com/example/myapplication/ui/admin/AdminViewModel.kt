package com.example.myapplication.ui.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.App
import com.example.myapplication.data.repositories.AttendanceRepository
import com.example.myapplication.data.repositories.ScheduleRepository
import com.example.myapplication.data.repositories.UserRepository
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.google.ai.client.generativeai.GenerativeModel
import com.example.myapplication.RetrofitClient
import com.example.myapplication.data.repositories.AnnouncementRepository
import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.data.sources.models.Assignment
import com.example.myapplication.data.sources.remote.json.AnnouncementJson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AdminViewModel (
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository,
    private val announcementRepository: AnnouncementRepository
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

    private val _announcements = MutableLiveData<List<Announcement>?>()
    val announcements: LiveData<List<Announcement>?>
        get() = _announcements

    private val _assignments = MutableLiveData<List<Assignment>>(emptyList())
    val assignments: LiveData<List<Assignment>>
        get() = _assignments

    // 💡 STATE FILTER AKTIF (Default: Juli / Bulan ke-7 dan Tahun 2026 sesuai image_de0505.png)
    val selectedMonth = MutableLiveData<Int>(0)
    val selectedYear = MutableLiveData<Int>(0)

    // 🎯 LIVEDATA UTAMA HASIL FILTER GABUNGAN (Bulan + Tahun) UNTUK RECYCLERVIEW FRAGMENT
    val filteredSchedules = MediatorLiveData<List<Schedule>>()

    private val _burnoutAnalysis = MutableLiveData<String>()
    val burnoutAnalysis: LiveData<String>
        get() = _burnoutAnalysis

    private val _companyName = MutableLiveData<String>()
    val companyName: LiveData<String>
        get() = _companyName

    val availableYears = MediatorLiveData<List<String>>()

    init {
        filteredSchedules.addSource(_schedules) { combineAndFilterTime() }
        filteredSchedules.addSource(selectedMonth) { combineAndFilterTime() }
        filteredSchedules.addSource(selectedYear) { combineAndFilterTime() }

        availableYears.addSource(_schedules) { currentSchedules ->
            if (currentSchedules.isNullOrEmpty()) {
                availableYears.value = listOf("Semua Tahun")
            } else {
                val cal = Calendar.getInstance()
                val yearsSet = currentSchedules.map { schedule ->
                    cal.timeInMillis = schedule.start_time
                    cal.get(Calendar.YEAR)
                }.toSet()

                if (yearsSet.isNotEmpty()) {
                    val minYear = yearsSet.minOrNull() ?: 2026
                    val maxYear = yearsSet.maxOrNull() ?: 2026

                    val dynamicYearsList = ArrayList<String>()
                    dynamicYearsList.add("Semua Tahun")
                    for (year in minYear..maxYear) {
                        dynamicYearsList.add(year.toString())
                    }
                    availableYears.value = dynamicYearsList
                } else {
                    availableYears.value = listOf("Semua Tahun")
                }
            }
        }
    }

    // ⚙️ FUNGSI INTI PENYARINGAN WAKTU SECARA REAKTIF
    private fun combineAndFilterTime() {
        val currentSchedules = _schedules.value ?: return
        val month = selectedMonth.value ?: 0
        val year = selectedYear.value ?: 0

        val cal = Calendar.getInstance()

        val resultFiltered = currentSchedules.filter { schedule ->
            try {
                cal.timeInMillis = schedule.start_time

                val scheduleMonth = cal.get(Calendar.MONTH) + 1
                val scheduleYear = cal.get(Calendar.YEAR)

                val isMonthMatch = (month == 0) || (scheduleMonth == month)
                val isYearMatch = (year == 0) || (scheduleYear == year)

                isMonthMatch && isYearMatch
            } catch (e: Exception) {
                false
            }
        }
        filteredSchedules.value = resultFiltered
    }

    // 💡 Pemicu perubahan filter dari Dropdown Fragment
    fun updateDateFilter(monthId: Int, year: Int) {
        selectedMonth.value = monthId
        selectedYear.value = year
    }



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

    fun loadAnnouncements(companyId: Int) {
        Log.d("LOAD_ANNOUNCEMENT", "loadAnnouncements DIPANGGIL, companyId=$companyId")
        viewModelScope.launch {
            try {
                val result = userRepository.getAllUser()
                Log.d("TRACK_USER", "Data user dari repository: $result")
                _users.postValue(result ?: emptyList())
                val userList = _users.value ?: emptyList()
                Log.d("LOAD_ANNOUNCEMENT", "Jumlah user ter-load: ${userList.size}")
                Log.d("LOAD_ANNOUNCEMENT", "Isi userList: $userList")

                val userMap = userList.associateBy { it.id }
                Log.d("LOAD_ANNOUNCEMENT", "Isi userMap keys: ${userMap.keys}")

                val response = RetrofitClient.apiService.getAllAnnouncements()
                val responseList = response.body()
                Log.d("LOAD_ANNOUNCEMENT", "Jumlah announcement dari server: ${responseList?.size}")

                if (responseList != null && responseList.isNotEmpty()) {
                    val demoList = responseList.mapNotNull { announcementJson ->
                        Log.d("LOAD_ANNOUNCEMENT", "Mencari user id=${announcementJson.created_by} di userMap")
                        val matchedUser = userMap[announcementJson.created_by]
                        Log.d("LOAD_ANNOUNCEMENT", "User ditemukan: $matchedUser")

                        // Skip kalau user tidak ditemukan sama sekali
                        if (matchedUser == null) {
                            Log.d("LOAD_ANNOUNCEMENT", "Announcement id=${announcementJson.id} di-skip, created_by tidak match user manapun")
                            return@mapNotNull null
                        }

                        // Skip kalau user ditemukan tapi company_id-nya beda
                        if (matchedUser.company_id != companyId) {
                            Log.d("LOAD_ANNOUNCEMENT", "Announcement id=${announcementJson.id} di-skip, company_id user (${matchedUser.company_id}) tidak sama dengan companyId ($companyId)")
                            return@mapNotNull null
                        }

                        Announcement(
                            id = announcementJson.id,
                            title = announcementJson.title,
                            message = announcementJson.message,
                            created_by = announcementJson.created_by,
                            created_at = announcementJson.created_at
                        ).apply {
                            authorName = matchedUser.full_name
                            Log.d("LOAD_ANNOUNCEMENT", "authorName di-set jadi: $authorName")
                        }
                    }
                    Log.d("LOAD_ANNOUNCEMENT", "demoList final: $demoList")
                    _announcements.value = demoList
                } else {
                    _announcements.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("LOAD_ANNOUNCEMENT", "Gagal load data demo: ${e.message}")
                _announcements.value = emptyList()
            }
        }
    }

    fun InsertAnnouncement(announcement: Announcement,companyId: Int) {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.apiService.insertAnnouncements(announcement)
                if (result != null) {
                    Log.d("INSERT_ANNOUNCEMENT", "Berhasil insert: $result")
                    loadAnnouncements(companyId)
                } else {
                    Log.e("INSERT_ANNOUNCEMENT", "Gagal insert, response body null")
                }
            } catch (e: Exception) {
                Log.e("INSERT_ANNOUNCEMENT", "Gagal insert announcement: ${e.message}")
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
        Log.d("TRACK_SCHEDULE", "========================================")
        Log.d("TRACK_SCHEDULE", "🔄 loadSchedules() dipicu untuk Company ID: $companyId")

        viewModelScope.launch {
            try {
                val result = scheduleRepository.getByCompanyId(companyId)
                Log.d("TRACK_SCHEDULE", "✅ BERHASIL! Mendapatkan ${result?.size ?: 0} data dari repositori.")

                _schedules.postValue(result ?: emptyList())
            } catch (e: Exception) {
                Log.e("TRACK_SCHEDULE", "❌ GAGAL mengambil jadwal karena Error: ${e.javaClass.simpleName} -> ${e.message}")
                e.printStackTrace()
                _schedules.postValue(emptyList())
            }
            Log.d("TRACK_SCHEDULE", "========================================")
        }
    }

    fun loadStaffByIsActive(companyId: Int) {
        Log.d("TRACK_USER", "========================================")
        Log.d("TRACK_USER", "🔄 loadStaffByCompanyId() dipicu untuk Company ID: $companyId")

        viewModelScope.launch {
            try {
                val result = userRepository.getUserByCompanyId(companyId)
                _users.postValue(result ?: emptyList())
            } catch (e: Exception) {
                android.util.Log.e("TRACK_SCHEDULE", "❌ GAGAL: ${e.message}")
                _users.postValue(emptyList())
            }
        }
    }

    fun loadUserByCompanyId(companyId: Int) {
        Log.d("TRACK_USER", "========================================")
        viewModelScope.launch {
            try {
                val result = userRepository.getUserByCompanyId(companyId)
                Log.d("TRACK_USER", "Data user dari repository: $result")
                _users.postValue(result ?: emptyList())
            } catch (e: Exception) {
                _users.postValue(emptyList())
            }
        }
    }

    suspend fun loadUsers() {
        Log.d("TRACK_USER", "========================================")
        try {
            val result = userRepository.getAllUser()
            Log.d("TRACK_USER", "Data user dari repository: $result")
            _users.postValue(result ?: emptyList())
        } catch (e: Exception) {
            Log.e("TRACK_USER", "Gagal fetch user: ${e.message}")
            _users.postValue(emptyList())
        }
    }

    fun loadAttendanceByCompanyId(companyId: Int) {
        Log.d("TRACK_ATTENDANCE", "========================================")
        viewModelScope.launch {
            try {
                val result = attendanceRepository.getAttendanceByCompanyId(companyId)
                _attendances.postValue(result ?: emptyList())
            } catch (e: Exception) {
                _attendances.postValue(emptyList())
            }
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

    fun addUser(user: User, companyId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.insertUser(user)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun updateSchedule(schedule: Schedule, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                scheduleRepository.update(schedule)
                onResult(true)
            } catch (e: Exception) {
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
                onResult(false)
            }
        }
    }

    fun deleteSchedule(id: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                scheduleRepository.delete(id)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteUser(id: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.deleteUser(id)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun assignStaffToSchedule(scheduleId: Int, staffId: Int, roleInEvent: String, jobDesc: String, dateMillis: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Gunakan langsung constructor data class Assignment asli kamumu!
                val newAssignment = Assignment(
                    id = 0,
                    schedule_id = scheduleId,
                    user_id = staffId,
                    role_in_event = roleInEvent,
                    job_desc = jobDesc,
                    status = "pending",
                    assigned_at = dateMillis // Kirim data Long milidetik murni
                )

                val response = RetrofitClient.apiService.insertAssignments(newAssignment)
                if (response.isSuccessful && response.body() != null) {
                    onResult(true)
                } else {
                    android.util.Log.e("TRACK_ASSIGNMENT", "Server menolak: ${response.errorBody()?.string()}")
                    onResult(false)
                }
            } catch (e: Exception) {
                android.util.Log.e("TRACK_ASSIGNMENT", "Crash Jaringan: ${e.message}")
                onResult(false)
            }
        }
    }

    fun addAnnoouncement(user: User, companyId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.insertUser(user)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}