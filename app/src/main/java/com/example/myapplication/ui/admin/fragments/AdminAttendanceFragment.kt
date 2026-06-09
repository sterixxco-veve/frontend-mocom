package com.example.myapplication.ui.admin.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.databinding.FragmentAdminAttendanceBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.example.myapplication.ui.admin.adapters.UserAdapter

class AdminAttendanceFragment : Fragment(R.layout.fragment_admin_attendance) {

    private var _binding: FragmentAdminAttendanceBinding? = null
    private val binding get() = _binding!!

    // =========================================================================
    // 💡 PERBAIKAN 1: Suntikkan scheduleRepository DAN userRepository ke Factory
    // =========================================================================
    private val viewModel: AdminViewModel by viewModels {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository)
    }

    private lateinit var userAdapter: UserAdapter
    private var originalUserList: List<User> = emptyList()
    private var currentCompanyId: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminAttendanceBinding.bind(view)

        // Ambil data Company ID riil dari Session SharedPreferences yang sudah login
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", Context.MODE_PRIVATE)
        currentCompanyId = sharedPref.getInt("LOGIN_COMPANY_ID", 1)

        // 1. Setup RecyclerView dan Pasang UserAdapter
        setupRecyclerView()

        // 2. Setup Swipe Refresh Layout
        binding.swipeRefreshAttendance.setOnRefreshListener {
            loadDataFromApi()
        }

        // 3. Setup Tombol FAB Add (+) untuk memunculkan BottomSheet Input
        binding.fabAdd.setOnClickListener {
            val addBottomSheet = AddScheduleBottomSheetFragment()
            addBottomSheet.show(childFragmentManager, "ADD_SCHEDULE_BOTTOM_SHEET")
        }

        // 4. Setup Logika Klik Filter Kategori Tombol
        setupFilterButtons()

        // =========================================================================
        // 💡 PERBAIKAN 2: Amati perubahan data LiveData asli dari UserRepository
        // =========================================================================
        setupObservers()

        // Pertama kali masuk halaman, pemicu load data dari API Node.js
        loadDataFromApi()
    }

    private fun setupRecyclerView() {
        binding.rvAttendance.layoutManager = LinearLayoutManager(context)

        userAdapter = UserAdapter(emptyList()) { selectedUser, viewAnchor ->
            Toast.makeText(context, "Opsi untuk ${selectedUser.full_name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvAttendance.adapter = userAdapter
    }

    private fun loadDataFromApi() {
        binding.swipeRefreshAttendance.isRefreshing = true

        // 💡 PERBAIKAN 3: Perintahkan ViewModel untuk menarik data nyata dari UserRepository
        // Pastikan di AdminViewModel kamu sudah punya fungsi ini yang menembak userRepository.getUsersByCompanyId(companyId)
        viewModel.loadUserByCompanyId(currentCompanyId)
    }

    private fun setupObservers() {
        // 💡 Amati LiveData users dari ViewModel (sesuaikan namanya dengan LiveData di AdminViewModel-mu, misal: users / userList)
        viewModel.schedules.observe(viewLifecycleOwner) { _ ->
            // Catatan: Jika kamu membuat LiveData terpisah bernama 'users' di ViewModel, amati yang itu:
            // viewModel.users.observe(viewLifecycleOwner) { listUser -> ... }
        }

        // Contoh penampung LiveData terpadu:
        // Gantilah objek di bawah ini dengan variabel LiveData khusus User milikmu di ViewModel
        viewModel.users.observe(viewLifecycleOwner) { listUserDariApi ->
            originalUserList = listUserDariApi

            // Masukkan data murni ke adapter
            userAdapter.submitList(originalUserList)

            // Matikan roda putar loading swipe refresh
            binding.swipeRefreshAttendance.isRefreshing = false

            // Kembalikan seleksi visual filter tombol ke "Semua" setelah data segar masuk
            triggerButtonVisual(binding.btnFilterAll)
        }

    }

    private fun setupFilterButtons() {
        binding.btnFilterAll.setOnClickListener {
            triggerButtonVisual(binding.btnFilterAll)
            userAdapter.submitList(originalUserList)
            Toast.makeText(context, "Menampilkan seluruh staf", Toast.LENGTH_SHORT).show()
        }

        binding.btnFilterPresent.setOnClickListener {
            triggerButtonVisual(binding.btnFilterPresent)

            // FILTER DINAMIS NYATA: Hanya ambil user dengan is_active == 1 (Active)
            val filteredList = originalUserList.filter { it.is_active == 1 }
            userAdapter.submitList(filteredList)
            Toast.makeText(context, "Menampilkan staff berstatus Active", Toast.LENGTH_SHORT).show()
        }

        binding.btnFilterAbsent.setOnClickListener {
            triggerButtonVisual(binding.btnFilterAbsent)

            // FILTER DINAMIS NYATA: Hanya ambil user dengan is_active == 2 (Nonaktif)
            val filteredList = originalUserList.filter { it.is_active == 2 }
            userAdapter.submitList(filteredList)
            Toast.makeText(context, "Menampilkan staff berstatus Nonaktif", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerButtonVisual(selectedButton: android.widget.Button) {
        resetButtons()
        selectedButton.setBackgroundColor(Color.parseColor("#06B6D4"))
        selectedButton.setTextColor(Color.parseColor("#0F172A"))
    }

    private fun resetButtons() {
        val darkSlate = Color.parseColor("#1E293B")
        val textSlate = Color.parseColor("#CBD5E1")

        binding.btnFilterAll.setBackgroundColor(darkSlate)
        binding.btnFilterAll.setTextColor(textSlate)

        binding.btnFilterPresent.setBackgroundColor(darkSlate)
        binding.btnFilterPresent.setTextColor(textSlate)

        binding.btnFilterAbsent.setBackgroundColor(darkSlate)
        binding.btnFilterAbsent.setTextColor(textSlate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}