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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.ui.staff.adapter.BroadcastStaffAdapter
import com.example.myapplication.ui.staff.adapters.ShiftTodayAdapter
import kotlin.text.Charsets
import androidx.appcompat.app.AlertDialog
import android.widget.Button

class StaffHomeFragment : Fragment(R.layout.fragment_staff_home), NfcAdapter.ReaderCallback {

    private var _binding: FragmentStaffHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StaffHomeViewModel by viewModels {

        val app = requireActivity().application as App

        StaffHomeViewModelFactory(
            RetrofitClient.apiService,

            app.userRepository
        )

    }

    private var nfcAdapter: NfcAdapter? = null
    private var currentUserId = -1
    private var nfcDialog: AlertDialog? = null
    private lateinit var announcementAdapter: BroadcastStaffAdapter

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
        val staffName = sharedPref.getString("LOGIN_USER_NAME", "Staff")
        binding.tvGreeting.text = "Halo, $staffName!"

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())

        binding.btnCheckIn.setOnClickListener {
            activateNfcScanner()
        }

        announcementAdapter = BroadcastStaffAdapter()
        binding.rvAnnouncements.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = announcementAdapter
            
            // Tambahkan snap helper agar RecyclerView berperilaku seperti komidi putar (Carousel)
            val snapHelper = androidx.recyclerview.widget.PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
        }

        val companyId =
            requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", -1)

        viewModel.loadAnnouncements(companyId)

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

        // Tampilkan dialog scanning nfc kustom kita
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_nfc_scanning, null)
        builder.setView(dialogView)
        builder.setCancelable(false)

        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_nfc)
        btnCancel.setOnClickListener {
            isScanningMode = false
            binding.btnCheckIn.isEnabled = true
            binding.btnCheckIn.text = if (currentAction == "CHECK_IN") "Mulai Check In" else "Mulai Check Out"
            nfcDialog?.dismiss()
            Toast.makeText(requireContext(), "Pemindaian NFC dibatalkan", Toast.LENGTH_SHORT).show()
        }

        nfcDialog = builder.create().apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        nfcDialog?.show()
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
            nfcDialog?.dismiss()
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
                        binding.tvAttendance.text = "Belum Melakukan Absen Hari Ini"
                        binding.tvAttendance.setTextColor(Color.parseColor("#64748B"))
                    } else if (attendance.check_out.isNullOrEmpty()) {
                        // Kasus B: Sudah check-in tapi kolom check-out masih kosong murni
                        currentAction = "CHECK_OUT"
                        binding.btnCheckIn.text = "Mulai Check Out"
                        binding.btnCheckIn.isEnabled = true
                        
                        val inTime = formatTime(attendance.check_in)
                        binding.tvAttendance.text = "Check-In: $inTime (Menunggu Check-Out)"
                        binding.tvAttendance.setTextColor(Color.parseColor("#E65100"))
                    } else {
                        // Kasus C: Keduanya sudah terpenuhi (Selesai shift tugas)
                        currentAction = "DONE"
                        binding.btnCheckIn.text = "Sudah Absen Hari Ini (Selesai)"
                        binding.btnCheckIn.isEnabled = false
                        
                        val inTime = formatTime(attendance.check_in)
                        val outTime = formatTime(attendance.check_out)
                        binding.tvAttendance.text = "Check-In: $inTime | Check-Out: $outTime (Shift Selesai)"
                        binding.tvAttendance.setTextColor(Color.parseColor("#2E7D32"))
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
                    nfcDialog?.dismiss()
                    binding.btnCheckIn.isEnabled = true
                    binding.btnCheckIn.text = if (currentAction == "CHECK_IN") "Mulai Check In" else "Mulai Check Out"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.announcements.collectLatest { list ->
                val oneDayAgo = System.currentTimeMillis() - 86400000L
                val filteredList = list.filter { it.created_at >= oneDayAgo }
                announcementAdapter.submitList(filteredList)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getTodayAssignmentsByUserId(currentUserId)
                if (response.isSuccessful && response.body() != null) {
                    val listAllAssignments = response.body()!!

                    val parser = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    val cal = java.util.Calendar.getInstance()
                    val currentMonth = cal.get(java.util.Calendar.MONTH) // 0-11
                    val currentYear = cal.get(java.util.Calendar.YEAR)

                    // Mengatur nama bulan secara dinamis pada teks header
                    val monthNames = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
                    binding.tvShiftTitle.text = "Shift Bulan ${monthNames[currentMonth]}"

                    val monthlyList = listAllAssignments.filter { assignment ->
                        val date = try {
                            if (assignment.assigned_at != null) parser.parse(assignment.assigned_at) else null
                        } catch (e: Exception) {
                            null
                        }
                        if (date != null) {
                            val c = java.util.Calendar.getInstance().apply { time = date }
                            c.get(java.util.Calendar.MONTH) == currentMonth && c.get(java.util.Calendar.YEAR) == currentYear
                        } else {
                            val scheduleDate = try {
                                parser.parse(assignment.start_time)
                            } catch (e: Exception) {
                                null
                            }
                            if (scheduleDate != null) {
                                val c = java.util.Calendar.getInstance().apply { time = scheduleDate }
                                c.get(java.util.Calendar.MONTH) == currentMonth && c.get(java.util.Calendar.YEAR) == currentYear
                            } else {
                                false
                            }
                        }
                    }

                    // Pasang ke RecyclerView menggunakan ShiftTodayAdapter
                    binding.rvShiftToday.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvShiftToday.adapter = ShiftTodayAdapter(monthlyList)
                }
            } catch (e: Exception) {
                android.util.Log.e("HOME_SHIFT_ERROR", "Gagal load shift hari ini", e)
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

    private fun formatTime(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "--:--"
        return try {
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val date = parser.parse(dateStr)
            if (date != null) formatter.format(date) else "--:--"
        } catch (e: java.lang.Exception) {
            "--:--"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}