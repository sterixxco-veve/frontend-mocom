package com.example.myapplication.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    // 1. Inisialisasi Model Gemini (Gunakan Pro untuk performa terbaik)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "gen-lang-client-0990912019"
    )

    private val _burnoutRecommendations = MutableStateFlow<String>("Memuat analisis beban kerja asisten...")
    val burnoutRecommendations: StateFlow<String> = _burnoutRecommendations

    // Simulasi data asisten & jam mengajar per minggu untuk dimasukkan ke prompt
    private val mockStaffScheduleData = """
        [
          {"nama": "Andi Wijaya", "matkul": "Struktur Data", "jam_per_minggu": 18},
          {"nama": "Siti Rahma", "matkul": "Pemrograman Dasar", "jam_per_minggu": 24},
          {"nama": "Budi Santoso", "matkul": "Kecerdasan Buatan", "jam_per_minggu": 8},
          {"nama": "Rian Ardiansyah", "matkul": "Jaringan Komputer", "jam_per_minggu": 21}
        ]
    """.trimIndent()

    fun fetchBurnoutAnalysis() {
        viewModelScope.launch {
            try {
                _burnoutRecommendations.value = "Gemini sedang menganalisis tingkat stres asdos..."

                val prompt = """
                    Kamu adalah sistem AI EduStaff Pro. Analisis data asisten dosen berikut:
                    $mockStaffScheduleData
                    
                    Batasi rekomendasi maksimal 20 jam kerja per minggu agar tidak burnout. 
                    Berikan output terformat rapi berupa:
                    1. Urutan risiko burnout asisten dari yang tertinggi ke terendah disertai tingkat skalanya (1-10).
                    2. Rekomendasi konkret asisten mana yang harus ditugaskan ulang/dikurangi jadwalnya dan asisten cadangan potensial.
                    Bahasa Indonesia, profesional dan ringkas untuk dibaca Admin di dashboard.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                _burnoutRecommendations.value = response.text ?: "Gagal mendapatkan rekomendasi AI."
            } catch (e: Exception) {
                _burnoutRecommendations.value = "Error koneksi Gemini API: ${e.message}"
            }
        }
    }
}