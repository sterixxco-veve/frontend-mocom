package com.example.myapplication.ui.admin.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import android.widget.Toast
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.databinding.FragmentAdminReportsBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminReportsFragment : Fragment(R.layout.fragment_admin_reports) {

    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by viewModels {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository, app.attendanceRepository)
    }

    private var currentCompanyId: Int = 1
    private var currentCompanyName: String = ""
    private var usersList: List<User> = emptyList()
    private var attendanceList: List<Attendance> = emptyList()
    
    // Variabel lokal untuk relasi tabel database & filter staff
    private var schedulesList: List<Schedule> = emptyList()
    private var assignmentsList: List<com.example.myapplication.data.sources.local.entities.AssignmentEntity> = emptyList()
    private var selectedStaff: User? = null
    private var filteredStaffList: List<User> = emptyList()

    // Status pratinjau & range tanggal
    enum class ReportType { ATTENDANCE, BURNOUT }
    private var activeReportType: ReportType? = null
    private var startDateMs: Long = 0L
    private var endDateMs: Long = 0L

    private fun initDefaultDates() {
        val calendar = java.util.Calendar.getInstance()
        
        // Start of today (00:00:00.000)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        startDateMs = calendar.timeInMillis

        // End of today (23:59:59.999)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        endDateMs = calendar.timeInMillis
    }

    private fun updateDateButtonsText() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.btnSelectStartDate.text = "📅 Mulai: ${sdf.format(Date(startDateMs))}"
        binding.btnSelectEndDate.text = "📅 Selesai: ${sdf.format(Date(endDateMs))}"
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = if (isStartDate) startDateMs else endDateMs
        
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCal = java.util.Calendar.getInstance()
                selectedCal.set(java.util.Calendar.YEAR, selectedYear)
                selectedCal.set(java.util.Calendar.MONTH, selectedMonth)
                selectedCal.set(java.util.Calendar.DAY_OF_MONTH, selectedDay)
                
                if (isStartDate) {
                    selectedCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    selectedCal.set(java.util.Calendar.MINUTE, 0)
                    selectedCal.set(java.util.Calendar.SECOND, 0)
                    selectedCal.set(java.util.Calendar.MILLISECOND, 0)
                    startDateMs = selectedCal.timeInMillis
                } else {
                    selectedCal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                    selectedCal.set(java.util.Calendar.MINUTE, 59)
                    selectedCal.set(java.util.Calendar.SECOND, 59)
                    selectedCal.set(java.util.Calendar.MILLISECOND, 999)
                    endDateMs = selectedCal.timeInMillis
                }
                updateDateButtonsText()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminReportsBinding.bind(view)

        // Inisialisasi tanggal default ke hari ini
        initDefaultDates()
        updateDateButtonsText()

        // Mengambil data Company ID aktif dari SharedPreferences session
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", Context.MODE_PRIVATE)
        currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", -1).let {
            if (it != -1) it else sharedPref.getInt("LOGIN_COMPANY_ID", 1)
        }

        // 1. Observe data staff terdaftar & populate filter dropdown
        viewModel.users.observe(viewLifecycleOwner) { listUsers ->
            usersList = listUsers ?: emptyList()
            
            // Saring staff yang bukan superadmin (role_id != 1)
            filteredStaffList = usersList.filter { it.role_id != 1 }
            
            val staffNames = mutableListOf("Semua Staff / Member")
            staffNames.addAll(filteredStaffList.map { it.full_name })

            val dropdownAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                staffNames
            )
            binding.actvSelectStaff.setAdapter(dropdownAdapter)

            // Sinkronisasi assignments untuk staff ini dari server API ke Room DB secara asinkron
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val db = com.example.myapplication.data.sources.local.database.AppDatabase.getInstance(requireContext())
                    android.util.Log.d("REPORTS_ASSIGNMENT", "Syncing assignments for ${usersList.size} users...")
                    usersList.forEach { user ->
                        try {
                            android.util.Log.d("REPORTS_ASSIGNMENT", "Fetching assignments for User: ${user.full_name} (ID: ${user.id})...")
                            val responseList = com.example.myapplication.RetrofitClient.webService.getAssignmentByUserId(user.id)
                            android.util.Log.d("REPORTS_ASSIGNMENT", "User ID: ${user.id} -> Fetched ${responseList.size} assignments.")
                            val entities = responseList.map { 
                                com.example.myapplication.data.sources.local.entities.AssignmentEntity.fromRawModel(it.toAssignment())
                            }
                            db.assignmentDao().insertAllAssignments(entities)
                        } catch (e: java.lang.Exception) {
                            android.util.Log.e("REPORTS_ASSIGNMENT", "Failed fetching assignments for User ID: ${user.id}", e)
                            e.printStackTrace()
                        }
                    }
                    assignmentsList = db.assignmentDao().getAllAssignments()
                    android.util.Log.d("REPORTS_ASSIGNMENT", "Assignments sync complete. Total in DB: ${assignmentsList.size}")
                } catch (e: java.lang.Exception) {
                    android.util.Log.e("REPORTS_ASSIGNMENT", "Failed overall assignments sync", e)
                    e.printStackTrace()
                }
            }
        }

        binding.actvSelectStaff.setText("Semua Staff / Member", false)
        binding.actvSelectStaff.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                selectedStaff = null
            } else {
                selectedStaff = filteredStaffList.getOrNull(position - 1)
            }
        }

        // 2. Observe data absensi
        viewModel.attendances.observe(viewLifecycleOwner) {
            attendanceList = it ?: emptyList()
        }

        // Observe data jadwal (schedules)
        viewModel.schedules.observe(viewLifecycleOwner) {
            schedulesList = it ?: emptyList()
        }

        // Observe nama perusahaan
        viewModel.companyName.observe(viewLifecycleOwner) {
            currentCompanyName = it ?: "Perusahaan #$currentCompanyId"
        }

        // 3. Observe hasil rekomendasi Gemini AI untuk Burnout
        viewModel.burnoutAnalysis.observe(viewLifecycleOwner) { analysisText ->
            if (analysisText.isNotEmpty() && analysisText != "Sedang memuat data dan menganalisis..." && analysisText != "Tidak ada data absensi untuk dianalisis pada rentang tanggal tersebut.") {
                binding.btnPreviewBurnout.isEnabled = true
                binding.btnPreviewBurnout.text = "🔥 Tinjau Analisis Burnout AI"
                
                binding.tvPdfPlaceholder.visibility = View.GONE
                binding.svPdfContent.visibility = View.VISIBLE
                binding.tvPdfDocTitle.text = "ANALISIS BURNOUT STAFF AI"
                binding.tvPdfDocBody.text = analysisText
                
                // Tampilkan tombol download
                binding.btnDownloadPdf.visibility = View.VISIBLE
                activeReportType = ReportType.BURNOUT
            } else if (analysisText == "Sedang memuat data dan menganalisis...") {
                binding.btnPreviewBurnout.isEnabled = false
                binding.btnPreviewBurnout.text = "Menganalisis..."
            } else if (analysisText.isNotEmpty()) {
                binding.btnPreviewBurnout.isEnabled = true
                binding.btnPreviewBurnout.text = "🔥 Tinjau Analisis Burnout AI"
                Toast.makeText(context, analysisText, Toast.LENGTH_LONG).show()
                binding.tvPdfPlaceholder.visibility = View.VISIBLE
                binding.svPdfContent.visibility = View.GONE
                binding.btnDownloadPdf.visibility = View.GONE
                activeReportType = null
            }
        }

        // Load assignments secara lokal dari Room DB
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val db = com.example.myapplication.data.sources.local.database.AppDatabase.getInstance(requireContext())
                assignmentsList = db.assignmentDao().getAllAssignments()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Muat data awal
        viewModel.loadUserByCompanyId(currentCompanyId)
        viewModel.loadAttendanceByCompanyId(currentCompanyId)
        viewModel.loadSchedules(currentCompanyId)
        viewModel.loadCompanyName(currentCompanyId)

        // Klik pemilih tanggal
        binding.btnSelectStartDate.setOnClickListener {
            showDatePicker(isStartDate = true)
        }
        binding.btnSelectEndDate.setOnClickListener {
            showDatePicker(isStartDate = false)
        }

        // Aksi tombol Tinjau Laporan Kehadiran (Preview saja)
        binding.btnPreviewAttendance.setOnClickListener {
            // Saring data absensi berdasarkan range tanggal & pilihan staff
            val filteredAttendance = attendanceList.filter { att ->
                val dateInRange = att.check_in in startDateMs..endDateMs
                val matchesStaff = if (selectedStaff != null) {
                    val assignment = assignmentsList.find { it.id == att.assignment_id }
                    assignment != null && assignment.user_id == selectedStaff!!.id
                } else {
                    true
                }
                dateInRange && matchesStaff
            }
            
            if (filteredAttendance.isEmpty()) {
                Toast.makeText(context, "Tidak ada data absensi untuk kriteria yang dipilih.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reportHtml = buildAttendanceReportHtml(usersList, filteredAttendance)

            // Tampilkan laporan di integrated viewer aplikasi dengan perenderan HTML
            binding.tvPdfPlaceholder.visibility = View.GONE
            binding.svPdfContent.visibility = View.VISIBLE
            binding.tvPdfDocTitle.text = "LAPORAN KEHADIRAN STAFF"
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                binding.tvPdfDocBody.text = Html.fromHtml(reportHtml, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION")
                binding.tvPdfDocBody.text = Html.fromHtml(reportHtml)
            }

            // Tampilkan tombol unduh PDF
            binding.btnDownloadPdf.visibility = View.VISIBLE
            activeReportType = ReportType.ATTENDANCE
        }

        // Aksi tombol Tinjau Analisis Burnout AI (Preview saja dengan custom prompt yang terfilter)
        binding.btnPreviewBurnout.setOnClickListener {
            val filteredStaffForAi = if (selectedStaff != null) {
                listOf(selectedStaff!!)
            } else {
                usersList.filter { it.role_id != 1 } // excluding superadmin
            }

            val filteredAttendanceForAi = attendanceList.filter { att ->
                val dateInRange = att.check_in in startDateMs..endDateMs
                val matchesStaff = if (selectedStaff != null) {
                    val assignment = assignmentsList.find { it.id == att.assignment_id }
                    assignment != null && assignment.user_id == selectedStaff!!.id
                } else {
                    true
                }
                dateInRange && matchesStaff
            }

            if (filteredAttendanceForAi.isEmpty()) {
                Toast.makeText(context, "Tidak ada data absensi untuk dianalisis oleh AI.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val promptBuilder = StringBuilder()
            promptBuilder.append("Analisis tingkat burnout staff berikut:\n\n")
            
            // 1. Informasi staff
            promptBuilder.append("Daftar Staff Aktif:\n")
            filteredStaffForAi.forEach { user ->
                promptBuilder.append("- ID #${user.id}: ${user.full_name} (Status: ${if (user.is_active == 1) "Aktif" else "Nonaktif"})\n")
            }

            // 2. Riwayat Kehadiran
            promptBuilder.append("\nRiwayat Kehadiran:\n")
            val sdfTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")
            }
            
            filteredAttendanceForAi.forEach { att ->
                val checkInStr = if (att.check_in > 0) sdfTime.format(Date(att.check_in)) else "--"
                val checkOutStr = if (att.check_out > 0) sdfTime.format(Date(att.check_out)) else "--"
                
                // Cari nama staff & job desc assignment
                val assignment = assignmentsList.find { it.id == att.assignment_id }
                val staffName = filteredStaffForAi.find { it.id == assignment?.user_id }?.full_name 
                    ?: usersList.find { it.id == assignment?.user_id }?.full_name 
                    ?: "Staff #${assignment?.user_id ?: att.assignment_id}"
                
                val jobDesc = assignment?.job_desc ?: "Tugas Dinas"
                
                promptBuilder.append("- Staff: $staffName | Keperluan: $jobDesc | Status=${att.status} | CheckIn=$checkInStr | CheckOut=$checkOutStr\n")
            }

            promptBuilder.append("\nHarap berikan analisis dalam Bahasa Indonesia yang mencakup:\n")
            promptBuilder.append("1. Ringkasan singkat statistik kehadiran (berapa persen Present, Late, Absent).\n")
            promptBuilder.append("2. Deteksi staff yang memiliki indikasi burnout (terlalu sering terlambat/absen atau pola tidak sehat).\n")
            promptBuilder.append("3. Rekomendasi konkret bagi koordinator untuk meningkatkan kebugaran kerja staff.\n")
            promptBuilder.append("Berikan hasil dengan format dokumen resmi yang rapi tanpa menyertakan kode markdown seperti asteriks tebal berlebih, tapi gunakan spasi paragraf yang bagus.")

            viewModel.generateBurnoutAnalysis(promptBuilder.toString())
        }

        // Aksi tombol Unduh PDF hasil pratinjau aktif
        binding.btnDownloadPdf.setOnClickListener {
            val reportType = activeReportType
            if (reportType == null) {
                Toast.makeText(context, "Silakan pilih dan tinjau laporan terlebih dahulu.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (reportType) {
                ReportType.ATTENDANCE -> {
                    val filteredAttendance = attendanceList.filter { att ->
                        val dateInRange = att.check_in in startDateMs..endDateMs
                        val matchesStaff = if (selectedStaff != null) {
                            val assignment = assignmentsList.find { it.id == att.assignment_id }
                            assignment != null && assignment.user_id == selectedStaff!!.id
                        } else {
                            true
                        }
                        dateInRange && matchesStaff
                    }
                    if (filteredAttendance.isEmpty()) {
                        Toast.makeText(context, "Tidak ada data absensi untuk diunduh.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    generateAttendancePdfAndSave(
                        fileName = "Laporan_Kehadiran_EduStaffPro.pdf",
                        docTitle = "LAPORAN KEHADIRAN EDUSTAFF PRO",
                        users = usersList,
                        attendances = filteredAttendance
                    )
                }
                ReportType.BURNOUT -> {
                    val analysisText = binding.tvPdfDocBody.text.toString()
                    if (analysisText.isEmpty() || analysisText == "Detail Laporan...") {
                        Toast.makeText(context, "Konten analisis kosong.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    generateTextPdfAndSave(
                        fileName = "Analisis_Burnout_Staff_Absence.pdf",
                        docTitle = "ANALISIS BURNOUT STAFF ABSENCE",
                        content = analysisText
                    )
                }
            }
        }
    }

    private fun buildAttendanceReportHtml(users: List<User>, attendances: List<Attendance>): String {
        val total = attendances.size
        val presentCount = attendances.count { it.status.lowercase(Locale.getDefault()) == "present" }
        val lateCount = attendances.count { it.status.lowercase(Locale.getDefault()) == "late" }
        val absentCount = attendances.count { it.status.lowercase(Locale.getDefault()) == "absent" }
        
        val presentPct = if (total > 0) (presentCount * 100) / total else 0
        val latePct = if (total > 0) (lateCount * 100) / total else 0
        val absentPct = if (total > 0) (absentCount * 100) / total else 0
        
        val html = StringBuilder()
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        html.append("<p><b>Tanggal Cetak:</b> ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}<br>")
        html.append("<b>Periode Laporan:</b> ${sdfDate.format(Date(startDateMs))} - ${sdfDate.format(Date(endDateMs))}<br>")
        html.append("<b>Perusahaan:</b> $currentCompanyName (ID: $currentCompanyId)</p><br>")
        
        html.append("<h3>I. RINGKASAN STATISTIK KEHADIRAN</h3>")
        html.append("<p>")
        html.append("• <b>Total Catatan Absensi:</b> $total<br>")
        html.append("• <font color='#2E7D32'><b>Hadir (Present):</b> $presentCount ($presentPct%)</font><br>")
        html.append("• <font color='#E65100'><b>Terlambat (Late):</b> $lateCount ($latePct%)</font><br>")
        html.append("• <font color='#C62828'><b>Absen (Absent):</b> $absentCount ($absentPct%)</font>")
        html.append("</p>")
        
        html.append("<br><br><h3>II. DAFTAR STAFF AKTIF</h3>")
        val assistants = users.filter { it.role_id == 2 }
        if (assistants.isEmpty()) {
            html.append("<p><i>Tidak ada staff aktif terdaftar.</i></p>")
        } else {
            html.append("<ul>")
            assistants.forEach { user ->
                html.append("<li>&nbsp;<b>${user.full_name}</b> (@${user.username}) - ${user.email}</li>")
            }
            html.append("</ul>")
        }
        
        html.append("<br><br><h3>III. LOG KEHADIRAN STAFF RINCI</h3>")
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")
        }
        
        attendances.forEachIndexed { index, att ->
            val checkInStr = if (att.check_in > 0) sdf.format(Date(att.check_in)) else "--"
            val checkOutStr = if (att.check_out > 0) sdf.format(Date(att.check_out)) else "--"
            
            // Resolve staff name & job desc assignment
            val assignment = assignmentsList.find { it.id == att.assignment_id }
            android.util.Log.d("REPORTS_HTML", "Finding assignment for ID: ${att.assignment_id}. Result: ${if (assignment != null) "Found (User ID: ${assignment.user_id}, Job: ${assignment.job_desc})" else "NULL"}")
            
            val staffName = users.find { it.id == assignment?.user_id }?.full_name ?: "Staff #${assignment?.user_id ?: att.assignment_id}"
            val jobDesc = assignment?.job_desc ?: "Tugas Dinas"
            
            val color = when (att.status.lowercase(Locale.getDefault())) {
                "present" -> "#2E7D32"
                "late" -> "#E65100"
                "absent" -> "#C62828"
                else -> "#475569"
            }
            
            html.append("<div style='border: 1px solid #E2E8F0; padding:10px; margin-bottom:8px; border-radius:6px;'>")
            html.append("<b>Catatan Absensi #${index + 1}</b><br>")
            html.append("Staff: <b>$staffName</b><br>")
            html.append("Keperluan: <b>$jobDesc</b> (Penugasan: #${att.assignment_id})<br>")
            html.append("Masuk: $checkInStr WIB<br>")
            html.append("Keluar: $checkOutStr WIB<br>")
            html.append("Status: <font color='$color'><b>${att.status.uppercase(Locale.getDefault())}</b></font>")
            html.append("</div><br>")
        }
        
        return html.toString()
    }

    private fun generateAttendancePdfAndSave(fileName: String, docTitle: String, users: List<User>, attendances: List<Attendance>) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f
            
            // Perhitungan Statistik
            val total = attendances.size
            val presentCount = attendances.count { it.status.lowercase(Locale.getDefault()) == "present" }
            val lateCount = attendances.count { it.status.lowercase(Locale.getDefault()) == "late" }
            val absentCount = attendances.count { it.status.lowercase(Locale.getDefault()) == "absent" }
            
            val presentPct = if (total > 0) (presentCount * 100) / total else 0
            val latePct = if (total > 0) (lateCount * 100) / total else 0
            val absentPct = if (total > 0) (absentCount * 100) / total else 0

            // Page 1
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            // 1. Draw Header Banner (Blue Premium)
            paint.color = Color.parseColor("#4361EE")
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, paint)

            // Header Teks Putih
            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.isFakeBoldText = true
            canvas.drawText("EDUSTAFF PRO ABSENCE REPORT", margin, 35f, paint)

            paint.textSize = 18f
            canvas.drawText(docTitle, margin, 65f, paint)

            paint.textSize = 9f
            paint.isFakeBoldText = false
            val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateRangeStr = "${sdfDate.format(Date(startDateMs))} - ${sdfDate.format(Date(endDateMs))}"
            canvas.drawText("Perusahaan: $currentCompanyName | Periode: $dateRangeStr", margin, 90f, paint)

            // 2. Draw Statistik Rekapitulasi (3 Cards)
            val cardWidth = 158f
            val cardHeight = 60f
            val cardY = 130f
            val cardGap = 20f

            // Card 1: Present (Green Accent)
            drawStatCard(canvas, margin, cardY, cardWidth, cardHeight, "Hadir (Present)", "$presentCount ($presentPct%)", "#2E7D32", "#E8F5E9")

            // Card 2: Late (Orange Accent)
            drawStatCard(canvas, margin + cardWidth + cardGap, cardY, cardWidth, cardHeight, "Terlambat (Late)", "$lateCount ($latePct%)", "#E65100", "#FFF3E0")

            // Card 3: Absent (Red Accent)
            drawStatCard(canvas, margin + (cardWidth + cardGap) * 2, cardY, cardWidth, cardHeight, "Absen (Absent)", "$absentCount ($absentPct%)", "#C62828", "#FFEBEE")

            // 3. Draw Section Title: Log Kehadiran Staff
            paint.color = Color.parseColor("#1E1E24")
            paint.textSize = 11f
            paint.isFakeBoldText = true
            canvas.drawText("TABEL LOG KEHADIRAN STAFF (TOTAL: $total)", margin, 220f, paint)

            // Garis tipis pembatas
            paint.color = Color.parseColor("#E2E8F0")
            paint.strokeWidth = 1f
            canvas.drawLine(margin, 230f, pageWidth - margin, 230f, paint)

            // 4. Draw Table Log
            var currentY = 245f
            val rowHeight = 30f
            val colWidths = floatArrayOf(110f, 125f, 95f, 95f, 90f) // STAFF, KEPERLUAN, MASUK, KELUAR, STATUS

            // Draw Table Header Background
            paint.color = Color.parseColor("#EEF2F6")
            canvas.drawRect(margin, currentY, pageWidth - margin, currentY + rowHeight, paint)

            // Draw Table Header Text
            paint.color = Color.parseColor("#475569")
            paint.textSize = 9f
            paint.isFakeBoldText = true
            
            var currentX = margin
            canvas.drawText("STAFF", currentX + 5f, currentY + 18f, paint)
            currentX += colWidths[0]
            canvas.drawText("KEPERLUAN", currentX + 5f, currentY + 18f, paint)
            currentX += colWidths[1]
            canvas.drawText("MASUK", currentX + 5f, currentY + 18f, paint)
            currentX += colWidths[2]
            canvas.drawText("KELUAR", currentX + 5f, currentY + 18f, paint)
            currentX += colWidths[3]
            canvas.drawText("STATUS", currentX + 5f, currentY + 18f, paint)

            // Draw Table Rows
            paint.isFakeBoldText = false
            paint.textSize = 8.5f
            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")
            }

            attendances.forEachIndexed { index, att ->
                // Untuk demo visual, kita hanya mencetak log yang masuk dalam batas 1 halaman (max ~18 baris)
                if (currentY + rowHeight < pageHeight - 50f) {
                    currentY += rowHeight
                    
                    // Draw background row alternating
                    if (index % 2 == 1) {
                        paint.color = Color.parseColor("#F8FAFC")
                        canvas.drawRect(margin, currentY, pageWidth - margin, currentY + rowHeight, paint)
                    }

                    // Draw cell borders (bottom line)
                    paint.color = Color.parseColor("#F1F5F9")
                    paint.strokeWidth = 0.5f
                    canvas.drawLine(margin, currentY + rowHeight, pageWidth - margin, currentY + rowHeight, paint)

                    // Resolve name and job desc assignment
                    val assignment = assignmentsList.find { it.id == att.assignment_id }
                    val rawStaffName = users.find { it.id == assignment?.user_id }?.full_name ?: "Staff #${assignment?.user_id ?: att.assignment_id}"
                    val rawJobDesc = assignment?.job_desc ?: "Tugas Dinas"
                    
                    val staffName = if (rawStaffName.length > 18) rawStaffName.substring(0, 16) + ".." else rawStaffName
                    val jobDesc = if (rawJobDesc.length > 20) rawJobDesc.substring(0, 18) + ".." else rawJobDesc

                    val checkInStr = if (att.check_in > 0) sdf.format(Date(att.check_in)) else "--:--"
                    val checkOutStr = if (att.check_out > 0) sdf.format(Date(att.check_out)) else "--:--"

                    paint.color = Color.parseColor("#1E1E24")
                    currentX = margin
                    
                    // Staff
                    canvas.drawText(staffName, currentX + 5f, currentY + 18f, paint)
                    currentX += colWidths[0]
                    
                    // Keperluan
                    canvas.drawText(jobDesc, currentX + 5f, currentY + 18f, paint)
                    currentX += colWidths[1]
                    
                    // Waktu Masuk
                    canvas.drawText(checkInStr, currentX + 5f, currentY + 18f, paint)
                    currentX += colWidths[2]
                    
                    // Waktu Keluar
                    canvas.drawText(checkOutStr, currentX + 5f, currentY + 18f, paint)
                    currentX += colWidths[3]
                    
                    // Status Badge (Draw colored rounded rect)
                    val statusText = att.status.uppercase(Locale.getDefault())
                    val badgeColor = when (statusText.lowercase(Locale.getDefault())) {
                        "present" -> "#2E7D32"
                        "late" -> "#E65100"
                        "absent" -> "#C62828"
                        else -> "#475569"
                    }
                    val badgeBg = when (statusText.lowercase(Locale.getDefault())) {
                        "present" -> "#E8F5E9"
                        "late" -> "#FFF3E0"
                        "absent" -> "#FFEBEE"
                        else -> "#F1F5F9"
                    }

                    val rectPaint = Paint().apply {
                        color = Color.parseColor(badgeBg)
                        style = Paint.Style.FILL
                    }
                    val textPaint = Paint().apply {
                        color = Color.parseColor(badgeColor)
                        textSize = 8f
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                    }
                    
                    val badgeLeft = currentX + 5f
                    val badgeTop = currentY + 6f
                    val badgeRight = badgeLeft + 60f
                    val badgeBottom = badgeTop + 18f
                    canvas.drawRoundRect(badgeLeft, badgeTop, badgeRight, badgeBottom, 4f, 4f, rectPaint)
                    canvas.drawText(statusText, badgeLeft + 30f, badgeTop + 12f, textPaint)
                }
            }

            pdfDocument.finishPage(page)

            // Simpan menggunakan MediaStore untuk Android 10+ (Scoped Storage) agar tidak butuh ijin runtime
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = requireContext().contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val outputStream = resolver.openOutputStream(uri)
                    if (outputStream != null) {
                        pdfDocument.writeTo(outputStream)
                        pdfDocument.close()
                        outputStream.close()
                        Toast.makeText(requireContext(), "Berhasil mengunduh berkas: $fileName di direktori Downloads", Toast.LENGTH_LONG).show()
                    } else {
                        pdfDocument.close()
                        throw java.io.IOException("Gagal membuka output stream")
                    }
                } else {
                    pdfDocument.close()
                    throw java.io.IOException("Gagal membuat entri MediaStore untuk Downloads")
                }
            } else {
                // Fallback untuk Android 9 kebawah
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                val fileOutputStream = FileOutputStream(file)
                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
                fileOutputStream.close()
                Toast.makeText(requireContext(), "Berhasil mengunduh berkas: $fileName di direktori Downloads", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal membuat berkas PDF: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun drawStatCard(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, title: String, value: String, accentColor: String, bgColor: String) {
        val rectPaint = Paint().apply {
            color = Color.parseColor(bgColor)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val accentBarPaint = Paint().apply {
            color = Color.parseColor(accentColor)
            style = Paint.Style.FILL
        }
        
        // Background card
        canvas.drawRoundRect(x, y, x + width, y + height, 6f, 6f, rectPaint)
        canvas.drawRoundRect(x, y, x + width, y + height, 6f, 6f, borderPaint)
        
        // Accent bar on the left (make rounded on the left corners)
        canvas.drawRoundRect(x, y, x + 6f, y + height, 6f, 6f, accentBarPaint)
        canvas.drawRect(x + 3f, y, x + 6f, y + height, accentBarPaint) // cover right curve
        
        // Title Text
        val textPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            isFakeBoldText = true
        }
        canvas.drawText(title, x + 15f, y + 22f, textPaint)
        
        // Value Text
        textPaint.color = Color.parseColor(accentColor)
        textPaint.textSize = 13f
        textPaint.isFakeBoldText = true
        canvas.drawText(value, x + 15f, y + 45f, textPaint)
    }

    private fun generateTextPdfAndSave(fileName: String, docTitle: String, content: String) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 50f
            val contentWidth = pageWidth - (2 * margin).toInt()

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Paint untuk Header & Title
            val headerPaint = TextPaint().apply {
                color = Color.parseColor("#8D99AE")
                textSize = 9f
                isFakeBoldText = true
            }

            val titlePaint = TextPaint().apply {
                color = Color.parseColor("#4361EE") // EduStaff Pro Blue
                textSize = 18f
                isFakeBoldText = true
            }

            val textPaint = TextPaint().apply {
                color = Color.parseColor("#1E1E24")
                textSize = 10f
            }

            // Draw header teks
            canvas.drawText("EDUSTAFF PRO ABSENCE REPORT", margin, 45f, headerPaint)
            canvas.drawText(docTitle, margin, 70f, titlePaint)
            
            // Draw periode range text
            val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val rangeStr = "Periode Laporan: ${sdfDate.format(Date(startDateMs))} - ${sdfDate.format(Date(endDateMs))}"
            val subTextPaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 9f
                isAntiAlias = true
            }
            canvas.drawText(rangeStr, margin, 88f, subTextPaint)

            // Garis pembatas biru
            val linePaint = Paint().apply {
                color = Color.parseColor("#4361EE")
                strokeWidth = 2f
            }
            canvas.drawLine(margin, 96f, pageWidth - margin, 96f, linePaint)

            // Render konten teks menggunakan StaticLayout agar otomatis bungkus baris (auto-wrap)
            canvas.save()
            canvas.translate(margin, 120f)
            
            val builder = StaticLayout.Builder.obtain(content, 0, content.length, textPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(3f, 1f)
                .setIncludePad(true)
            
            val staticLayout = builder.build()
            staticLayout.draw(canvas)
            canvas.restore()

            pdfDocument.finishPage(page)

            // Simpan menggunakan MediaStore untuk Android 10+ (Scoped Storage) agar tidak butuh ijin runtime
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = requireContext().contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val outputStream = resolver.openOutputStream(uri)
                    if (outputStream != null) {
                        pdfDocument.writeTo(outputStream)
                        pdfDocument.close()
                        outputStream.close()
                        Toast.makeText(requireContext(), "Berhasil mengunduh berkas: $fileName di direktori Downloads", Toast.LENGTH_LONG).show()
                    } else {
                        pdfDocument.close()
                        throw java.io.IOException("Gagal membuka output stream")
                    }
                } else {
                    pdfDocument.close()
                    throw java.io.IOException("Gagal membuat entri MediaStore untuk Downloads")
                }
            } else {
                // Fallback untuk Android 9 kebawah
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                val fileOutputStream = FileOutputStream(file)
                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
                fileOutputStream.close()
                Toast.makeText(requireContext(), "Berhasil mengunduh berkas: $fileName di direktori Downloads", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal membuat berkas PDF: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}