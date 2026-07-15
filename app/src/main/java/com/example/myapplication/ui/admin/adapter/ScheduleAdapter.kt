package com.example.myapplication.ui.admin.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.databinding.ItemScheduleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleAdapter(
    private var scheduleList: List<Schedule> = emptyList(),
    private val onItemClick: (Schedule) -> Unit,    // 💡 Callback Klik Item (Buka Staff Assignment)
    private val onEditClick: (Schedule) -> Unit,    // 💡 TAMBAHAN: Callback untuk Edit Jadwal
    private val onDeleteClick: (Schedule) -> Unit   // 💡 TAMBAHAN: Callback untuk Hapus Jadwal
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: Schedule) {
            binding.tvTitle.text = schedule.title
            binding.tvDescription.text = schedule.description ?: "Tidak ada deskripsi"
            binding.tvLocation.text = "📍 ${schedule.location ?: "Online / Tidak Diketahui"}"

            // Konversi dari format Timestamp Long ke format Jam (HH:mm) dan Tanggal (dd MMM yyyy)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            dateFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")

            val startTimeStr = timeFormat.format(Date(schedule.start_time))
            val endTimeStr = timeFormat.format(Date(schedule.end_time))
            val dateStr = dateFormat.format(Date(schedule.start_time))
            binding.tvTime.text = "🕒 $dateStr | $startTimeStr - $endTimeStr"

            // Bind Status Penugasan
            val status = schedule.assignmentStatus
            val staff = schedule.staffName
            if (status != null && staff != null) {
                binding.tvAssignmentStatus.text = "Petugas: $staff (${status.uppercase()})"
                if (status.lowercase() == "accepted") {
                    binding.tvAssignmentStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                } else if (status.lowercase() == "pending") {
                    binding.tvAssignmentStatus.setTextColor(android.graphics.Color.parseColor("#F57C00"))
                } else {
                    binding.tvAssignmentStatus.setTextColor(android.graphics.Color.parseColor("#64748B"))
                }
            } else {
                binding.tvAssignmentStatus.text = "Belum ditugaskan"
                binding.tvAssignmentStatus.setTextColor(android.graphics.Color.parseColor("#64748B"))
            }

            android.util.Log.d("DEBUG_STATUS", "Judul: ${schedule.title} | Status: ${schedule.assignmentStatus} | Staff: ${schedule.staffName}")
            android.util.Log.d("DEBUG_JAM_EDUSTAFF", "Judul Jadwal: ${schedule.title}")
            android.util.Log.d("DEBUG_JAM_EDUSTAFF", "Tipe Data / Isi Start: ${schedule.start_time}")
            android.util.Log.d("DEBUG_JAM_EDUSTAFF", "Tipe Data / Isi End  : ${schedule.end_time}")

            // Listener ketika seluruh area kartu jadwal diklik
            binding.root.setOnClickListener {
                onItemClick(schedule)
            }

            binding.btnMenuOptions.setOnClickListener { view ->
                val context = view.context

                // 1. Buat instansiasi PopupMenu terikat pada tombol titik tiga
                val popupMenu = PopupMenu(context, view)

                // 2. Masukkan opsi menu secara langsung lewat kode
                popupMenu.menu.add(0, 1, 0, "✏️  Edit Jadwal")
                popupMenu.menu.add(0, 2, 1, "🗑️  Hapus Jadwal")

                // 3. Tangkap aksi ketukan pada item menu dropdown
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> {
                            onEditClick(schedule) // Memicu callback edit ke Fragment
                            true
                        }
                        2 -> {
                            onDeleteClick(schedule) // Memicu callback delete ke Fragment
                            true
                        }
                        else -> false
                    }
                }

                // 4. Tampilkan menu melayang tepat di bawah ikon titik tiga
                popupMenu.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(scheduleList[position])
    }

    override fun getItemCount(): Int = scheduleList.size

    fun submitList(newList: List<Schedule>) {
        this.scheduleList = newList
        notifyDataSetChanged()
    }
}