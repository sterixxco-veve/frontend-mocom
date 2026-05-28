package com.example.myapplication.ui.admin.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.RetrofitClient
import com.example.myapplication.data.sources.remote.RetrofitDataSource
import com.example.myapplication.data.repositories.ScheduleRepository // Impor Interface asli kamu
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.databinding.FragmentScheduleCrudBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.adapter.ScheduleAdapter

class ScheduleCrudFragment : Fragment(R.layout.fragment_schedule_crud) {

    private var _binding: FragmentScheduleCrudBinding? = null
    private val binding get() = _binding!!

    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var viewModel: AdminViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScheduleCrudBinding.bind(view)

        // 1. Inisialisasi Adapter & RecyclerView
        scheduleAdapter = ScheduleAdapter()
        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }

        // 2. Siapkan Data Source
        val webService = RetrofitClient.webService
        val remoteDataSource = RetrofitDataSource(webService)

        // 3. TRIK SULAP: Implementasikan Interface ScheduleRepository secara anonim (langsung jadi objek)
        val repository = object : ScheduleRepository {
            override suspend fun getAll(): List<Schedule> {
                // Langsung arahkan fungsi getAll() ke fungsi fetch milik Retrofit
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

        // 4. Masukkan objek repository anonim tersebut ke dalam ViewModel Factory
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AdminViewModel(repository) as T
            }
        })[AdminViewModel::class.java]

        // 5. Amati perubahan LiveData schedules dari AdminViewModel
        viewModel.schedules.observe(viewLifecycleOwner) { listJadwal ->
            if (listJadwal != null && listJadwal.isNotEmpty()) {
                scheduleAdapter.submitList(listJadwal)
            } else {
                Toast.makeText(requireContext(), "Tidak ada data jadwal saat ini", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. Jalankan fungsi loadSchedules() untuk menarik data dari Node.js murni
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