package com.example.myapplication.ui.admin.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAdminReplacementDetailBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory

class AdminReplacementDetailFragment :
    Fragment(R.layout.fragment_admin_replacement_detail) {

    private var _binding:
            FragmentAdminReplacementDetailBinding? = null

    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by viewModels {

        val app = requireActivity().application as App

        AdminViewModelFactory(

            app.scheduleRepository,

            app.userRepository,

            app.attendanceRepository,

            app.announcementRepository,

            app.replacementRepository

        )

    }

    private var replacementId = -1

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        _binding =
            FragmentAdminReplacementDetailBinding.bind(view)

        replacementId =
            requireArguments().getInt("replacement_id")

        Log.d("DETAIL", "replacementId = $replacementId")

        viewModel.loadReplacementDetail(
            replacementId
        )

        viewModel.replacementDetail.observe(viewLifecycleOwner){

            binding.tvRequester.text =
                "Requester : ${it.requesterName}"

            binding.tvReplacement.text =
                "Pengganti : ${it.replacementName}"

            binding.tvSchedule.text =
                "${it.title}\n${it.location}"

            binding.tvTime.text =
                "${it.startTime} - ${it.endTime}"

            binding.tvReason.text =
                it.reason

        }

        val pref=requireActivity()
            .getSharedPreferences(
                "EduStaffSession",
                Context.MODE_PRIVATE
            )

        val adminId=
            pref.getInt(
                "LOGIN_USER_ID",
                0
            )

        binding.btnApprove.setOnClickListener{

            viewModel.approveReplacement(

                replacementId,

                adminId

            )

        }

        binding.btnReject.setOnClickListener{

            viewModel.rejectReplacement(

                replacementId

            )

        }

        viewModel.replacementAction.observe(viewLifecycleOwner){

            if(it){

                Toast.makeText(

                    requireContext(),

                    "Berhasil",

                    Toast.LENGTH_SHORT

                ).show()

                findNavController().popBackStack()

            }else{

                Toast.makeText(

                    requireContext(),

                    "Gagal",

                    Toast.LENGTH_SHORT

                ).show()

            }

        }

    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

}