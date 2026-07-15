package com.example.myapplication.ui.admin.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.sources.models.ReplacementItem
import com.example.myapplication.databinding.ItemReplacementRequestBinding

class ReplacementAdapter(

    private val onClick:(ReplacementItem)->Unit,
    private val onCloseClick:(ReplacementItem)->Unit

):RecyclerView.Adapter<ReplacementAdapter.ViewHolder>(){

    private var items= mutableListOf<ReplacementItem>()

    fun submitList(list:List<ReplacementItem>){

        items.clear()

        items.addAll(list)

        notifyDataSetChanged()

    }

    inner class ViewHolder(

        private val binding:ItemReplacementRequestBinding

    ):RecyclerView.ViewHolder(binding.root){

        fun bind(item:ReplacementItem){

            binding.tvRequester.text=item.requesterName

            binding.tvReplacement.text="Pengganti : ${item.replacementName}"

            binding.tvTitle.text=item.title

            binding.tvLocation.text=item.location

            binding.tvTime.text="Waktu : ${item.startTime} - ${item.endTime}"

            binding.tvCreatedAt.text="Diajukan : ${formatDateTime(item.createdAt)}"

            binding.tvStatus.text=item.status.uppercase()

            if (item.status.lowercase() == "approved" || item.status.lowercase() == "rejected") {
                binding.btnClose.visibility = View.VISIBLE
            } else {
                binding.btnClose.visibility = View.GONE
            }

            binding.btnClose.setOnClickListener {
                onCloseClick(item)
            }

            when(item.status.lowercase()){

                "pending"->{

                    binding.tvStatus.setTextColor(
                        Color.parseColor("#F57C00")
                    )

                }

                "approved"->{

                    binding.tvStatus.setTextColor(
                        Color.parseColor("#2E7D32")
                    )

                }

                "rejected"->{

                    binding.tvStatus.setTextColor(
                        Color.parseColor("#C62828")
                    )

                }

            }

            binding.root.setOnClickListener{

                onClick(item)

            }

        }

    }

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):ViewHolder{

        val binding=ItemReplacementRequestBinding.inflate(

            LayoutInflater.from(parent.context),

            parent,

            false

        )

        return ViewHolder(binding)

    }

    override fun getItemCount()=items.size

    override fun onBindViewHolder(holder:ViewHolder,position:Int){

        holder.bind(items[position])

    }

    private fun formatDateTime(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "-"
        return try {
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val formatter = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getDefault()
            }
            val date = parser.parse(dateStr)
            if (date != null) formatter.format(date) else dateStr
        } catch (e: Exception) {
            try {
                val parser = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val formatter = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault())
                val date = parser.parse(dateStr)
                if (date != null) formatter.format(date) else dateStr
            } catch (e2: Exception) {
                dateStr ?: "-"
            }
        }
    }
}