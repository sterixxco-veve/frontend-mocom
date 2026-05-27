package com.example.myapplication.admin.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentScheduleCrudBinding

class ScheduleCrudFragment : Fragment(R.layout.fragment_schedule_crud) {

    private var _binding: FragmentScheduleCrudBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScheduleCrudBinding.bind(view)

        binding.fabAdd.setOnClickListener {
            // Simulasi Trigger Modal Buat Jadwal Baru
            Toast.makeText(context, "Membuka Form Tambah Jadwal Baru...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}