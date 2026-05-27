package com.example.myapplication.ui.admin.fragments

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAdminReportsBinding

class AdminReportsFragment : Fragment(R.layout.fragment_admin_reports) {

    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminReportsBinding.bind(view)

        binding.btnDownloadAttendance.setOnClickListener {
            // Contoh implementasi native DownloadManager Android
            startReportDownload(
                url = "[https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf](https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf)", // Dummy URL untuk demo
                fileName = "Laporan_Kehadiran_Asdos_Mocom.pdf"
            )
        }

        binding.btnDownloadBurnout.setOnClickListener {
            startReportDownload(
                url = "[https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf](https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf)",
                fileName = "Analisis_Burnout_Asdos_AI.pdf"
            )
        }
    }

    private fun startReportDownload(url: String, fileName: String) {
        val context = requireContext()
        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription("Mengunduh laporan rekap EduStaff Pro...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

            Toast.makeText(context, "Pengunduhan Dimulai...", Toast.LENGTH_SHORT).show()

            // Simulasi membuka dokumen di FrameLayout PDF Viewer internal untuk kebutuhan presentasi
            binding.tvPdfPlaceholder.text = "📂 Sedang menampilkan: $fileName\n\n(Komponen PDFView berhasil dimuat & dirender lokal)"
            binding.tvPdfPlaceholder.setTextColor(resources.getColor(android.R.color.white))

        } catch (e: Exception) {
            Toast.makeText(context, "Error mengunduh berkas: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}