package com.example.myapplication.ui.admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.User
import java.util.Locale

class UserAdapter(
    private var userList: List<User>,
    // 💡 Kita sesuaikan callback agar mengirimkan User beserta ID Aksi Menu (1 = Edit, 2 = Hapus)
    private val onMenuActionClick: (User, actionId: Int) -> Unit
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

            // Tampilkan Nama Role Asli (Staff / Member) Berdasarkan role_id
            val roleName = when (user.role_id) {
                2 -> "Staff"
                3 -> "Member"
                else -> "User"
            }

            // Logika Warna Status Keaktifan (1 = Aktif, 0 = Nonaktif)
            when (user.is_active) {
                1 -> {
                    tvUserRole.text = "$roleName • Active ✔"
                    tvUserRole.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                }
                0 -> {
                    tvUserRole.text = "$roleName • Nonaktif ❌"
                    tvUserRole.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                }
                else -> {
                    tvUserRole.text = "$roleName • Unknown"
                    tvUserRole.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.darker_gray))
                }
            }

            // Pembuatan Inisial Avatar Otomatis (Bobby Danendra -> BD)
            val words = user.full_name.trim().split("\\s+".toRegex())
            val initials = if (words.size >= 2) {
                (words[0].take(1) + words[1].take(1)).uppercase(Locale.getDefault())
            } else {
                user.full_name.take(2).uppercase(Locale.getDefault())
            }
            tvAvatarInitials.text = initials
            btnUserOptions.setOnClickListener { view ->
                val popup = PopupMenu(itemView.context, view)
                popup.menu.add(0, 1, 0, "Edit Pengguna")
                popup.menu.add(0, 2, 1, "Hapus Pengguna")

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        1 -> {
                            onMenuActionClick(user, 1) // 💡 Mengirim angka 1 ke fragment
                            true
                        }
                        2 -> {
                            onMenuActionClick(user, 2) // 💡 Mengirim angka 2 ke fragment
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
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