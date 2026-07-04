package com.example.myapplication.ui.staff.fragments

import android.os.Bundle
import android.view.View
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

        binding.rvMySchedule.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myScheduleAdapter
        }

        viewModel.mySchedules.observe(viewLifecycleOwner) { schedules ->
            myScheduleAdapter.submitList(schedules)
        }

        // Pemicu load data dengan ID yang valid dari session
        viewModel.loadMySchedule(userId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}