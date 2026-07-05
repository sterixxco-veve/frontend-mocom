package com.example.myapplication.ui.staff.fragments

import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.RetrofitClient
import com.example.myapplication.databinding.FragmentStaffHomeBinding
import com.example.myapplication.ui.staff.StaffHomeViewModel
import com.example.myapplication.ui.staff.StaffHomeViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.nfc.tech.Ndef
import kotlin.text.Charsets

class StaffHomeFragment : Fragment(R.layout.fragment_staff_home), NfcAdapter.ReaderCallback {

    private var _binding: FragmentStaffHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StaffHomeViewModel by viewModels {
        StaffHomeViewModelFactory(RetrofitClient.apiService)
    }

    private var nfcAdapter: NfcAdapter? = null
    private var currentUserId = -1

    // ==========================================
    // 💡 LIVE STATE MACHINE UNTUK KENDALI NFC
    // ==========================================
    private var isScanningMode = false   // true jika tombol sudah diklik & siap scan
    private var currentAction = "CHECK_IN" // "CHECK_IN", "CHECK_OUT", atau "DONE"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStaffHomeBinding.bind(view)

        // Ambil ID dinamis dari session login
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", android.content.Context.MODE_PRIVATE)
        currentUserId = sharedPref.getInt("LOGIN_USER_ID", -1)

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())

        binding.btnCheckIn.setOnClickListener {
            activateNfcScanner()
        }

        viewModel.loadStaffHomeData(currentUserId)
        setupObservers()
    }

    // ==========================================
    // ⚡ AKTIVASI SENSOR BERDASARKAN KLIK
    // ==========================================
    private fun activateNfcScanner() {
        if (nfcAdapter == null) {
            Toast.makeText(requireContext(), "HP Anda tidak mendukung NFC", Toast.LENGTH_SHORT).show()
            return
        }
        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(requireContext(), "Silakan aktifkan NFC di pengaturan HP!", Toast.LENGTH_LONG).show()
            return
        }

        // Kunci status menjadi sedang mendengarkan pindaian kartu
        isScanningMode = true
        binding.btnCheckIn.isEnabled = false // Nonaktifkan klik tambahan selama scanning

        // Ubah teks tombol penunggu sesuai mode aksi aktif
        if (currentAction == "CHECK_IN") {
            binding.btnCheckIn.text = "⌛ Menunggu Tap Check In..."
        } else if (currentAction == "CHECK_OUT") {
            binding.btnCheckIn.text = "⌛ Menunggu Tap Check Out..."
        }

        Toast.makeText(requireContext(), "Sensor NFC Aktif! Silakan tempelkan kartu Anda.", Toast.LENGTH_SHORT).show()
    }

    // ==========================================
    // 📡 RADAR HARDWARE NFC DISCOVERED
    // ==========================================
    override fun onTagDiscovered(tag: Tag?) {
        if (!isScanningMode) return

        // 1. Baca UID Asli bawaan pabrik saat kartu di-tap buat absen
        val idBytes = tag?.id ?: return
        val realHardwareUid = idBytes.joinToString("") { String.format("%02X", it) }

        // 2. AMBIL UID VIRTUALNYA DARI MEMORI HP
        val sharedPref = requireActivity().getSharedPreferences("NfcMappingSession", android.content.Context.MODE_PRIVATE)
        // Jika kartu belum pernah diregistrasi, dia akan memakai UID aslinya sebagai cadangan
        val finalUidToSend = sharedPref.getString(realHardwareUid, realHardwareUid)

        // Kembalikan pekerjaan ke Main UI Thread Android
        requireActivity().runOnUiThread {
            isScanningMode = false
            Toast.makeText(requireContext(), "Kartu Terbaca: $finalUidToSend. Memproses...", Toast.LENGTH_SHORT).show()

            // 3. Tembak API absen menggunakan UID topeng tadi
            viewModel.checkInWithNfc(currentUserId, finalUidToSend!!)
        }
    }

    // ==========================================
    // 👁️ OBSERVERS STATUS REAL-TIME DASHBOARD
    // ==========================================
    private fun setupObservers() {
        // Pantau data log absensi hari ini untuk menentukan wujud teks tombol saat Idle
        viewLifecycleOwner.lifecycleScope.launch {
            // Catatan: Sesuaikan 'todayAttendance' dengan nama StateFlow/LiveData di ViewModel-mu
            viewModel.todayAttendance.collectLatest { attendance ->

                // Aturan teks tombol HANYA boleh berubah jika sedang TIDAK menunggu tempelan kartu
                if (!isScanningMode) {
                    if (attendance == null) {
                        // Kasus A: Belum absen sama sekali hari ini
                        currentAction = "CHECK_IN"
                        binding.btnCheckIn.text = "Mulai Check In"
                        binding.btnCheckIn.isEnabled = true
                    } else if (attendance.check_out.isNullOrEmpty()) {
                        // Kasus B: Sudah check-in tapi kolom check-out masih kosong murni
                        currentAction = "CHECK_OUT"
                        binding.btnCheckIn.text = "Mulai Check Out"
                        binding.btnCheckIn.isEnabled = true
                    } else {
                        // Kasus C: Keduanya sudah terpenuhi (Selesai shift tugas)
                        currentAction = "DONE"
                        binding.btnCheckIn.text = "Sudah Absen Hari Ini (Selesai)"
                        binding.btnCheckIn.isEnabled = false
                    }
                }
            }
        }

        // Pantau pesan Toast pop-up dari server
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toastMessage.collectLatest { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

                // Fallback pengaman: Jika terjadi kegagalan jaringan atau server menolak,
                // kembalikan kondisi tombol ke posisi normal agar bisa diklik ulang
                if (msg.contains("Gagal") || msg.contains("Error") || msg.contains("ditolak")) {
                    isScanningMode = false
                    binding.btnCheckIn.isEnabled = true
                    binding.btnCheckIn.text = if (currentAction == "CHECK_IN") "Mulai Check In" else "Mulai Check Out"
                }
            }
        }
    }

    // ==========================================
    // LIFECYCLE CONTROLLERS
    // ==========================================
    override fun onStart() {
        super.onStart()
        nfcAdapter?.enableReaderMode(
            requireActivity(),
            this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    override fun onStop() {
        super.onStop()
        nfcAdapter?.disableReaderMode(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}