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
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AdminStaffAssignmentBottomSheetFragment(private val selectedSchedule: Schedule) : BottomSheetDialogFragment() {

    private val viewModel: AdminViewModel by viewModels({ requireActivity() }) {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository, app.attendanceRepository)
    }

    // List penampung staff yang sudah terfilter role_id = 2
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
        // 💡 PERBAIKAN UTAMA: Tambahkan penyaringan .filter { it.role_id == 2 }
        // =========================================================================
        viewModel.users.observe(viewLifecycleOwner) { listUsers ->
            if (listUsers != null) {
                // 🎯 Saring runtime: Hanya ambil user yang memiliki role_id bernilai 2
                currentStaffList = listUsers.filter { it.role_id == 2 }

                // Ambil daftar nama lengkap dari hasil staff yang sudah terfilter saja
                val staffNames = currentStaffList.map { it.full_name }

                val adapterDropdown = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    staffNames
                )
                actvStaff.setAdapter(adapterDropdown)
            }
        }

        // Ambil ID perusahaan aktif untuk memicu penarikan data dari cloud MySQL
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", Context.MODE_PRIVATE)
        val currentCompanyId = sharedPref.getInt("LOGIN_COMPANY_ID", 1)

        viewModel.loadUserByCompanyId(currentCompanyId)

        // Logika Validasi Ketat Klik Tombol Konfirmasi Penugasan
        btnConfirmAssignment.setOnClickListener {
            val inputNama = actvStaff.text.toString().trim()

            if (inputNama.isEmpty()) {
                Toast.makeText(context, "Silakan pilih staff terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cari nama staf yang diinput dari list terfilter
            val staffTerpilih = currentStaffList.find { it.full_name.equals(inputNama, ignoreCase = true) }

            if (staffTerpilih == null) {
                actvStaff.setError("Nama tidak terdaftar sebagai Staff! Pilih nama dari list dropdown.")
                Toast.makeText(context, "Staff tidak valid! Pilih dari daftar yang tersedia.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val staffIdDariSql = staffTerpilih.id

            // Siap ditembakkan ke endpoint penugasan HTTP POST kamu berikutnya, Bob!
            Toast.makeText(context, "Valid! Menugaskan ${staffTerpilih.full_name} (ID #$staffIdDariSql)", Toast.LENGTH_SHORT).show()
        }
    }
}