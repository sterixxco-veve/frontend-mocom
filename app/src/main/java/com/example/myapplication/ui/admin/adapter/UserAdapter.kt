package com.example.myapplication.ui.admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.User
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class UserAdapter(
    private var userList: List<User>,
    private val onOptionsClick: (User, View) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatarInitials: TextView = view.findViewById(R.id.tvAvatarInitials)
        val tvUserName: TextView = view.findViewById(R.id.tvUsername)
        val tvUserEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvUserRole: TextView = view.findViewById(R.id.tvUserRole)
        val btnUserOptions: ImageButton = view.findViewById(R.id.btnUserOptions)

        fun bind(user: User) {
            tvUserName.text = user.full_name
            tvUserEmail.text = user.email ?: "- Tidak ada Email -"

            // =========================================================================
            // 💡 LOGIKA BARU STATUS IS_ACTIVE (1 = Active, 2 = Nonaktif)
            // =========================================================================
            when (user.is_active) {
                1 -> {
                    // Status Aktif (Hijau)
                    tvUserRole.text = "Active ✔"
                    tvUserRole.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))

                }
                2 -> {
                    // Status Nonaktif (Merah)
                    tvUserRole.text = "Nonaktif ❌"
                    tvUserRole.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                }
                else -> {
                    // Fallback jika ada angka lain di luar skema (Abu-abu)
                    tvUserRole.text = "Unknown"
                    tvUserRole.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.darker_gray))
                }
            }

            // Membuat Inisial Avatar Otomatis (Bobby Pratama -> BP)
            val words = user.full_name.trim().split("\\s+".toRegex())
            val initials = if (words.size >= 2) {
                (words[0].take(1) + words[1].take(1)).uppercase(Locale.getDefault())
            } else {
                user.full_name.take(2).uppercase(Locale.getDefault())
            }
            tvAvatarInitials.text = initials

            btnUserOptions.setOnClickListener { view ->
                onOptionsClick(user, view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    fun submitList(newList: List<User>) {
        this.userList = newList
        notifyDataSetChanged()
    }
}