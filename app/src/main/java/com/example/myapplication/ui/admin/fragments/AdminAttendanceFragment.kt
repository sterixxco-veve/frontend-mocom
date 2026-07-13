package com.example.myapplication.ui.admin.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.sources.local.entities.AttendanceEntity
import com.example.myapplication.data.sources.models.Attendance
import com.example.myapplication.databinding.FragmentAdminAttendanceBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.example.myapplication.ui.admin.adapter.AttendanceAdapter
import androidx.core.widget.doAfterTextChanged
import java.util.Locale

class AdminAttendanceFragment : Fragment() {

    private var _binding: FragmentAdminAttendanceBinding? = null
    private val binding get() = _binding!!

    // =========================================================================
    // 💡 SINKRONISASI FACTORY: Menyuntikkan 3 repositori utama secara seimbang
    // =========================================================================
    private val viewModel: AdminViewModel by viewModels {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository, app.attendanceRepository, app.announcementRepository, app.replacementRepository)
    }

    private lateinit var attendanceAdapter: AttendanceAdapter
    private var originalAttendanceList: List<Attendance> = emptyList()
    private var currentCompanyId: Int = 1
    private var currentFilterStatus: String = "all" // Pilihan: "all", "present", "late", "absent"
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data Company ID riil dari Session SharedPreferences yang sudah login
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", Context.MODE_PRIVATE)
        currentCompanyId = sharedPref.getInt("LOGIN_COMPANY_ID", 1)

        // 1. Setup RecyclerView dan Pasang AttendanceAdapter murni View Binding
        setupRecyclerView()

        // 2. Setup Swipe Refresh Layout untuk tarik-pemicu segarkan data cloud
        binding.swipeRefreshAttendance.setOnRefreshListener {
            loadDataFromApi()
        }

        // 3. Setup Logika Klik Filter 4 Status Kehadiran
        setupFilterButtons()

        // 3a. Setup Logika Pencarian
        binding.etSearchAttendance.doAfterTextChanged { text ->
            searchQuery = text?.toString().orEmpty()
            filterAndDisplayData()
        }

        // 4. Amati pergerakan LiveData Absensi dari ViewModel
        setupObservers()

        // Pertama kali masuk halaman, otomatis tarik data dari cloud MySQL
        loadDataFromApi()
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdapter(emptyList())
        binding.rvAttendanceHistory.layoutManager = LinearLayoutManager(context)
        binding.rvAttendanceHistory.adapter = attendanceAdapter
    }

    private fun loadDataFromApi() {
        binding.swipeRefreshAttendance.isRefreshing = true
        // 💡 PERBAIKAN: Menembak fungsi pencari data absensi, bukan data user lagi
        viewModel.loadAttendanceByCompanyId(currentCompanyId)
    }

    private fun setupObservers() {
        viewModel.attendances.observe(viewLifecycleOwner) { listAbsenDariApi ->
            // Langsung terima berkat kecocokan tipe data yang harmonis!
            originalAttendanceList = listAbsenDariApi ?: emptyList()
            filterAndDisplayData()
            binding.swipeRefreshAttendance.isRefreshing = false
        }
    }

    private fun setupFilterButtons() {
        binding.btnFilterAll.setOnClickListener {
            currentFilterStatus = "all"
            updateButtonVisual(binding.btnFilterAll)
            filterAndDisplayData()
            Toast.makeText(context, "Menampilkan semua riwayat absensi", Toast.LENGTH_SHORT).show()
        }

        binding.btnFilterPresent.setOnClickListener {
            currentFilterStatus = "present"
            updateButtonVisual(binding.btnFilterPresent)
            filterAndDisplayData()
            Toast.makeText(context, "Menampilkan staff berstatus Present", Toast.LENGTH_SHORT).show()
        }

        binding.btnFilterLate.setOnClickListener {
            currentFilterStatus = "late"
            updateButtonVisual(binding.btnFilterLate)
            filterAndDisplayData()
            Toast.makeText(context, "Menampilkan staff berstatus Late", Toast.LENGTH_SHORT).show()
        }

        binding.btnFilterAbsent.setOnClickListener {
            currentFilterStatus = "absent"
            updateButtonVisual(binding.btnFilterAbsent)
            filterAndDisplayData()
            Toast.makeText(context, "Menampilkan staff berstatus Absent", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================
    // 💡 LOGIKA FILTER: Menyaring data absensi secara runtime lokal di HP
    // =========================================================================
    private fun filterAndDisplayData() {
        val query = searchQuery.trim()
        val filteredList = originalAttendanceList.filter { attendance ->
            // Filter berdasarkan status tombol
            val statusMatch = currentFilterStatus == "all" ||
                    attendance.status.lowercase(Locale.getDefault()) == currentFilterStatus

            // Filter berdasarkan kolom pencarian (ID Penugasan / Nama Staff Penugasan)
            val searchMatch = query.isEmpty() ||
                    attendance.assignment_id.toString().contains(query) ||
                    "staff penugasan #${attendance.assignment_id}".contains(query, ignoreCase = true)

            statusMatch && searchMatch
        }
        attendanceAdapter.submitList(filteredList)
    }

    // =========================================================================
    // 🎨 VISUAL STYLING: Menyesuaikan warna tombol aktif (Biru Premium) vs Nonaktif
    // =========================================================================
    private fun updateButtonVisual(activeButton: Button) {
        val allButtons = listOf(
            binding.btnFilterAll,
            binding.btnFilterPresent,
            binding.btnFilterLate,
            binding.btnFilterAbsent
        )

        allButtons.forEach { button ->
            if (button == activeButton) {
                // State Aktif (Gaya Premium Biru EduStaff Pro)
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4361EE"))
                button.setTextColor(Color.WHITE)
            } else {
                // State Non-Aktif (Gaya Abu Lembut Netral)
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EEF2F6"))
                button.setTextColor(Color.parseColor("#8D99AE"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}