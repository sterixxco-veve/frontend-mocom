package com.example.myapplication.ui.admin.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAdminBroadcastBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.example.myapplication.ui.admin.adapter.BroadcastAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminBroadcastFragment : Fragment(R.layout.fragment_admin_broadcast) {
    private var _binding: FragmentAdminBroadcastBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels({ requireParentFragment() }) {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository, app.attendanceRepository, app.announcementRepository)
    }
    private lateinit var broadcastAdapter: BroadcastAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminBroadcastBinding.bind(view)

        val currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", 1)

        broadcastAdapter = BroadcastAdapter()
        binding.rvBroadcastHistory.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = broadcastAdapter
        }

        viewModel.loadAnnouncements(currentCompanyId)

        // ACTION SWIPE REFRESH: Jalankan ulang load data dari server saat ditarik
        binding.swipeRefreshBroadcast.setOnRefreshListener {
            viewModel.loadAnnouncements(currentCompanyId)
        }

        // OBSERVER DATA: Matikan lingkaran loading saat data baru mendarat
        viewModel.announcements.observe(viewLifecycleOwner) { listAnnouncement ->
            binding.swipeRefreshBroadcast.isRefreshing = false

            if (listAnnouncement != null) {
                broadcastAdapter.submitList(listAnnouncement)
            }
        }

        binding.btnSendBroadcast.setOnClickListener {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}