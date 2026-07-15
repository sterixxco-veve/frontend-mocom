package com.example.myapplication.ui.admin.fragments

import android.content.Context
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
import com.example.myapplication.databinding.FragmentAdminScheduleBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.example.myapplication.ui.admin.adapter.ScheduleAdapter // 💡 Menggunakan AdminScheduleAdapter pengunci visual kita
import androidx.core.widget.doAfterTextChanged

class AdminScheduleFragment : Fragment(R.layout.fragment_admin_schedule) {

    private var _binding: FragmentAdminScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var pendingAdapter: ScheduleAdapter
    private lateinit var acceptedAdapter: ScheduleAdapter
    private lateinit var otherAdapter: ScheduleAdapter
    private val currentCal = java.util.Calendar.getInstance()
    private var selectedMonthPosition = currentCal.get(java.util.Calendar.MONTH) + 1
    private var selectedYearValue = currentCal.get(java.util.Calendar.YEAR)

    private val viewModel: AdminViewModel by viewModels({ requireActivity() }) {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository, app.attendanceRepository, app.announcementRepository, app.replacementRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminScheduleBinding.bind(view)

        // Ambil ID dari SharedPreferences agar sinkron dengan session login global admin
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", Context.MODE_PRIVATE)
        val companyId = sharedPref.getInt("LOGIN_COMPANY_ID", -1)

        val onEdit: (com.example.myapplication.data.sources.models.Schedule) -> Unit = { scheduleTerpilih ->
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
        }

        val onDelete: (com.example.myapplication.data.sources.models.Schedule) -> Unit = { scheduleTerpilih ->
            viewModel.deleteSchedule(scheduleTerpilih.id) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Jadwal berhasil dihapus!", Toast.LENGTH_SHORT).show()
                    viewModel.loadSchedules(companyId)
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus jadwal!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Setup 3 adapter terpisah untuk masing-masing bagian status
        pendingAdapter = ScheduleAdapter(
            scheduleList = emptyList(),
            onItemClick = { schedule ->
                val name = schedule.staffName ?: "karyawan"
                Toast.makeText(requireContext(), "Jadwal ini sudah ditugaskan ke $name (menunggu konfirmasi).", Toast.LENGTH_SHORT).show()
            },
            onEditClick = onEdit,
            onDeleteClick = onDelete
        )

        acceptedAdapter = ScheduleAdapter(
            scheduleList = emptyList(),
            onItemClick = { schedule ->
                val name = schedule.staffName ?: "karyawan"
                Toast.makeText(requireContext(), "Jadwal ini sudah disetujui oleh $name dan tidak dapat diubah.", Toast.LENGTH_SHORT).show()
            },
            onEditClick = onEdit,
            onDeleteClick = onDelete
        )

        otherAdapter = ScheduleAdapter(
            scheduleList = emptyList(),
            onItemClick = { scheduleTerpilih ->
                val staffAssignmentBottomSheet = AdminStaffAssignmentBottomSheetFragment(scheduleTerpilih)
                staffAssignmentBottomSheet.show(parentFragmentManager, "AdminStaffAssignmentBottomSheet")
            },
            onEditClick = onEdit,
            onDeleteClick = onDelete
        )

        // Setup Filter Dropdowns (Bulan & Tahun)
        val months = arrayOf("Semua", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val years = arrayOf("Semua", "2024", "2025", "2026", "2027", "2028")

        selectedMonthPosition = 0
        selectedYearValue = 0

// Setup Dropdown Bulan
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, months)
        binding.actvFilterMonth.setAdapter(monthAdapter)
        binding.actvFilterMonth.setText(months[selectedMonthPosition], false)
        viewModel.selectedMonth.value = selectedMonthPosition

        binding.actvFilterMonth.setOnItemClickListener { _, _, position, _ ->
            selectedMonthPosition = position // 0 = Semua Bulan, 1 = Januari, dst.
            viewModel.updateDateFilter(selectedMonthPosition, selectedYearValue)
        }

// Setup Dropdown Tahun secara Dinamis berdasarkan data LiveData
        viewModel.availableYears.observe(viewLifecycleOwner) { listTahun ->
            val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listTahun)
            binding.actvFilterYear.setAdapter(yearAdapter)
            
            val currentYearText = if (selectedYearValue == 0) "Semua" else selectedYearValue.toString()
            val index = listTahun.indexOf(currentYearText)
            if (index != -1) {
                binding.actvFilterYear.setText(listTahun[index], false)
            } else {
                binding.actvFilterYear.setText(listTahun[0], false)
                selectedYearValue = 0
                viewModel.selectedYear.value = 0
            }
        }

        binding.actvFilterYear.setOnItemClickListener { _, _, position, _ ->
            val selectedText = binding.actvFilterYear.adapter.getItem(position).toString()
            if (selectedText == "Semua") {
                selectedYearValue = 0
            } else {
                selectedYearValue = selectedText.toInt()
            }
            viewModel.updateDateFilter(selectedMonthPosition, selectedYearValue)
        }

        // Setup Search
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.updateSearchQuery(text?.toString().orEmpty())
        }

