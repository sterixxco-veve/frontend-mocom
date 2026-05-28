package com.example.myapplication.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.ScheduleRepository
import com.example.myapplication.data.sources.models.Schedule
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminViewModel (
    private val scheduleRepository: ScheduleRepository
): ViewModel() {
    private val _scheduleList = ArrayList<Schedule>()
    private val _schedules = MutableLiveData(_scheduleList.toList())
    val schedules: LiveData<List<Schedule>>
        get() = _schedules

    private val _burnoutRecommendations = MutableStateFlow<String>("Memuat analisis beban kerja asisten...")
    val burnoutRecommendations: StateFlow<String> = _burnoutRecommendations

    // Inisialisasi Model Gemini SDK
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "gen-lang-client-0990912019"
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

    fun loadSchedules() {
        viewModelScope.launch {
            try {
                val data = scheduleRepository.getAll()
                _schedules.postValue(data)
            } catch (e: Exception) {
                e.printStackTrace()
                _schedules.postValue(emptyList()) // Tetap kirim list kosong jika server Node.js mati
            }
        }
    }

    fun addSchedule(schedule: Schedule, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Eksekusi insert ke MySQL lewat repository anonim yang ada di fragment
                val response = scheduleRepository.insert(schedule)

                // 2. Cek apakah server mengembalikan data dengan ID baru (sukses)
                // Jika sukses, MySQL akan mengembalikan id > 0 karena Auto_Increment
                if (response.id > 0) {
                    // Pindah ke Main Thread untuk mengembalikan status sukses ke UI (BottomSheet)
                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Jika terjadi crash jaringan atau server Node.js mati, kembalikan false
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun fetchBurnoutAnalysis() {
        viewModelScope.launch {
            try {
                _burnoutRecommendations.value = "Gemini sedang menganalisis tingkat stres asdos..."

                // 1. Mengambil data jadwal riil saat ini
                val currentSchedules = scheduleRepository.getAll()

                // 2. Format daftar jadwal menjadi teks terstruktur agar mudah dipahami oleh AI
                val staffScheduleDataText = if (currentSchedules.isEmpty()) {
                    "Tidak ada data jadwal praktikum aktif saat ini."
                } else {
                    currentSchedules.joinToString(separator = "\n") { schedule ->
                        "- Asisten ID: ${schedule.created_by}, Judul/Matkul: ${schedule.title}, Lokasi: ${schedule.location ?: "Tidak diketahui"}"
                    }
                }

                // 3. Bangun Prompt Pintar EduStaff Pro dengan data dinamis
                val prompt = """
                    Kamu adalah sistem AI terintegrasi dari proyek EduStaff Pro.
                    Analisis data aktivitas mengajar asisten dosen berikut yang diambil langsung dari database:

                    $staffScheduleDataText

                    Tugas utama kamu:
                    1. Berikan urutan perkiraan risiko burnout asisten dari yang tertinggi ke terendah disertai skala tingkat stres (1-10) berdasarkan frekuensi mengajar mereka di atas.
                    2. Berikan rekomendasi konkret tindakan pencegahan, seperti pembatasan maksimal jadwal mengajar atau saran penugasan asisten cadangan potensial.

                    Ketentuan Output:
                    - Menggunakan Bahasa Indonesia yang profesional, padat, dan instruktif.
                    - Format rapi menggunakan poin-poin Markdown agar mudah dibaca oleh Admin pada halaman dashboard aplikasi Android.
                """.trimIndent()

                // 4. Minta respons dari Google Gemini AI Server
                val response = generativeModel.generateContent(prompt)
                _burnoutRecommendations.value = response.text ?: "Gagal mendapatkan rekomendasi AI dari server."

            } catch (e: Exception) {
                e.printStackTrace()
                _burnoutRecommendations.value = "Error koneksi Gemini API: ${e.message}"
            }
        }
    }
}