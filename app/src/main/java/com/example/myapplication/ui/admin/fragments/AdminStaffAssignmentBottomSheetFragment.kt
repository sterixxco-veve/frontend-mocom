package com.example.myapplication.ui.admin.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.databinding.FragmentAdminStaffAssignmentBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AdminStaffAssignmentBottomSheetFragment(private val selectedSchedule: Schedule) : BottomSheetDialogFragment() {

    // Gunakan View Binding untuk fragment
    private var _binding: FragmentAdminStaffAssignmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by viewModels({ requireActivity() }) {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository, app.attendanceRepository)
    }

    private var currentStaffList: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminStaffAssignmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSelectedSchedule.text = "Jadwal: ${selectedSchedule.title} (${selectedSchedule.location})"

        // Mengamati data user (Staff)
        viewModel.users.observe(viewLifecycleOwner) { listUsers ->
            if (listUsers != null) {
                currentStaffList = listUsers.filter { it.role_id == 2 }
                val staffNames = currentStaffList.map { it.full_name }

                val adapterDropdown = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    staffNames
                )
                binding.actvStaff.setAdapter(adapterDropdown)
            }
        }

        // 💡 OPSIONAL: Ambil response/status simpan dari ViewModel (jika Anda membuat livedata status)
        // viewModel.assignmentResult.observe(viewLifecycleOwner) { isSuccess ->
        //     if (isSuccess) {
        //         Toast.makeText(context, "Penugasan Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
        //         dismiss() // Tutup BottomSheet jika berhasil
        //     } else {
        //         Toast.makeText(context, "Gagal menyimpan penugasan.", Toast.LENGTH_SHORT).show()
        //     }
        // }

        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", Context.MODE_PRIVATE)
        val currentCompanyId = sharedPref.getInt("LOGIN_COMPANY_ID", 1)
        viewModel.loadUserByCompanyId(currentCompanyId)

        // Logika Klik Tombol Konfirmasi Penugasan
        binding.btnConfirmAssignment.setOnClickListener {
            val inputNama = binding.actvStaff.text.toString().trim()

            if (inputNama.isEmpty()) {
                binding.actvStaff.error = "Silakan pilih staff terlebih dahulu!"
                return@setOnClickListener
            }

            val staffTerpilih = currentStaffList.find { it.full_name.equals(inputNama, ignoreCase = true) }

            if (staffTerpilih == null) {
                binding.actvStaff.error = "Nama tidak terdaftar sebagai Staff!"
                return@setOnClickListener
            }

            // Di dalam btnConfirmAssignment.setOnClickListener fragment Anda:
            val staffIdDariSql = staffTerpilih.id
            val scheduleId = selectedSchedule.id

// Tampilkan loading/proses berjalan awal
            Toast.makeText(context, "Sedang menugaskan ${staffTerpilih.full_name}...", Toast.LENGTH_SHORT).show()

// Panggil fungsi ViewModel dengan callback onResult
            viewModel.assignStaffToSchedule(scheduleId, staffIdDariSql) { isSuccess ->
                if (isSuccess) {
                    Toast.makeText(context, "Penugasan ${staffTerpilih.full_name} BERHASIL disimpan!", Toast.LENGTH_SHORT).show()
                    dismiss() // Tutup bottom sheet hanya jika sukses disimpan ke DB
                } else {
                    Toast.makeText(context, "Gagal menyimpan penugasan ke database. Coba lagi!", Toast.LENGTH_LONG).show()
                }
            }

            // Jika Anda belum membuat LiveData penampung response di atas, Anda bisa langsung dismiss di sini sementara:
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Hindari memory leak
    }
}