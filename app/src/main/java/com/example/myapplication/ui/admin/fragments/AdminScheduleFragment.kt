package com.example.myapplication.ui.admin.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.databinding.FragmentAdminScheduleBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.example.myapplication.ui.admin.adapter.ScheduleAdapter

class AdminScheduleFragment : Fragment(R.layout.fragment_admin_schedule) {

    private var _binding: FragmentAdminScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var scheduleAdapter: ScheduleAdapter
    private val currentCal = java.util.Calendar.getInstance()
    private var selectedMonthPosition = currentCal.get(java.util.Calendar.MONTH) + 1
    private var selectedYearValue = currentCal.get(java.util.Calendar.YEAR)

    // =========================================================================
    // 💡 PERBAIKAN UTAMA: Gunakan Factory Terpusat yang Mengambil Data dari App.kt
    // Menggunakan scope 'requireActivity()' agar ViewModel di-share otomatis ke BottomSheet
    // =========================================================================
    private val viewModel: AdminViewModel by viewModels({ requireActivity() }) {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository, app.attendanceRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminScheduleBinding.bind(view)

        // Ambil nilai EXTRA_COMPANY_ID yang dikirim dari MainActivity lewat AdminActivity
        val companyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", -1)
        val userId = requireActivity().intent.getIntExtra("EXTRA_USER_ID", -1)

        // Setup Adapter RecyclerView
        scheduleAdapter = ScheduleAdapter(
            scheduleList = emptyList(),
            onItemClick = { scheduleTerpilih ->
                val staffAssignmentBottomSheet = AdminStaffAssignmentBottomSheetFragment(scheduleTerpilih)
                staffAssignmentBottomSheet.show(parentFragmentManager, "AdminStaffAssignmentBottomSheet")
            },
            onEditClick = { scheduleTerpilih ->
                val bundle = Bundle().apply {
                    putInt("EDIT_ID", scheduleTerpilih.id)
                    putInt("EDIT_CREATED_BY", scheduleTerpilih.created_by)
                    putInt("EDIT_COMPANY_ID", scheduleTerpilih.company_id)
                    putString("EDIT_TITLE", scheduleTerpilih.title)
                    putString("EDIT_DESC", scheduleTerpilih.description)
                    putString("EDIT_LOCATION", scheduleTerpilih.location)
                    putLong("EDIT_START", scheduleTerpilih.start_time)
                    putLong("EDIT_END", scheduleTerpilih.end_time)
                }

                val addScheduleBottomSheet = AddScheduleBottomSheetFragment().apply {
                    arguments = bundle
                }
                addScheduleBottomSheet.show(parentFragmentManager, "AddScheduleBottomSheet")
            },
            onDeleteClick = { scheduleTerpilih ->
                viewModel.deleteSchedule(scheduleTerpilih.id) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Jadwal berhasil dihapus!", Toast.LENGTH_SHORT).show()
                        viewModel.loadSchedules(companyId)
                    } else {
                        Toast.makeText(requireContext(), "Gagal menghapus jadwal!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        // Setup Filter Dropdowns (Bulan & Tahun)
        val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val years = arrayOf("2024", "2025", "2026", "2027", "2028")

        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, months)
        binding.actvFilterMonth.setAdapter(monthAdapter)
        val defaultMonthName = months[selectedMonthPosition - 1]
        binding.actvFilterMonth.setText(defaultMonthName, false)
        binding.actvFilterMonth.setOnItemClickListener { _, _, position, _ ->
            selectedMonthPosition = position + 1
            filterAndSubmitList(viewModel.schedules.value)
        }

        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years)
        binding.actvFilterYear.setAdapter(yearAdapter)
        var defaultYearIdx = years.indexOf(selectedYearValue.toString())
        if (defaultYearIdx == -1) defaultYearIdx = years.indexOf("2026")
        if (defaultYearIdx == -1) defaultYearIdx = 0
        binding.actvFilterYear.setText(years[defaultYearIdx], false)
        selectedYearValue = years[defaultYearIdx].toInt()
        binding.actvFilterYear.setOnItemClickListener { _, _, position, _ ->
            selectedYearValue = years[position].toInt()
            filterAndSubmitList(viewModel.schedules.value)
        }

        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }

        // Setup Tampilan Warna Swipe-to-Refresh
        binding.swipeRefresh.setColorSchemeColors(Color.parseColor("#06B6D4"))
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#1E293B"))

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadSchedules(companyId)
        }

        // =========================================================================
        // 💡 KODEAN ANONIM REPOSITORY YANG PANJANG SUDAH DIHAPUS
        // Karena sekarang data langsung mengalir dari App.kt -> Factory -> ViewModel
        // =========================================================================

        // Amat-amati perubahan LiveData Schedules dari server
        viewModel.schedules.observe(viewLifecycleOwner) { listJadwal ->
            binding.swipeRefresh.isRefreshing = false
            filterAndSubmitList(listJadwal)
        }

        // Jalankan load data pertama kali dengan memfilter companyId
        binding.swipeRefresh.isRefreshing = true
        viewModel.loadSchedules(companyId)

        binding.fabAdd.setOnClickListener {
            val addScheduleBottomSheet = AddScheduleBottomSheetFragment()
            addScheduleBottomSheet.show(parentFragmentManager, "AddScheduleBottomSheet")
        }
    }

    private fun filterAndSubmitList(listJadwal: List<Schedule>?) {
        if (listJadwal == null) {
            scheduleAdapter.submitList(emptyList())
            return
        }

        val filteredList = listJadwal.filter { schedule ->
            val cal = java.util.Calendar.getInstance().apply {
                timeInMillis = schedule.start_time
            }
            val scheduleMonth = cal.get(java.util.Calendar.MONTH) + 1 // 1-12
            val scheduleYear = cal.get(java.util.Calendar.YEAR)

            val monthMatch = scheduleMonth == selectedMonthPosition
            val yearMatch = scheduleYear == selectedYearValue

            monthMatch && yearMatch
        }

        scheduleAdapter.submitList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}