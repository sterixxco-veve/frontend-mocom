package com.example.myapplication.ui.staff.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.RetrofitClient
import com.example.myapplication.databinding.FragmentStaffNotificationBinding
import com.example.myapplication.ui.staff.NotificationReplacementViewModel
import com.example.myapplication.ui.staff.NotificationReplacementViewModelFactory
import com.example.myapplication.ui.staff.adapter.NotificationReplacementAdapter

class StaffNotificationFragment :
    Fragment(R.layout.fragment_staff_notification) {

    private var _binding: FragmentStaffNotificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NotificationReplacementAdapter

    private val viewModel: NotificationReplacementViewModel by viewModels {

        val app = requireActivity().application as App

        NotificationReplacementViewModelFactory(
            RetrofitClient.apiService,

            app.scheduleRepository,

            app.attendanceRepository,

            app.assignmentRepository,

            app.announcementRepository,

            app.replacementRepository

        )

    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        _binding =
            FragmentStaffNotificationBinding.bind(view)

        adapter = NotificationReplacementAdapter()

        binding.rvNotification.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvNotification.adapter =
            adapter

        val pref = requireActivity().getSharedPreferences(
            "EduStaffSession",
            Context.MODE_PRIVATE
        )

        val userId =
            pref.getInt(
                "LOGIN_USER_ID",
                0
            )

        viewModel.loadReplacementNotifications(userId)

        viewModel.replacementNotifications.observe(viewLifecycleOwner){ list ->
            adapter.submitList(list)
            if (list.isNullOrEmpty()) {
                binding.llEmptyState.visibility = View.VISIBLE
                binding.rvNotification.visibility = View.GONE
            } else {
                binding.llEmptyState.visibility = View.GONE
                binding.rvNotification.visibility = View.VISIBLE
            }
        }

    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

}