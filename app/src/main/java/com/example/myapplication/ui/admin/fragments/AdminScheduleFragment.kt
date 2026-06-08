package com.example.myapplication.ui.admin.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.RetrofitClient
import com.example.myapplication.data.sources.remote.RetrofitDataSource
import com.example.myapplication.data.repositories.ScheduleRepository
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.databinding.FragmentAdminScheduleBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.adapter.ScheduleAdapter

class AdminScheduleFragment : Fragment(R.layout.fragment_admin_schedule) {

    private var _binding: FragmentAdminScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var viewModel: AdminViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminScheduleBinding.bind(view)

        // 💡 KUNCI UTAMA: Ambil nilai EXTRA_COMPANY_ID yang dikirim dari MainActivity lewat AdminActivity
        val companyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", -1)
        val userId = requireActivity().intent.getIntExtra("EXTRA_USER_ID", -1)

        // 💡 Di dalam AdminScheduleFragment.kt -> onViewCreated()
        scheduleAdapter = ScheduleAdapter(
            scheduleList = emptyList(),
            onItemClick = { scheduleTerpilih ->
                // Aksi lama kamu: Membuka bottom sheet penugasan staff
                val staffAssignmentBottomSheet = AdminStaffAssignmentBottomSheetFragment(scheduleTerpilih)
                staffAssignmentBottomSheet.show(parentFragmentManager, "AdminStaffAssignmentBottomSheet")
            },
            onEditClick = { scheduleTerpilih ->
                // ✏️ FUNGSI UNTUK EDIT: Kirim data lewat Bundle Arguments
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
                // 🗑️ Tempat menaruh logika hapus jadwal kamu (misal panggil viewModel.delete(id))
                Toast.makeText(requireContext(), "Hapus ID: ${scheduleTerpilih.id}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }

        // 2. Setup Tampilan Warna Swipe-to-Refresh (Aksen Cyan & Dark Mode)
        binding.swipeRefresh.setColorSchemeColors(Color.parseColor("#06B6D4"))
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#1E293B"))

        // 3. REVISI SINKRONISASI: Swipe-to-refresh sekarang membawa companyId riil ke server
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadSchedules(companyId)
        }

        val webService = RetrofitClient.webService
        val remoteDataSource = RetrofitDataSource(webService)


        // 5. Implementasikan Interface ScheduleRepository secara anonim
        // 5. Implementasikan Interface ScheduleRepository secara anonim
        val repository = object : ScheduleRepository {
            override suspend fun getAll(): List<Schedule> {
                return remoteDataSource.fetchAllSchedules()
            }

            override suspend fun getById(id: Int): Schedule? {
                return null
            }

            override suspend fun getByCompanyId(companyId: Int): List<Schedule> {
                return remoteDataSource.fetchScheduleByCompanyId(companyId)
            }

            override suspend fun insert(schedule: Schedule): Schedule {
                return remoteDataSource.insertSchedule(schedule)
            }

            // 💡 TAMBAHKAN OVERRIDE INI UNTUK MENYEMBUHKAN ERROR BARIS 86:
            override suspend fun update(schedule: Schedule) {
                // Alirkan pemanggilan data langsung ke remoteDataSource kamu
                remoteDataSource.updateSchedule(schedule)
            }

            override suspend fun sync() {
                // Kosongkan jika belum digunakan
            }
        }

        // 6. Gunakan scope 'requireActivity()' agar ViewModel bisa di-share ke BottomSheet
        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AdminViewModel(repository) as T
            }
        })[AdminViewModel::class.java]

        viewModel.schedules.observe(viewLifecycleOwner) { listJadwal ->
            binding.swipeRefresh.isRefreshing = false

            if (listJadwal != null && listJadwal.isNotEmpty()) {
                scheduleAdapter.submitList(listJadwal)
            } else {
                scheduleAdapter.submitList(emptyList())
            }
        }

        // 8. REVISI SINKRONISASI: Jalankan load data pertama kali dengan memfilter companyId
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