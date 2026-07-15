package com.example.myapplication.ui.staff.adapter

import android.graphics.Color
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.sources.models.NotificationReplacement
import com.example.myapplication.databinding.ItemNotificationReplacementBinding

class NotificationReplacementAdapter :
    RecyclerView.Adapter<NotificationReplacementAdapter.ViewHolder>() {

    private val items = mutableListOf<NotificationReplacement>()
    private var onCloseClick: ((NotificationReplacement) -> Unit)? = null

    fun setOnCloseClickListener(listener: (NotificationReplacement) -> Unit) {
        this.onCloseClick = listener
    }

    fun submitList(list: List<NotificationReplacement>) {

        items.clear()
        items.addAll(list)
        notifyDataSetChanged()

    }

    inner class ViewHolder(

        private val binding: ItemNotificationReplacementBinding

    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationReplacement) {

            binding.tvTitle.text = item.title

            binding.tvLocation.text = item.location

            binding.tvTime.text =
                "${item.startTime} - ${item.endTime}"

            binding.btnClose.setOnClickListener {
                onCloseClick?.invoke(item)
            }

            when(item.status.lowercase()){

                "pending"->{

                    binding.tvStatus.text =
                        "⏳ Menunggu Persetujuan"

                    binding.tvStatus.setTextColor(
                        Color.parseColor("#F57C00")
                    )

                    binding.tvMessage.text =
                        "Permohonanmu sedang diproses Admin."
                    
                    binding.btnClose.visibility = View.GONE

                }

                "approved"->{

                    binding.tvStatus.text =
                        "✅ Permohonan Disetujui"

                    binding.tvStatus.setTextColor(
                        Color.parseColor("#2E7D32")
                    )

                    binding.tvMessage.text =
                        "Admin menyetujui permohonanmu.\nPengganti : ${item.replacementName}"
                    
                    binding.btnClose.visibility = View.VISIBLE

                }

                "rejected"->{

                    binding.tvStatus.text =
                        "❌ Permohonan Ditolak"

                    binding.tvStatus.setTextColor(
                        Color.parseColor("#C62828")
                    )

                    binding.tvMessage.text =
                        "Admin menolak permohonanmu."
                    
                    binding.btnClose.visibility = View.VISIBLE

                }

            }

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemNotificationReplacementBinding.inflate(

                LayoutInflater.from(parent.context),

                parent,

                false

            )

        return ViewHolder(binding)

    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.bind(items[position])

    }

}