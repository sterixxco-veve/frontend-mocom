package com.example.myapplication.ui.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository)
    }

    // 💡 Properti global untuk melacak mode edit data
    private var scheduleId: Int? = null
    private var currentCreatedBy: Int = 1
    private var currentCompanyId: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data User ID dan Company ID riil yang aktif dari Activity induk
        currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", 1)
        currentCreatedBy = requireActivity().intent.getIntExtra("EXTRA_USER_ID", 1)

        val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)

        val etStartTime = view.findViewById<EditText>(R.id.etStartTime)
        val etEndTime = view.findViewById<EditText>(R.id.etEndTime)
        val tilStartTime = view.findViewById<TextInputLayout>(R.id.tilStartTime)
        val tilEndTime = view.findViewById<TextInputLayout>(R.id.tilEndTime)

        val btnSave = view.findViewById<Button>(R.id.btnSave)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateTimeStr = sdf.format(Date())

        // Set waktu default awal
        etStartTime.setText(currentDateTimeStr)
        etEndTime.setText(currentDateTimeStr)

        // =========================================================================
        // 💡 LOGIKA DETEKSI MODE: Cek apakah ada data bundle kiriman (Mode Edit)
        // =========================================================================
        arguments?.let { bundle ->
            if (bundle.containsKey("EDIT_ID")) {
                scheduleId = bundle.getInt("EDIT_ID")
                currentCreatedBy = bundle.getInt("EDIT_CREATED_BY")
                currentCompanyId = bundle.getInt("EDIT_COMPANY_ID")

                // Auto-fill form inputan dengan data jadwal lama
                tvHeaderTitle.text = "Edit Jadwal"
                etTitle.setText(bundle.getString("EDIT_TITLE"))
                etDescription.setText(bundle.getString("EDIT_DESC"))
                etLocation.setText(bundle.getString("EDIT_LOCATION"))

                // Ubah format data Long kembali ke String terformat untuk form
                etStartTime.setText(sdf.format(Date(bundle.getLong("EDIT_START"))))
                etEndTime.setText(sdf.format(Date(bundle.getLong("EDIT_END"))))

                // Ganti teks tombol utama
                btnSave.text = "Perbarui Jadwal"
            }
        }

        // Setup Picker Jendela Dialog Kalender & Jam
        tilStartTime.setOnClickListener { showDateTimePicker(etStartTime, sdf) }
        etStartTime.setOnClickListener { showDateTimePicker(etStartTime, sdf) }
        tilEndTime.setOnClickListener { showDateTimePicker(etEndTime, sdf) }
        etEndTime.setOnClickListener { showDateTimePicker(etEndTime, sdf) }

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
                val startTimeLong: Long = sdf.parse(startTimeStr)?.time ?: Date().time
                val endTimeLong: Long = sdf.parse(endTimeStr)?.time ?: Date().time
                val createdAtLong: Long = Date().time

                // Bungkus menjadi satu objek Schedule utuh
                val scheduleData = Schedule(
                    id = scheduleId ?: 0, // Menggunakan ID lama jika mode edit, atau 0 jika baru
                    created_by = currentCreatedBy,
                    company_id = currentCompanyId,
                    title = title,
                    description = if (description.isEmpty()) null else description,
                    start_time = startTimeLong,
                    end_time = endTimeLong,
                    location = if (location.isEmpty()) null else location,
                    created_at = createdAtLong
                )

                // Jalankan fungsi network berdasarkan status mode saat ini
                if (scheduleId != null) {
                    // ✏️ PILIHAN A: Eksekusi Update Data (HTTP PUT)
                    viewModel.updateSchedule(scheduleData) { success ->
                        if (success) {
                            Toast.makeText(context, "Jadwal berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                            viewModel.loadSchedules(currentCompanyId) // Tarik ulang data terfilter company
                            dismiss()
                        } else {
                            Toast.makeText(context, "Gagal memperbarui jadwal di server", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // ➕ PILIHAN B: Eksekusi Tambah Data Baru (HTTP POST) seperti semula
                    viewModel.addSchedule(scheduleData, currentCompanyId) { success ->
                        if (success) {
                            Toast.makeText(context, "Jadwal berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                            viewModel.loadSchedules(currentCompanyId) // Sinkronkan ulang tampilan list
                            dismiss()
                        } else {
                            Toast.makeText(context, "Gagal menyimpan ke database server", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Terjadi kesalahan format data waktu!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDateTimePicker(targetEditText: EditText, formatter: SimpleDateFormat) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .build()

        datePicker.show(childFragmentManager, "ADD_SCHEDULE_DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection

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

                targetEditText.setText(formatter.format(calendar.time))
            }
        }
    }
}