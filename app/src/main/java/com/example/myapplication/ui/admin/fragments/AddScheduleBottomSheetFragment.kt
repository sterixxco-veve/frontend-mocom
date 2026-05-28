package com.example.myapplication.ui.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddScheduleBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: AdminViewModel by viewModels({ requireParentFragment() }) {
        AdminViewModelFactory((requireActivity().application as App).scheduleRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)

        // Ambil referensi EditText untuk menampilkan teks tanggal terpilih
        val etStartTime = view.findViewById<EditText>(R.id.etStartTime)
        val etEndTime = view.findViewById<EditText>(R.id.etEndTime)

        // 1. GANTI/TAMBAH: Ambil referensi TextInputLayout induk agar bisa mendeteksi klik dengan lancar
        val tilStartTime = view.findViewById<TextInputLayout>(R.id.tilStartTime)
        val tilEndTime = view.findViewById<TextInputLayout>(R.id.tilEndTime)

        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // Format standar sinkronisasi database MySQL Node.js
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateTimeStr = sdf.format(Date())

        // Set waktu default saat form pertama dibuka
        etStartTime.setText(currentDateTimeStr)
        etEndTime.setText(currentDateTimeStr)

        // 2. GANTI: Pasang Click Listener pada TextInputLayout (bukan EditText-nya langsung)
        tilStartTime.setOnClickListener {
            showDateTimePicker(etStartTime, sdf)
        }
        // Pasang juga pada EditText-nya sebagai cadangan jikalau user menekan tepat di tengah teks
        etStartTime.setOnClickListener {
            showDateTimePicker(etStartTime, sdf)
        }

        tilEndTime.setOnClickListener {
            showDateTimePicker(etEndTime, sdf)
        }
        etEndTime.setOnClickListener {
            showDateTimePicker(etEndTime, sdf)
        }

        // 3. AKTIFKAN KEMBALI: Logika penyimpanan data jadwal baru
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val startTimeStr = etStartTime.text.toString().trim()
            val endTimeStr = etEndTime.text.toString().trim()

            if (title.isEmpty() || startTimeStr.isEmpty() || endTimeStr.isEmpty()) {
                Toast.makeText(context, "Kolom utama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                // Mengonversi teks string rapi dari picker menjadi angka Long milidetik
                val startTimeLong: Long = sdf.parse(startTimeStr)?.time ?: Date().time
                val endTimeLong: Long = sdf.parse(endTimeStr)?.time ?: Date().time
                val createdAtLong: Long = Date().time

                val newSchedule = Schedule(
                    id = 0,
                    created_by = 1, // Default Admin EduStaff Pro
                    title = title,
                    description = if (description.isEmpty()) null else description,
                    start_time = startTimeLong,
                    end_time = endTimeLong,
                    location = if (location.isEmpty()) null else location,
                    created_at = createdAtLong
                )

                // 4. JALANKAN: Memasukkan data ke viewmodel untuk dikirim ke database SQL murni via Node.js
                viewModel.addSchedule(newSchedule) { success ->
                    if (success) {
                        Toast.makeText(context, "Jadwal berhasil ditambahkan!", Toast.LENGTH_SHORT).show()

                        // Picu reload/getAll() otomatis pada halaman utama setelah sukses input data baru
                        viewModel.loadSchedules()

                        // Menutup BottomSheet setelah sukses menyimpan
                        dismiss()
                    } else {
                        Toast.makeText(context, "Gagal menyimpan ke database server", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Terjadi kesalahan format data waktu!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 5. TAMBAHKAN: Fungsi dialog pembantu untuk memicu kemunculan Date & Time Picker berurutan
    private fun showDateTimePicker(targetEditText: EditText, formatter: SimpleDateFormat) {
        // Inisialisasi Google Material Date Picker
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .build()

        datePicker.show(childFragmentManager, "ADD_SCHEDULE_DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection

            // Lanjut otomatis memicu Material Time Picker (24 Jam) setelah tanggal dipilih
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                .setTitleText("Pilih Jam & Menit")
                .build()

            timePicker.show(childFragmentManager, "ADD_SCHEDULE_TIME_PICKER")

            timePicker.addOnPositiveButtonClickListener {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                calendar.set(Calendar.SECOND, 0)

                // Set teks terformat otomatis ke EditText target tanpa ketik manual
                targetEditText.setText(formatter.format(calendar.time))
            }
        }
    }
}