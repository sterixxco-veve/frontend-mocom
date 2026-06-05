package com.example.myapplication.ui.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.data.sources.remote.json.UserJson
import com.example.myapplication.ui.admin.AdminViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// 💡 GANTI: Sekarang menginduk ke BottomSheetDialogFragment dan menerima objek Schedule yang diklik
class AdminStaffAssignmentBottomSheetFragment(private val selectedSchedule: Schedule) : BottomSheetDialogFragment() {

    private lateinit var viewModel: AdminViewModel
    private var currentStaffList: List<UserJson> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Hubungkan langsung dengan layout XML penugasan staf yang sudah kita buat
        return inflater.inflate(R.layout.fragment_admin_staff_assignment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gunakan Shared ViewModel dengan scope Activity utama agar sinkronisasi data lancar
        viewModel = ViewModelProvider(requireActivity())[AdminViewModel::class.java]

        val tvSelectedSchedule = view.findViewById<TextView>(R.id.tvSelectedSchedule)
        val actvStaff = view.findViewById<AutoCompleteTextView>(R.id.actvStaff)
        val btnConfirmAssignment = view.findViewById<Button>(R.id.btnConfirmAssignment)

        // Set teks informasi jadwal yang terpilih
        tvSelectedSchedule.text = "Jadwal: ${selectedSchedule.title} (${selectedSchedule.location})"

        // 1. Amati perubahan LiveData Users hasil panggil backend Express.js kamu
        viewModel.users.observe(viewLifecycleOwner) { listUsers ->
            if (listUsers != null) {
                currentStaffList = listUsers

                // Masukkan daftar user riil dari SQL database ke Dropdown AutoComplete
                val adapterDropdown = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listUsers)
                actvStaff.setAdapter(adapterDropdown)
            }
        }

        // 2. Picu penarikan data user /api/getAllUsers dari server Node.js saat modal terbuka
        viewModel.loadAllUsers()

        // 3. Logika Validasi Ketat Klik Tombol Konfirmasi Penugasan
        btnConfirmAssignment.setOnClickListener {
            val inputNama = actvStaff.text.toString().trim()

            if (inputNama.isEmpty()) {
                Toast.makeText(context, "Silakan pilih staff terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // KUNCI COCOK: Cari nama staf yang diinput apakah ada di list users database
            val staffTerpilih = currentStaffList.find { it.full_name.equals(inputNama, ignoreCase = true) }

            // Jika nama asal ketik manual dan tidak terdaftar di database SQL:
            if (staffTerpilih == null) {
                actvStaff.setError("Nama tidak terdaftar! Silakan pilih nama dari list dropdown.")
                Toast.makeText(context, "Staff tidak valid! Pilih dari daftar yang tersedia.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Jika nama valid, ambil ID murni penanda primari key tabel users
            val staffIdDariSql = staffTerpilih.id

            // Kirim payload ke endpoint Node.js via ViewModel
//            viewModel.submitStaffAssignment(selectedSchedule.id, staffIdDariSql) { success ->
//                if (success) {
//                    Toast.makeText(context, "Sukses menugaskan ${staffTerpilih.full_name}!", Toast.LENGTH_SHORT).show()
//
//                    // Trigger getAllSchedules() otomatis di halaman utama biar daftar langsung ter-update
//                    viewModel.loadSchedules()
//
//                    dismiss() // Tutup lembaran modal bottom sheet
//                } else {
//                    Toast.makeText(context, "Gagal menyimpan penugasan ke database", Toast.LENGTH_SHORT).show()
//                }
//            }
        }
    }
}