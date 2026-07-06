package com.example.myapplication.ui.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.databinding.ItemBroadcastBinding
import java.util.concurrent.TimeUnit

class BroadcastAdapter : ListAdapter<Announcement, BroadcastAdapter.BroadcastViewHolder>(BroadcastDiffCallback()) {
    class BroadcastDiffCallback : DiffUtil.ItemCallback<Announcement>() {
        override fun areItemsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
            return oldItem == newItem
        }
    }

    inner class BroadcastViewHolder(private val binding: ItemBroadcastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(announcement: Announcement) {
            binding.tvItemTitle.text = announcement.title
            binding.tvItemDesc.text = announcement.message

            val timeAgo = getTimeAgo(announcement.created_at)
            binding.tvItemMeta.text = "${announcement.authorName} | $timeAgo"
        }

        private fun getTimeAgo(createdAt: Long): String {
            val now = System.currentTimeMillis()
            val diffMillis = now - createdAt

            val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
            val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

            return when {
                diffMinutes < 1 -> "Baru saja"
                diffMinutes < 60 -> "$diffMinutes menit lalu"
                diffHours < 24 -> "$diffHours jam lalu"
                diffDays < 7 -> "$diffDays hari lalu"
                else -> {
                    val diffWeeks = diffDays / 7
                    "$diffWeeks minggu lalu"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BroadcastViewHolder {
        val binding = ItemBroadcastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BroadcastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BroadcastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}