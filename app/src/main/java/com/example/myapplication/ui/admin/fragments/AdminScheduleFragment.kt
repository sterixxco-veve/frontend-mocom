package com.example.myapplication.ui.admin.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
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

    // =========================================================================
    // 💡 PERBAIKAN UTAMA: Gunakan Factory Terpusat yang Mengambil Data dari App.kt
    // Menggunakan scope 'requireActivity()' agar ViewModel di-share otomatis ke BottomSheet
    // =========================================================================
    private val viewModel: AdminViewModel by viewModels({ requireActivity() }) {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository)
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

            if (listJadwal != null && listJadwal.isNotEmpty()) {
                scheduleAdapter.submitList(listJadwal)
            } else {
                scheduleAdapter.submitList(emptyList())
            }
        }

        // Jalankan load data pertama kali dengan memfilter companyId
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