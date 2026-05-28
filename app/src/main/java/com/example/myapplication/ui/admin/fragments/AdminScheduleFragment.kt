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

        // 1. Inisialisasi Adapter & RecyclerView
        scheduleAdapter = ScheduleAdapter()
        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }

        // 2. TAMBAHKAN: Setup Tampilan Warna Swipe-to-Refresh (Aksen Cyan & Dark Mode)
        binding.swipeRefresh.setColorSchemeColors(Color.parseColor("#06B6D4"))
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#1E293B"))

        // 3. TAMBAHKAN: Listener geser layar ke bawah untuk reload/sync data dari SQL Node.js
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadSchedules()
        }

        // 4. Siapkan Data Source
        val webService = RetrofitClient.webService
        val remoteDataSource = RetrofitDataSource(webService)

        // 5. Implementasikan Interface ScheduleRepository secara anonim
        val repository = object : ScheduleRepository {
            override suspend fun getAll(): List<Schedule> {
                return remoteDataSource.fetchAllSchedules()
            }

            override suspend fun getById(id: Int): Schedule? {
                return null
            }

            override suspend fun insert(schedule: Schedule): Schedule {
                return remoteDataSource.insertSchedule(schedule)
            }

            override suspend fun sync() {
                // Kosongkan jika belum digunakan
            }
        }

        // 6. Masukkan objek repository anonim tersebut ke dalam ViewModel Factory
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AdminViewModel(repository) as T
            }
        })[AdminViewModel::class.java]

        // 7. Amati perubahan LiveData schedules dari AdminViewModel
        viewModel.schedules.observe(viewLifecycleOwner) { listJadwal ->
            // TAMBAHKAN: Hentikan animasi putaran loading begitu data dari server diterima
            binding.swipeRefresh.isRefreshing = false

            if (listJadwal != null && listJadwal.isNotEmpty()) {
                scheduleAdapter.submitList(listJadwal)
            } else {
                Toast.makeText(requireContext(), "Tidak ada data jadwal saat ini", Toast.LENGTH_SHORT).show()
            }
        }

        // 8. Tampilkan animasi loading saat pertama kali halaman dibuka, lalu panggil data SQL
        binding.swipeRefresh.isRefreshing = true
        viewModel.loadSchedules()

        // Klik aksi untuk memunculkan BottomSheet input jadwal baru
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