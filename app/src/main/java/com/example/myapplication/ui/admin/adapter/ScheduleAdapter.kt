package com.example.myapplication.ui.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.sources.models.Schedule
import com.example.myapplication.databinding.ItemScheduleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleAdapter(
    private var scheduleList: List<Schedule> = emptyList(),
//    private val onItemClick: (Schedule) -> Unit // Lambda fungsi untuk mendeteksi klik pada item
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {
    inner class ScheduleViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: Schedule) {
            binding.tvTitle.text = schedule.title
            binding.tvDescription.text = schedule.description ?: "Tidak ada deskripsi"
            binding.tvLocation.text = "📍 ${schedule.location ?: "Online / Tidak Diketahui"}"

            // Konversi dari format Timestamp Long ke format Jam (HH:mm)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startTimeStr = timeFormat.format(Date(schedule.start_time))
            val endTimeStr = timeFormat.format(Date(schedule.end_time))
            binding.tvTime.text = "$startTimeStr - $endTimeStr"

//            binding.root.setOnClickListener {
//                onItemClick(schedule)
//            }
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

    // Fungsi untuk memperbarui data list dari Fragment / Activity secara dinamis
    fun submitList(newList: List<Schedule>) {
        this.scheduleList = newList
        notifyDataSetChanged() // Memaksa RecyclerView menggambar ulang data terbaru
    }
}