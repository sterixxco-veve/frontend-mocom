package com.example.myapplication.ui.admin.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.Attendance
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
    private var usersList: List<User> = emptyList()
    private var attendanceList: List<Attendance> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminReportsBinding.bind(view)

        // Mengambil data Company ID aktif dari SharedPreferences session
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", Context.MODE_PRIVATE)
        currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", -1).let {
            if (it != -1) it else sharedPref.getInt("LOGIN_COMPANY_ID", 1)
        }

        // 1. Observe data asisten terdaftar
        viewModel.users.observe(viewLifecycleOwner) {
            usersList = it ?: emptyList()
        }

        // 2. Observe data absensi
        viewModel.attendances.observe(viewLifecycleOwner) {
            attendanceList = it ?: emptyList()
        }

        // 3. Observe hasil rekomendasi Gemini AI untuk Burnout
        viewModel.burnoutAnalysis.observe(viewLifecycleOwner) { analysisText ->
            if (analysisText.isNotEmpty() && analysisText != "Sedang memuat data dan menganalisis...") {
                binding.btnDownloadBurnout.isEnabled = true
                binding.btnDownloadBurnout.text = "🔥 Analisis Burnout AI (.pdf)"
                
                binding.tvPdfPlaceholder.visibility = View.GONE
                binding.svPdfContent.visibility = View.VISIBLE
                binding.tvPdfDocTitle.text = "ANALISIS BURNOUT AI"
                binding.tvPdfDocBody.text = analysisText
                
                // Buat PDF asli dan simpan secara lokal di folder Downloads
                generatePdfAndSave(
                    fileName = "Analisis_Burnout_Asdos_AI.pdf",
                    docTitle = "ANALISIS BURNOUT ASDOS AI",
                    content = analysisText
                )
            } else if (analysisText == "Sedang memuat data dan menganalisis...") {
                binding.btnDownloadBurnout.isEnabled = false
                binding.btnDownloadBurnout.text = "Menganalisis..."
            }
        }

        // Muat data awal
        viewModel.loadUserByCompanyId(currentCompanyId)
        viewModel.loadAttendanceByCompanyId(currentCompanyId)

        // Aksi tombol Laporan Kehadiran
        binding.btnDownloadAttendance.setOnClickListener {
            if (attendanceList.isEmpty()) {
                Toast.makeText(context, "Data absensi kosong. Tidak ada data untuk dijadikan laporan.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reportContent = buildAttendanceReportText(usersList, attendanceList)

            // Tampilkan laporan di integrated viewer aplikasi
            binding.tvPdfPlaceholder.visibility = View.GONE
            binding.svPdfContent.visibility = View.VISIBLE
            binding.tvPdfDocTitle.text = "LAPORAN KEHADIRAN ASISTEN"
            binding.tvPdfDocBody.text = reportContent

            // Buat PDF asli dan simpan secara lokal di folder Downloads
            generatePdfAndSave(
                fileName = "Laporan_Kehadiran_Asdos_Mocom.pdf",
                docTitle = "LAPORAN KEHADIRAN ASISTEN MOCOM",
                content = reportContent
            )
        }

        // Aksi tombol Analisis Burnout AI
        binding.btnDownloadBurnout.setOnClickListener {
            viewModel.generateBurnoutAnalysis(currentCompanyId)
        }
    }

    private fun buildAttendanceReportText(users: List<User>, attendances: List<Attendance>): String {
        val total = attendances.size
        val presentCount = attendances.count { it.status.lowercase(Locale.getDefault()) == "present" }
        val lateCount = attendances.count { it.status.lowercase(Locale.getDefault()) == "late" }
        val absentCount = attendances.count { it.status.lowercase(Locale.getDefault()) == "absent" }
        
        val presentPct = if (total > 0) (presentCount * 100) / total else 0
        val latePct = if (total > 0) (lateCount * 100) / total else 0
        val absentPct = if (total > 0) (absentCount * 100) / total else 0
        
        val report = StringBuilder()
        report.append("Tanggal Cetak: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        report.append("ID Perusahaan: $currentCompanyId\n\n")
        
        report.append("I. RINGKASAN STATISTIK KEHADIRAN\n")
        report.append("===========================================\n")
        report.append("Total Catatan Absensi : $total\n")
        report.append("Hadir (Present)       : $presentCount ($presentPct%)\n")
        report.append("Terlambat (Late)      : $lateCount ($latePct%)\n")
        report.append("Absen (Absent)        : $absentCount ($absentPct%)\n\n")
        
        report.append("II. DAFTAR ASISTEN AKTIF (ROLE #2)\n")
        report.append("===========================================\n")
        val assistants = users.filter { it.role_id == 2 }
        if (assistants.isEmpty()) {
            report.append("Tidak ada asisten terdaftar aktif.\n\n")
        } else {
            assistants.forEachIndexed { index, user ->
                report.append("${index + 1}. ${user.full_name} (@${user.username}) - ${user.email}\n")
            }
            report.append("\n")
        }
        
        report.append("III. LOG KEHADIRAN ASISTEN RINCI\n")
        report.append("===========================================\n")
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")
        }
        
        attendances.forEachIndexed { index, att ->
            val checkInStr = if (att.check_in > 0) sdf.format(Date(att.check_in)) else "--"
            val checkOutStr = if (att.check_out > 0) sdf.format(Date(att.check_out)) else "--"
            
            report.append("Catatan Absensi #${index + 1}\n")
            report.append("  Nomor Penugasan : #${att.assignment_id}\n")
            report.append("  Waktu Masuk     : $checkInStr WIB\n")
            report.append("  Waktu Keluar    : $checkOutStr WIB\n")
            report.append("  Status Kehadiran: ${att.status.uppercase(Locale.getDefault())}\n")
            report.append("-------------------------------------------\n")
        }
        
        return report.toString()
    }

    private fun generatePdfAndSave(fileName: String, docTitle: String, content: String) {
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
            canvas.drawText("MOCOM: EDUSTAFF PRO REPORT SYSTEM", margin, 45f, headerPaint)
            canvas.drawText(docTitle, margin, 75f, titlePaint)
            
            // Garis pembatas biru
            val linePaint = Paint().apply {
                color = Color.parseColor("#4361EE")
                strokeWidth = 2f
            }
            canvas.drawLine(margin, 90f, pageWidth - margin, 90f, linePaint)

            // Render konten teks menggunakan StaticLayout agar otomatis bungkus baris (auto-wrap)
            canvas.save()
            canvas.translate(margin, 115f)
            
            val builder = StaticLayout.Builder.obtain(content, 0, content.length, textPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(3f, 1f)
                .setIncludePad(true)
            
            val staticLayout = builder.build()
            staticLayout.draw(canvas)
            canvas.restore()

            pdfDocument.finishPage(page)

            // Simpan ke direktori Downloads perangkat
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.close()

            Toast.makeText(requireContext(), "Berhasil mengunduh berkas: $fileName di direktori Downloads", Toast.LENGTH_LONG).show()
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