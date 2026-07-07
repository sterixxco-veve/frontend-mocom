package com.example.myapplication.ui.admin.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAdminReplacementBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.example.myapplication.ui.admin.adapter.ReplacementAdapter

class AdminReplacementFragment:Fragment(R.layout.fragment_admin_replacement){

    private var _binding:FragmentAdminReplacementBinding?=null

    private val binding get()=_binding!!

    private lateinit var adapter:ReplacementAdapter

    private val viewModel:AdminViewModel by viewModels{

        val app=requireActivity().application as App

        AdminViewModelFactory(

            app.scheduleRepository,

            app.userRepository,

            app.attendanceRepository,

            app.announcementRepository,

            app.replacementRepository

        )

    }

    override fun onViewCreated(view:View,savedInstanceState:Bundle?){

        super.onViewCreated(view,savedInstanceState)

        _binding=FragmentAdminReplacementBinding.bind(view)

        adapter=ReplacementAdapter{

            val bundle=Bundle()

            bundle.putInt("replacement_id",it.id)

            findNavController().navigate(

                R.id.action_replacement_to_detail,

                bundle

            )

        }

        binding.rvReplacement.layoutManager=LinearLayoutManager(requireContext())

        binding.rvReplacement.adapter=adapter

        viewModel.replacementRequests.observe(viewLifecycleOwner){

            adapter.submitList(it)

        }

        val pref = requireActivity().getSharedPreferences(
            "EduStaffSession",
            Context.MODE_PRIVATE
        )

        val companyId = pref.getInt(
            "LOGIN_COMPANY_ID",
            0
        )

        Log.d("COMPANY", companyId.toString())

        viewModel.loadReplacementRequests(companyId)

    }

    override fun onDestroyView(){

        super.onDestroyView()

        _binding=null

    }

}