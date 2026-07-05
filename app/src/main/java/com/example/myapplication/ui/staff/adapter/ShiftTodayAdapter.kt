package com.example.myapplication.ui.staff.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemShiftTodayBinding
import com.example.myapplication.data.sources.remote.json.AssignmentJson

class ShiftTodayAdapter(
    private val listShift: List<AssignmentJson>
) : RecyclerView.Adapter<ShiftTodayAdapter.ShiftViewHolder>() {

    inner class ShiftViewHolder(private val binding: ItemShiftTodayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AssignmentJson) {
            // 1. 🎯 UBAH KE item.title agar judulnya muncul (bukan job_desc yang rawan NULL)
            binding.tvShiftTitle.text = item.title

            // 2. Potong String Tanggal MySQL agar hanya menampilkan Jam & Menit saja (HH:mm)
            try {
                val startTimeClean = item.start_time.substringAfter(" ").substringBeforeLast(":")
                val endTimeClean = item.end_time.substringAfter(" ").substringBeforeLast(":")
                binding.tvShiftTime.text = "⏱️ $startTimeClean - $endTimeClean"
            } catch (e: Exception) {
                // Fallback jika format string berbeda dari dugaan
                binding.tvShiftTime.text = "⏱️ ${item.start_time} - ${item.end_time}"
            }

            // 3. Set Lokasi Penugasan Kantor
            binding.tvShiftLocation.text = "📍 ${item.location}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftViewHolder {
        val binding = ItemShiftTodayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShiftViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShiftViewHolder, position: Int) {
        holder.bind(listShift[position])
    }

    override fun getItemCount(): Int = listShift.size
}