package com.example.myapplication.ui.staff.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.sources.models.MySchedule
import com.example.myapplication.databinding.ItemMyScheduleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MyScheduleAdapter(

    private var myScheduleList: List<MySchedule> = emptyList(),

    private val onItemClick: (MySchedule) -> Unit

) : RecyclerView.Adapter<MyScheduleAdapter.MyScheduleViewHolder>() {

    inner class MyScheduleViewHolder(

        private val binding: ItemMyScheduleBinding

    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: MySchedule) {

            binding.tvTitle.text = schedule.title

            binding.tvDescription.text =
                schedule.description ?: "Tidak ada deskripsi"

            binding.tvLocation.text =
                "📍 ${schedule.location ?: "-"}"

            binding.tvRole.text =
                schedule.role_in_event ?: "-"

            binding.tvStatus.text =
                schedule.status

            val timeFormat =
                SimpleDateFormat("HH:mm", Locale.getDefault())

            timeFormat.timeZone =
                TimeZone.getTimeZone("Asia/Jakarta")

            val start =
                timeFormat.format(Date(schedule.start_time))

            val end =
                timeFormat.format(Date(schedule.end_time))

            binding.tvTime.text =
                "$start - $end"

            binding.root.setOnClickListener {

                onItemClick(schedule)

            }

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyScheduleViewHolder {

        val binding =
            ItemMyScheduleBinding.inflate(

                LayoutInflater.from(parent.context),

                parent,

                false

            )

        return MyScheduleViewHolder(binding)

    }

    override fun onBindViewHolder(
        holder: MyScheduleViewHolder,
        position: Int
    ) {

        holder.bind(myScheduleList[position])

    }

    override fun getItemCount(): Int {

        return myScheduleList.size

    }

    fun submitList(

        newList: List<MySchedule>

    ) {

        myScheduleList = newList

        notifyDataSetChanged()

    }

}