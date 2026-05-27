package com.example.myapplication.admin.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAdminAttendanceBinding

class AdminAttendanceFragment : Fragment(R.layout.fragment_admin_attendance) {

    private var _binding: FragmentAdminAttendanceBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminAttendanceBinding.bind(view)

        binding.btnFilterAll.setOnClickListener {
            resetButtons()
            binding.btnFilterAll.setBackgroundColor(android.graphics.Color.parseColor("#06B6D4"))
            binding.btnFilterAll.setTextColor(android.graphics.Color.parseColor("#0F172A"))

            // Simulasikan menampilkan kembali seluruh card
            binding.cardAttendance1.visibility = View.VISIBLE
            binding.cardAttendance2.visibility = View.VISIBLE
            binding.cardAttendance3.visibility = View.VISIBLE

            Toast.makeText(context, "Menampilkan seluruh jadwal kehadiran hari ini", Toast.LENGTH_SHORT).show()
        }

        binding.btnFilterPresent.setOnClickListener {
            resetButtons()
            binding.btnFilterPresent.setBackgroundColor(android.graphics.Color.parseColor("#06B6D4"))
            binding.btnFilterPresent.setTextColor(android.graphics.Color.parseColor("#0F172A"))

            // Saring dan tampilkan yang hanya berstatus 'Hadir' saja
            binding.cardAttendance1.visibility = View.VISIBLE
            binding.cardAttendance2.visibility = View.GONE
            binding.cardAttendance3.visibility = View.VISIBLE

            Toast.makeText(context, "Menampilkan staff yang telah melakukan presensi", Toast.LENGTH_SHORT).show()
        }

        binding.btnFilterAbsent.setOnClickListener {
            resetButtons()
            binding.btnFilterAbsent.setBackgroundColor(android.graphics.Color.parseColor("#06B6D4"))
            binding.btnFilterAbsent.setTextColor(android.graphics.Color.parseColor("#0F172A"))

            // Saring dan tampilkan yang hanya berstatus 'Sakit/Absen' saja (Siti Rahma)
            binding.cardAttendance1.visibility = View.GONE
            binding.cardAttendance2.visibility = View.VISIBLE
            binding.cardAttendance3.visibility = View.GONE

            Toast.makeText(context, "Menampilkan staff berhalangan hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetButtons() {
        val darkSlate = android.graphics.Color.parseColor("#1E293B")
        val textSlate = android.graphics.Color.parseColor("#CBD5E1")

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