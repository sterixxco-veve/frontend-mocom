package com.example.myapplication.ui.staff.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.RetrofitClient
import com.example.myapplication.data.sources.remote.json.ReplacementRequest
import com.example.myapplication.data.sources.remote.json.UserJson
import com.example.myapplication.databinding.FragmentLeaveRequestBinding
import kotlinx.coroutines.launch

class LeaveRequestFragment : Fragment(R.layout.fragment_leave_request) {

    private var _binding: FragmentLeaveRequestBinding? = null
    private val binding get() = _binding!!

    private var currentUserId = -1
    private var currentCompanyId = -1

    // Variabel penampung id asli yang dipilih dari dropdown
    private var selectedAssignmentId: Int? = null
    private var selectedReplacementUserId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLeaveRequestBinding.bind(view)

        // 1. Ambil session ID Login dari SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", android.content.Context.MODE_PRIVATE)
        currentUserId = sharedPref.getInt("LOGIN_USER_ID", -1).let {
            if (it != -1) it else requireActivity().intent.getIntExtra("EXTRA_USER_ID", -1)
        }
        currentCompanyId = sharedPref.getInt("LOGIN_COMPANY_ID", -1).let {
            if (it != -1) it else requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", -1)
        }

        // 2. Load data ke dalam dropdown menu
        loadShiftsDropdown()
        loadStaffDropdown()

        // 3. Logika pencetan tombol kirim formulir
        binding.btnSubmitRequest.setOnClickListener {
            executeSubmitRequest()
        }
    }

    private fun loadShiftsDropdown() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Ambil objek response mentah dari Retrofit
                val response = RetrofitClient.apiService.getAssignmentsByUserId(currentUserId)

                // 2. Cek apakah koneksi HTTP sukses & datanya tidak kosong murni
                if (response.isSuccessful && response.body() != null) {

                    // 3. Ekstrak data array aslinya ke variabel baru
                    val dataMurniShift = response.body()!!

                    if (dataMurniShift.isNotEmpty()) {
                        // Proses mapping string dari dataMurniShift
                        val shiftLabels = dataMurniShift.map { "${it.title} (${it.location})" }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, shiftLabels)

                        binding.actvShift.setAdapter(adapter)

                        // Tangkap item keberapa yang di-klik oleh user
                        binding.actvShift.setOnItemClickListener { parent, _, position, _ ->
                            val selectedLabel = parent.getItemAtPosition(position) as String
                            val matchedShift = dataMurniShift.find { "${it.title} (${it.location})" == selectedLabel }
                            selectedAssignmentId = matchedShift?.assignment_id
                        }

                        // Cek jika terdapat argumen pemindah jadwal dari fragment luar (pre-filled)
                        val argAssignmentId = arguments?.getInt("EXTRA_ASSIGNMENT_ID", -1) ?: -1
                        if (argAssignmentId != -1) {
                            val matchedShift = dataMurniShift.find { it.assignment_id == argAssignmentId }
                            if (matchedShift != null) {
                                selectedAssignmentId = matchedShift.assignment_id
                                val label = "${matchedShift.title} (${matchedShift.location})"
                                binding.actvShift.setText(label, false)
                            }
                        }
                    } else {
                        binding.actvShift.setHint("Anda tidak memiliki shift aktif.")
                    }
                } else {
                    Toast.makeText(requireContext(), "Server memberikan respon gagal", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal memuat jadwal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadStaffDropdown() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val allUsers = RetrofitClient.apiService.getUsersByCompanyId(currentCompanyId)

                // 💡 FILTER UTAMA: Singkirkan ID kita sendiri agar tidak muncul di pilihan pengganti
                val filteredStaff = allUsers.filter { it.id != currentUserId }

                if (filteredStaff.isNotEmpty()) {
                    val staffLabels = filteredStaff.map { it.full_name ?: it.username ?: "Staff" }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, staffLabels)
                    binding.actvStaff.setAdapter(adapter)

                    binding.actvStaff.setOnItemClickListener { parent, _, position, _ ->
                        val selectedLabel = parent.getItemAtPosition(position) as String
                        val matchedStaff = filteredStaff.find { (it.full_name ?: it.username ?: "Staff") == selectedLabel }
                        selectedReplacementUserId = matchedStaff?.id
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal memuat rekan kerja: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun executeSubmitRequest() {
        val reasonInput = binding.etReason.text.toString().trim()

        // Validasi input manual sebelum menembak server
        if (selectedAssignmentId == null) {
            Toast.makeText(requireContext(), "Silakan pilih shift tugas terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedReplacementUserId == null) {
            Toast.makeText(requireContext(), "Silakan pilih rekan kerja pengganti!", Toast.LENGTH_SHORT).show()
            return
        }
        if (reasonInput.isEmpty()) {
            binding.tilReason.error = "Alasan izin tidak boleh kosong!"
            return
        } else {
            binding.tilReason.error = null
        }

        // Kunci tombol agar tidak terjadi double-send
        binding.btnSubmitRequest.isEnabled = false
        binding.btnSubmitRequest.text = "Mengirim..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val payload = ReplacementRequest(
                    assignmentId = selectedAssignmentId!!,
                    requestedBy = currentUserId,
                    replacementUserId = selectedReplacementUserId!!,
                    reason = reasonInput
                )

                val response = RetrofitClient.apiService.insertReplacements(payload)
                binding.btnSubmitRequest.isEnabled = true
                binding.btnSubmitRequest.text = "Kirim Permohonan Izin"

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), response.body()?.message ?: "Permohonan sukses terkirim!", Toast.LENGTH_LONG).show()

                    // Reset field form setelah sukses
                    binding.etReason.text?.clear()
                    binding.actvShift.text?.clear()
                    binding.actvStaff.text?.clear()
                    selectedAssignmentId = null
                    selectedReplacementUserId = null
                } else {
                    Toast.makeText(requireContext(), "Gagal mengirim permohonan ke server.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.btnSubmitRequest.isEnabled = true
                binding.btnSubmitRequest.text = "Kirim Permohonan Izin"
                Toast.makeText(requireContext(), "Koneksi Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}