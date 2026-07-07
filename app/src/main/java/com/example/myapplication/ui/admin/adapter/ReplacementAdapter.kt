package com.example.myapplication.ui.admin.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.sources.models.ReplacementItem
import com.example.myapplication.databinding.ItemReplacementRequestBinding

class ReplacementAdapter(

    private val onClick:(ReplacementItem)->Unit

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

            binding.tvStatus.text=item.status.uppercase()

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

}