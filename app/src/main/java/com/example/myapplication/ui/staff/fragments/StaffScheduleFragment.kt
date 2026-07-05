package com.example.myapplication.ui.staff.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentStaffScheduleBinding
import com.example.myapplication.ui.staff.MyScheduleViewModel
import com.example.myapplication.ui.staff.MyScheduleViewModelFactory
import com.example.myapplication.ui.staff.adapter.MyScheduleAdapter

class StaffScheduleFragment : Fragment(R.layout.fragment_staff_schedule) {

    private var _binding: FragmentStaffScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var myScheduleAdapter: MyScheduleAdapter
    private val currentCal = java.util.Calendar.getInstance()
    private var selectedMonthPosition = currentCal.get(java.util.Calendar.MONTH) + 1
    private var selectedYearValue = currentCal.get(java.util.Calendar.YEAR)

    private val viewModel: MyScheduleViewModel by viewModels {

        val app = requireActivity().application as App

        MyScheduleViewModelFactory(
            app.assignmentRepository
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStaffScheduleBinding.bind(view)

        // =========================================================================
        // 🎯 PERBAIKAN DI SINI: Ambil userId dari SharedPreferences Sesi Login
        // =========================================================================
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", android.content.Context.MODE_PRIVATE)

        // Pastikan key "LOGIN_USER_ID" ini namanya sama dengan yang Anda pakai saat save sesi di halaman Login
        val userId = sharedPref.getInt("LOGIN_USER_ID", -1)

        myScheduleAdapter = MyScheduleAdapter(
            myScheduleList = emptyList(),
            onItemClick = {
                // nanti kalau ingin buka detail jadwal
            }
        )

        // Setup Filter Dropdowns (Bulan & Tahun) untuk Staff
        val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val years = arrayOf("2024", "2025", "2026", "2027", "2028")

        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, months)
        binding.actvFilterMonth.setAdapter(monthAdapter)
        val defaultMonthName = months[selectedMonthPosition - 1]
        binding.actvFilterMonth.setText(defaultMonthName, false)
        binding.actvFilterMonth.setOnItemClickListener { _, _, position, _ ->
            selectedMonthPosition = position + 1
            filterAndSubmitList(viewModel.mySchedules.value)
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
            filterAndSubmitList(viewModel.mySchedules.value)
        }

        binding.rvMySchedule.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myScheduleAdapter
        }

        viewModel.mySchedules.observe(viewLifecycleOwner) { schedules ->
            filterAndSubmitList(schedules)
        }

        // Pemicu load data dengan ID yang valid dari session
        viewModel.loadMySchedule(userId)
    }

    private fun filterAndSubmitList(listAssignments: List<com.example.myapplication.data.sources.models.MySchedule>?) {
        if (listAssignments == null) {
            myScheduleAdapter.submitList(emptyList())
            return
        }

        val parser = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()

        val filteredList = listAssignments.filter { assignment ->
            val date = try {
                parser.parse(assignment.start_time)
            } catch (e: Exception) {
                null
            }

            if (date != null) {
                cal.time = date
                val scheduleMonth = cal.get(java.util.Calendar.MONTH) + 1 // 1-12
                val scheduleYear = cal.get(java.util.Calendar.YEAR)

                val monthMatch = scheduleMonth == selectedMonthPosition
                val yearMatch = scheduleYear == selectedYearValue

                monthMatch && yearMatch
            } else {
                false
            }
        }

        myScheduleAdapter.submitList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}