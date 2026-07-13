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

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.replacementDetail.observe(viewLifecycleOwner){

            binding.tvRequester.text =
                "Requester : ${it.requesterName}"

            binding.tvReplacement.text =
                "Pengganti : ${it.replacementName}"

            binding.tvSchedule.text =
                "${it.title}\n${it.location}"

            binding.tvTime.text =
                "${it.startTime} - ${it.endTime}"

            binding.tvCreatedAt.text =
                formatDateTime(it.createdAt)

            binding.tvReason.text =
                it.reason

            // Jika status sudah tidak pending (yaitu approved / rejected), sembunyikan tombol dan tampilkan TextView keterangan di pojok kanan atas
            val currentStatus = it.status.lowercase()
            if (currentStatus == "approved" || currentStatus == "rejected") {
                binding.layoutButtons.visibility = View.GONE
                binding.tvStatusDetail.visibility = View.VISIBLE
                
                val shape = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 24f // Membuat sudut membulat (pill badge)
                }
                
                if (currentStatus == "approved") {
                    binding.tvStatusDetail.text = "APPROVED"
                    binding.tvStatusDetail.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                    shape.setColor(android.graphics.Color.parseColor("#E8F5E9")) // Background Hijau Muda
                } else {
                    binding.tvStatusDetail.text = "REJECTED"
                    binding.tvStatusDetail.setTextColor(android.graphics.Color.parseColor("#C62828"))
                    shape.setColor(android.graphics.Color.parseColor("#FFEBEE")) // Background Merah Muda
                }
                binding.tvStatusDetail.background = shape
            } else {
                binding.layoutButtons.visibility = View.VISIBLE
                binding.tvStatusDetail.visibility = View.GONE
            }

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

    private fun formatDateTime(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "-"
        return try {
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val formatter = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getDefault()
            }
            val date = parser.parse(dateStr)
            if (date != null) formatter.format(date) else dateStr
        } catch (e: Exception) {
            try {
                val parser = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val formatter = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault())
                val date = parser.parse(dateStr)
                if (date != null) formatter.format(date) else dateStr
            } catch (e2: Exception) {
                dateStr ?: "-"
            }
        }
    }
}