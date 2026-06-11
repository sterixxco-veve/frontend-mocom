package com.example.myapplication.ui.admin.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.models.User // 💡 IMPORT MODEL USER MURNI
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AdminStaffAssignmentBottomSheetFragment(private val selectedSchedule: Schedule) : BottomSheetDialogFragment() {

    // =========================================================================
    // 💡 PERBAIKAN 1: Inisialisasi ViewModel Factory dengan 2 Repositori agar seragam
    // =========================================================================
    private val viewModel: AdminViewModel by viewModels({ requireActivity() }) {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository, app.attendanceRepository)
    }

    // 💡 PERBAIKAN 2: Ubah dari List<UserJson> menjadi List<User> murni
    private var currentStaffList: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_staff_assignment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvSelectedSchedule = view.findViewById<TextView>(R.id.tvSelectedSchedule)
        val actvStaff = view.findViewById<AutoCompleteTextView>(R.id.actvStaff)
        val btnConfirmAssignment = view.findViewById<Button>(R.id.btnConfirmAssignment)

        tvSelectedSchedule.text = "Jadwal: ${selectedSchedule.title} (${selectedSchedule.location})"

        // =========================================================================
        // 💡 PERBAIKAN 3: Amati LiveData 'userList' (List<User>) yang sudah kita buat kemarin
        // =========================================================================
        viewModel.users.observe(viewLifecycleOwner) { listUsers ->
            if (listUsers != null) {
                currentStaffList = listUsers

                // Ambil daftar nama lengkap saja untuk ditampilkan di Dropdown teks
                val staffNames = listUsers.map { it.full_name }

                val adapterDropdown = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, staffNames)
                actvStaff.setAdapter(adapterDropdown)
            }
        }

        // =========================================================================
        // 💡 PERBAIKAN 4: Ganti loadAllUsers() menjadi loadUsersByCompany via SharedPreferences
        // =========================================================================
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", Context.MODE_PRIVATE)
        val currentCompanyId = sharedPref.getInt("LOGIN_COMPANY_ID", 1)

        viewModel.loadUserByCompanyId(currentCompanyId)

        // 3. Logika Validasi Ketat Klik Tombol Konfirmasi Penugasan
        btnConfirmAssignment.setOnClickListener {
            val inputNama = actvStaff.text.toString().trim()

            if (inputNama.isEmpty()) {
                Toast.makeText(context, "Silakan pilih staff terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cari nama staf yang diinput apakah ada di list users database
            val staffTerpilih = currentStaffList.find { it.full_name.equals(inputNama, ignoreCase = true) }

            if (staffTerpilih == null) {
                actvStaff.setError("Nama tidak terdaftar! Silakan pilih nama dari list dropdown.")
                Toast.makeText(context, "Staff tidak valid! Pilih dari daftar yang tersedia.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val staffIdDariSql = staffTerpilih.id

            // Kode pemicu HTTP POST/PUT penugasan kamu di bawah ini nanti tinggal diaktifkan...
            Toast.makeText(context, "Valid! Menugaskan ID #$staffIdDariSql", Toast.LENGTH_SHORT).show()
        }
    }
}