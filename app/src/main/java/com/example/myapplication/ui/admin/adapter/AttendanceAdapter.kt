package com.example.myapplication.ui.admin.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.sources.models.Attendance // 💡 Murni menggunakan model domain Attendance
import com.example.myapplication.databinding.ItemAttendanceBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceAdapter(
    // 💡 KUNCI 1: List utama menggunakan model Attendance
    private var attendanceList: List<Attendance>
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemAttendanceBinding.bind(itemView)
        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        // 💡 KUNCI 2: Fungsi bind menerima model Attendance yang sama
        fun bind(attendance: Attendance) {
            binding.tvEmployeeName.text = "Staff Penugasan #${attendance.assignment_id}"

            // Format tipe data Long ke String Jam secara aman
            val checkInTime = if (attendance.check_in != null && attendance.check_in > 0) {
                timeFormatter.format(Date(attendance.check_in))
            } else {
                "--:--"
            }

            val checkOutTime = if (attendance.check_out != null && attendance.check_out > 0) {
                timeFormatter.format(Date(attendance.check_out))
            } else {
                "--:--"
            }

            binding.tvTimeCheckIn.text = "In: $checkInTime"
            binding.tvTimeCheckOut.text = "Out: $checkOutTime"
            binding.tvAvatarInitials.text = "ST"

            // Pewarnaan Badge Status Kehadiran
            when (attendance.status.lowercase(Locale.getDefault())) {
                "present" -> {
                    binding.tvAttendanceBadge.text = "Present"
                    binding.tvAttendanceBadge.setTextColor(Color.parseColor("#2E7D32"))
                    binding.tvAttendanceBadge.setBackgroundResource(android.R.drawable.toast_frame)
                    binding.tvAttendanceBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                }
                "late" -> {
                    binding.tvAttendanceBadge.text = "Late"
                    binding.tvAttendanceBadge.setTextColor(Color.parseColor("#E65100"))
                    binding.tvAttendanceBadge.setBackgroundResource(android.R.drawable.toast_frame)
                    binding.tvAttendanceBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
                }
                "absent" -> {
                    binding.tvAttendanceBadge.text = "Absent"
                    binding.tvAttendanceBadge.setTextColor(Color.parseColor("#C62828"))
                    binding.tvAttendanceBadge.setBackgroundResource(android.R.drawable.toast_frame)
                    binding.tvAttendanceBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFEBEE"))
                }
                else -> {
                    binding.tvAttendanceBadge.text = attendance.status
                    binding.tvAttendanceBadge.setTextColor(Color.parseColor("#37474F"))
                    binding.tvAttendanceBadge.setBackgroundResource(android.R.drawable.toast_frame)
                    binding.tvAttendanceBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ECEFF1"))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendanceViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        // 👍 SEKARANG SINKRON: Tipe data list [Attendance] masuk ke parameter bind [Attendance]
        holder.bind(attendanceList[position])
    }

    override fun getItemCount(): Int = attendanceList.size

    // 💡 KUNCI 3: Parameter submitList disesuaikan ke bentuk Attendance
    fun submitList(newList: List<Attendance>) {
        this.attendanceList = newList
        notifyDataSetChanged()
    }
}