package com.example.myapplication.ui.staff.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.sources.models.Announcement
import com.example.myapplication.databinding.ItemBroadcastBinding

class BroadcastStaffAdapter :
    ListAdapter<Announcement, BroadcastStaffAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        val binding: ItemBroadcastBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemBroadcastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = getItem(position)

        holder.binding.tvItemTitle.text = item.title
        holder.binding.tvItemDesc.text = item.message

        holder.binding.tvItemMeta.text =
            "${item.authorName} • ${item.created_at ?: ""}"

    }

    class DiffCallback : DiffUtil.ItemCallback<Announcement>() {

        override fun areItemsTheSame(
            oldItem: Announcement,
            newItem: Announcement
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Announcement,
            newItem: Announcement
        ) = oldItem == newItem
    }
}