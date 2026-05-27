package com.example.myapplication.ui.admin.fragments

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
            // PENTING: Pastikan nama kelas di sini cocok 100% dengan nama file BottomSheet Anda.
            // Jika sebelumnya Anda memberi nama 'AddScheduleBottomSheetFragment', ubah kodenya menjadi:
            val addScheduleBottomSheet = AddScheduleBottomSheetFragment()
            addScheduleBottomSheet.show(parentFragmentManager, "AddScheduleBottomSheet")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}