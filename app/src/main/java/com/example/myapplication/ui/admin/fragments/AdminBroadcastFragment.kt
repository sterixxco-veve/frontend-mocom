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
import com.example.myapplication.data.sources.models.Announcement
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
        AdminViewModelFactory(app.scheduleRepository, app.userRepository, app.attendanceRepository, app.announcementRepository, app.replacementRepository)
    }
    private lateinit var broadcastAdapter: BroadcastAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminBroadcastBinding.bind(view)

        val currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", 1)
        val currentUserId = requireActivity().intent.getIntExtra("EXTRA_USER_ID", 1)

        broadcastAdapter = BroadcastAdapter()
        binding.rvBroadcastHistory.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = broadcastAdapter
        }

        viewModel.loadAnnouncements(currentCompanyId)

        viewModel.announcements.observe(viewLifecycleOwner) { listAnnouncement ->
            binding.swipeRefreshBroadcast.isRefreshing = false
            if (listAnnouncement != null) {
                broadcastAdapter.submitList(listAnnouncement)
            }
        }

        binding.swipeRefreshBroadcast.setOnRefreshListener {
            viewModel.loadAnnouncements(currentCompanyId)
        }

        binding.btnSendBroadcast.setOnClickListener {
            val title = binding.etSubject.text.toString()
            val message = binding.etMessage.text.toString()
            val userID = currentUserId
            val announcement = Announcement(
                id = 0,
                title = title,
                message = message,
                created_by = userID
            )
            viewModel.InsertAnnouncement(announcement, currentCompanyId)
        }
    }

    override fun onResume() {
        super.onResume()
        val currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", 1)
        viewModel.loadAnnouncements(currentCompanyId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}