        // Setup Dropdown Sort (Mengatasi bug AutoCompleteTextView yang menyaring pilihan menu)
        val sortOptions = arrayOf("Terlama", "Terbaru", "Judul A-Z", "Judul Z-A")
        val sortKeys = arrayOf("date_asc", "date_desc", "title_asc", "title_desc")
        val sortAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, sortOptions) {
            override fun getFilter(): android.widget.Filter {
                return object : android.widget.Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        val results = FilterResults()
                        results.values = sortOptions
                        results.count = sortOptions.size
                        return results
                    }
                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        notifyDataSetChanged()
                    }
                }
            }
        }
        binding.actvFilterSort.setAdapter(sortAdapter)
        binding.actvFilterSort.setText(sortOptions[0], false)
        viewModel.updateSortOption(sortKeys[0])
        binding.actvFilterSort.setOnItemClickListener { _, _, position, _ ->
            viewModel.updateSortOption(sortKeys[position])
        }

        // Setup 3 RecyclerView untuk masing-masing seksi
        binding.rvPendingSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pendingAdapter
        }
        binding.rvAcceptedSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = acceptedAdapter
        }
        binding.rvOtherSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = otherAdapter
        }

        // Setup Tampilan Warna Swipe-to-Refresh
        binding.swipeRefresh.setColorSchemeColors(Color.parseColor("#06B6D4"))
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#1E293B"))

        // =========================================================================
// 💡 Pemicu Refresh Harus Menarik KEDUA Data Sekaligus dari Server
// =========================================================================
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadSchedules(companyId)
        }

// =========================================================================
// 🎯 Observer filteredSchedules (Pemisahan ke 3 Sub-List)
// =========================================================================
        viewModel.filteredSchedules.observe(viewLifecycleOwner) { listJadwalTerfilter ->
            binding.swipeRefresh.isRefreshing = false

            if (listJadwalTerfilter != null) {
                // Split list berdasarkan status penugasan
                val pendingList = listJadwalTerfilter.filter {
                    it.assignmentStatus?.lowercase() == "pending"
                }
                val acceptedList = listJadwalTerfilter.filter {
                    it.assignmentStatus?.lowercase() == "accepted"
                }
                val otherList = listJadwalTerfilter.filter {
                    it.assignmentStatus == null ||
                    it.assignmentStatus?.lowercase() == "declined" ||
                    it.assignmentStatus?.lowercase() == "completed"
                }

                // Update data untuk masing-masing seksi
                pendingAdapter.submitList(pendingList)
                acceptedAdapter.submitList(acceptedList)
                otherAdapter.submitList(otherList)

                // Atur visibilitas Header & List secara dinamis
                if (pendingList.isEmpty()) {
                    binding.tvHeaderPending.visibility = View.GONE
                    binding.rvPendingSchedules.visibility = View.GONE
                } else {
                    binding.tvHeaderPending.visibility = View.VISIBLE
                    binding.rvPendingSchedules.visibility = View.VISIBLE
                }

                if (acceptedList.isEmpty()) {
                    binding.tvHeaderAccepted.visibility = View.GONE
                    binding.rvAcceptedSchedules.visibility = View.GONE
                } else {
                    binding.tvHeaderAccepted.visibility = View.VISIBLE
                    binding.rvAcceptedSchedules.visibility = View.VISIBLE
                }
            } else {
                pendingAdapter.submitList(emptyList())
                acceptedAdapter.submitList(emptyList())
                otherAdapter.submitList(emptyList())
                binding.tvHeaderPending.visibility = View.GONE
                binding.rvPendingSchedules.visibility = View.GONE
                binding.tvHeaderAccepted.visibility = View.GONE
                binding.rvAcceptedSchedules.visibility = View.GONE
            }
        }

// =========================================================================
// 💡 PERBAIKAN 3: Load Pertama Kali Harus Mengunduh Schedules DAN Assignments
// =========================================================================
        binding.swipeRefresh.isRefreshing = true
        viewModel.loadSchedules(companyId)

        binding.fabAdd.setOnClickListener {
            val addScheduleBottomSheet = AddScheduleBottomSheetFragment()
            addScheduleBottomSheet.show(parentFragmentManager, "AddScheduleBottomSheet")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}