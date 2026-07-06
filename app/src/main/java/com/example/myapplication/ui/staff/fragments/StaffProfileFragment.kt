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
import com.example.myapplication.databinding.FragmentStaffProfileBinding
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.profile.ProfileViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.tech.Ndef
import androidx.navigation.fragment.findNavController
import kotlin.text.Charsets

class StaffProfileFragment : Fragment(R.layout.fragment_staff_profile), NfcAdapter.ReaderCallback {

    private var _binding: FragmentStaffProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(RetrofitClient.apiService)
    }

    private var nfcAdapter: NfcAdapter? = null
    private var currentUserId = -1

    // PERBAIKAN: Penanda status apakah aplikasi sedang siap merekam kartu atau tidak
    private var isScanningMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStaffProfileBinding.bind(view)

        // Ambil ID asli dari session login (Gak di-hardcode lagi)
        val sharedPref = requireActivity().getSharedPreferences("EduStaffSession", android.content.Context.MODE_PRIVATE)
        currentUserId = sharedPref.getInt("LOGIN_USER_ID", -1)

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        if (nfcAdapter == null) {
            binding.tvNfcStatusText.text = "Perangkat Tidak Mendukung Fitur NFC"
            binding.viewNfcIndicator.setBackgroundColor(Color.RED)
            binding.btnStartNfcScan.isEnabled = false
        }

        binding.btnStartNfcScan.setOnClickListener {
            startNfcListeningMode()
        }

        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(
                R.id.action_staffProfileFragment_to_changePasswordFragment
            )
        }

        viewModel.loadUserProfile(currentUserId)
        setupObservers()
    }

    private fun startNfcListeningMode() {
        if (nfcAdapter == null) return

        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(requireContext(), "Silakan aktifkan fitur NFC di pengaturan HP Anda terlebih dahulu!", Toast.LENGTH_LONG).show()
            return
        }

        // Ubah status UI menjadi mode bersiap/menunggu
        isScanningMode = true
        binding.viewNfcIndicator.setBackgroundColor(Color.parseColor("#FF9800")) // Warna Oranye (Waiting)
        binding.tvNfcStatusText.text = "Status NFC: Menunggu Tempelan Kartu..."
        binding.tvNfcUidValue.text = "UID: Sedang membaca..."

        binding.btnStartNfcScan.isEnabled = false
        binding.btnStartNfcScan.text = "Mendengarkan Kartu..."
    }

    // Fungsi hardware otomatis terpanggil dari background thread OS Android saat kartu menyentuh HP
    override fun onTagDiscovered(tag: Tag?) {
        if (!isScanningMode) return

        // 1. Baca UID Asli bawaan pabrik yang selalu stabil
        val idBytes = tag?.id ?: return
        val realHardwareUid = idBytes.joinToString("") { String.format("%02X", it) }

        // 2. Generate UID Virtual acak (Pura-pura kartunya baru)
        val allowedChars = ('A'..'Z') + ('0'..'9')
        val virtualNfcUid = "VIRT-" + (1..6).map { allowedChars.random() }.joinToString("")

        // 3. SIMPAN MAPPING-NYA KE HP (Mengikat UID Asli dengan UID Virtual)
        val sharedPref = requireActivity().getSharedPreferences("NfcMappingSession", android.content.Context.MODE_PRIVATE)
        sharedPref.edit().putString(realHardwareUid, virtualNfcUid).apply()

        // Kembalikan alur pekerjaan ke Main Thread UI
        requireActivity().runOnUiThread {
            isScanningMode = false
            binding.viewNfcIndicator.setBackgroundColor(Color.parseColor("#4CAF50")) // Hijau sukses
            binding.tvNfcStatusText.text = "Status NFC: Virtual UID Sukses Terbentuk!"
            binding.tvNfcUidValue.text = "UID Topeng: $virtualNfcUid"
            binding.btnStartNfcScan.isEnabled = true
            binding.btnStartNfcScan.text = "Mulai Registrasi Kartu"

            // 4. Kirim UID Virtual acak ini menuju database Node.js
            viewModel.registerNfcCard(currentUserId, virtualNfcUid)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collectLatest { user ->
                if (user != null) {
                    binding.tvProfileName.text = user.full_name
                    binding.tvProfileUsername.text = "@${user.username}"
                    binding.tvProfileEmail.text = user.email

                    val roleLabel = when (user.role_id) {
                        1 -> "Administrator Utama"
                        2 -> "Staff Lapangan Resmi"
                        else -> "Member Terdaftar"
                    }
                    binding.tvProfileRole.text = roleLabel
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toastMessage.collectLatest { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                // Jika server merespon dengan kegagalan, reset tombol agar bisa diklik ulang oleh user
                if (msg.contains("Gagal") || msg.contains("Server Error")) {
                    binding.viewNfcIndicator.setBackgroundColor(Color.parseColor("#94A3B8")) // Abu-abu default
                    binding.tvNfcStatusText.text = "Status NFC: Gagal, Coba Lagi"
                    binding.btnStartNfcScan.isEnabled = true
                    binding.btnStartNfcScan.text = "Mulai Registrasi Kartu"
                }
            }
        }
    }

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