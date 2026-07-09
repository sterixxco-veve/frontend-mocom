package com.example.myapplication.ui.admin.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.example.myapplication.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by viewModels({ requireParentFragment() }) {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository, app.attendanceRepository, app.announcementRepository, app.replacementRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminDashboardBinding.bind(view)

        // Ambil Company ID dari Session SharedPreferences atau Intent
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", Context.MODE_PRIVATE)
        val currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", -1).let {
            if (it != -1) it else sharedPref.getInt("LOGIN_COMPANY_ID", 1)
        }

        // Picu pemuatan data dari ViewModel
        viewModel.loadCompanyName(currentCompanyId)
        viewModel.loadUserByCompanyId(currentCompanyId)
        viewModel.loadSchedules(currentCompanyId)
        viewModel.loadAttendanceByCompanyId(currentCompanyId)
        viewModel.loadReplacementRequests(currentCompanyId)

        // Hubungkan LiveData dengan Tampilan (Views)
        
        // 1. Nama Perusahaan
        viewModel.companyName.observe(viewLifecycleOwner) { name ->
            binding.tvCompanyName.text = name ?: "Perusahaan #$currentCompanyId"
        }

        // 2. Jumlah Tutor Aktif
        viewModel.users.observe(viewLifecycleOwner) { list ->
            // Filter tutor yang bukan admin (role_id != 1)
            val tutors = list?.filter { it.role_id != 1 } ?: emptyList()
            binding.tvTutorCountVal.text = tutors.size.toString()
        }

        // 3. Total Jadwal
        viewModel.schedules.observe(viewLifecycleOwner) { list ->
            binding.tvScheduleCountVal.text = (list?.size ?: 0).toString()
        }

        // 4. Izin Menunggu Persetujuan (Pending)
        viewModel.replacementRequests.observe(viewLifecycleOwner) { list ->
            val pendingCount = list?.count { it.status == "pending" } ?: 0
            binding.tvPendingCountVal.text = pendingCount.toString()
        }

        // 5. Absensi & Grafik Rekap Kehadiran
        viewModel.attendances.observe(viewLifecycleOwner) { list ->
            val attendances = list ?: emptyList()
            val total = attendances.size
            val presentCount = attendances.count { it.status.lowercase() == "present" }
            val lateCount = attendances.count { it.status.lowercase() == "late" }
            val absentCount = attendances.count { it.status.lowercase() == "absent" }

            // Hitung persentase Kehadiran (Hadir + Telat) / Total
            val rate = if (total > 0) {
                (((presentCount + lateCount).toFloat() / total) * 100).toInt()
            } else {
                0
            }
            binding.tvAttendanceRateVal.text = "$rate%"

            // Konfigurasi bobot grafik batang horizontal stacked
            val weightSum = if (total > 0) total.toFloat() else 1.0f
            
            val paramsPresent = binding.viewBarPresent.layoutParams as LinearLayout.LayoutParams
            val paramsLate = binding.viewBarLate.layoutParams as LinearLayout.LayoutParams
            val paramsAbsent = binding.viewBarAbsent.layoutParams as LinearLayout.LayoutParams

            if (total > 0) {
                paramsPresent.weight = presentCount.toFloat()
                paramsLate.weight = lateCount.toFloat()
                paramsAbsent.weight = absentCount.toFloat()
                
                binding.viewBarPresent.visibility = if (presentCount > 0) View.VISIBLE else View.GONE
                binding.viewBarLate.visibility = if (lateCount > 0) View.VISIBLE else View.GONE
                binding.viewBarAbsent.visibility = if (absentCount > 0) View.VISIBLE else View.GONE
            } else {
                // Default full abu-abu jika belum ada data sama sekali
                paramsPresent.weight = 0f
                paramsLate.weight = 0f
                paramsAbsent.weight = 0f
                
                binding.viewBarPresent.visibility = View.GONE
                binding.viewBarLate.visibility = View.GONE
                binding.viewBarAbsent.visibility = View.GONE
            }

            binding.viewBarPresent.layoutParams = paramsPresent
            binding.viewBarLate.layoutParams = paramsLate
            binding.viewBarAbsent.layoutParams = paramsAbsent
            binding.layoutStackedBar.weightSum = weightSum

            // Legenda detail counts & persentase
            val pPct = if (total > 0) (presentCount * 100) / total else 0
            val lPct = if (total > 0) (lateCount * 100) / total else 0
            val aPct = if (total > 0) (absentCount * 100) / total else 0

            binding.tvLegendPresent.text = "$presentCount ($pPct%)"
            binding.tvLegendLate.text = "$lateCount ($lPct%)"
            binding.tvLegendAbsent.text = "$absentCount ($aPct%)"
        }

        // 6. AI Burnout Analysis
        viewModel.burnoutAnalysis.observe(viewLifecycleOwner) { analysis ->
            binding.tvAiContent.text = analysis
        }

        // Hubungkan Event Klik
        binding.btnLihatLeaveRequest.setOnClickListener {
            findNavController().navigate(
                R.id.action_to_view_request
            )
        }

        binding.btnRefreshAi.setOnClickListener {
            val tutorsCount = binding.tvTutorCountVal.text.toString()
            val schedulesCount = binding.tvScheduleCountVal.text.toString()
            val attendanceRate = binding.tvAttendanceRateVal.text.toString()
            val pendingIzin = binding.tvPendingCountVal.text.toString()

            val prompt = "Sebagai AI Burnout Optimizer, berikan analisis singkat ringkas (maksimal 3 paragraf, bahasa indonesia) untuk operasional bimbingan belajar dengan data berikut:\n" +
                    "- Jumlah Tutor Aktif: $tutorsCount\n" +
                    "- Total Jadwal Mengajar: $schedulesCount\n" +
                    "- Persentase Kehadiran: $attendanceRate\n" +
                    "- Pengajuan Izin Menunggu Persetujuan: $pendingIzin\n\n" +
                    "Berikan kesimpulan tingkat kelelahan/burnout tutor saat ini serta saran taktis pembagian tugas atau antisipasi burnout."
            
            viewModel.generateBurnoutAnalysis(prompt)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
