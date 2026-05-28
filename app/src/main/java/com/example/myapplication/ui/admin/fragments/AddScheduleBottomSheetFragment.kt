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
import java.text.SimpleDateFormat
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
        val etStartTime = view.findViewById<EditText>(R.id.etStartTime)
        val etEndTime = view.findViewById<EditText>(R.id.etEndTime)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // Format untuk membantu user menginput teks teks tanggal yang valid
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateTimeStr = sdf.format(Date())

        etStartTime.setText(currentDateTimeStr)
        etEndTime.setText(currentDateTimeStr)

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
                // FIX: Mengonversi format String teks dari EditText menjadi angka Long (milidetik)
                val startTimeLong: Long = sdf.parse(startTimeStr)?.time ?: Date().time
                val endTimeLong: Long = sdf.parse(endTimeStr)?.time ?: Date().time
                val createdAtLong: Long = Date().time

                // Sekarang constructor Schedule sukses menerima data Long
                val newSchedule = Schedule(
                    id = 0,
                    created_by = 1,
                    title = title,
                    description = if (description.isEmpty()) null else description,
                    start_time = startTimeLong, // Mengirimkan Long
                    end_time = endTimeLong,     // Mengirimkan Long
                    location = if (location.isEmpty()) null else location,
                    created_at = createdAtLong  // Mengirimkan Long
                )

//                viewModel.addSchedule(newSchedule) { success ->
//                    if (success) {
//                        Toast.makeText(context, "Jadwal berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
//                        dismiss()
//                    } else {
//                        Toast.makeText(context, "Gagal menyimpan ke database lokal", Toast.LENGTH_SHORT).show()
//                    }
//                }

            } catch (e: Exception) {
                // Menangkap error jika user memasukkan format teks tanggal yang salah/rusak
                Toast.makeText(context, "Format tanggal salah! Gunakan: yyyy-MM-dd HH:mm:ss", Toast.LENGTH_LONG).show()
            }
        }
    }
}