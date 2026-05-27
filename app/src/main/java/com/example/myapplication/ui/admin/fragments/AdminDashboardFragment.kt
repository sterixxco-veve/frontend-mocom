package com.example.myapplication.ui.admin.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.example.myapplication.databinding.FragmentAdminDashboardBinding
import kotlinx.coroutines.launch

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AdminViewModel by viewModels {
        AdminViewModelFactory((requireActivity().application as App).scheduleRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminDashboardBinding.bind(view)

        // Observasi status rekomendasi burnout asdos dari Gemini
        lifecycleScope.launch {
            viewModel.burnoutRecommendations.collect { recommendation ->
                binding.tvAiContent.text = recommendation
            }
        }

        binding.btnRefreshAi.setOnClickListener {
            viewModel.fetchBurnoutAnalysis()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
