package com.example.myapplication.ui.admin.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.example.myapplication.ui.admin.adapters.UserAdapter

class AdminUserManagementFragment : Fragment() {

    private val viewModel: AdminViewModel by viewModels {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository)
    }

    private lateinit var userAdapter: UserAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var btnStaff: Button
    private lateinit var btnMember: Button

    private var currentSelectedRoleId: Int = 2 // Default: 2 (Staff / Asisten)
    private var rawUserList: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_user_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi View berdasarkan XML baru ala Monitoring Absensi
        btnStaff = view.findViewById(R.id.btn_filter_staff)
        btnMember = view.findViewById(R.id.btn_filter_member)
        swipeRefresh = view.findViewById(R.id.swipeRefreshUser)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvAdminUserManagement)

        val currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", 1)

        // 2. Setup RecyclerView & Adapter
        userAdapter = UserAdapter(
            userList = emptyList(),
            onOptionsClick = { userTerpilih, anchorView ->
                openUserOptionsBottomSheet(userTerpilih, anchorView)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = userAdapter

        // 3. Amati LiveData dari ViewModel
        viewModel.users.observe(viewLifecycleOwner) { userList ->
            rawUserList = userList
            filterAndDisplayData()

            // Hentikan loading spinner SwipeRefreshLayout jika sedang berputar
            if (swipeRefresh.isRefreshing) {
                swipeRefresh.isRefreshing = false
            }
        }

        // 4. Listener klik untuk Tombol Staff / Asisten (Role 2)
        btnStaff.setOnClickListener {
            currentSelectedRoleId = 2
            changeButtonState(activeButton = btnStaff, inactiveButton = btnMember)
            filterAndDisplayData()
        }

        // 5. Listener klik untuk Tombol Member / Mahasiswa (Role 3)
        btnMember.setOnClickListener {
            currentSelectedRoleId = 3
            changeButtonState(activeButton = btnMember, inactiveButton = btnStaff)
            filterAndDisplayData()
        }

        // 6. Listener Swipe Refresh (Tarik untuk menyegarkan data)
        swipeRefresh.setOnRefreshListener {
            viewModel.loadUserByCompanyId(currentCompanyId)
        }
    }

    /**
     * Fungsi untuk memfilter data berdasarkan Role ID yang dipilih
     * dan mengurutkan status Aktif (1) agar selalu berada di posisi paling atas.
     */
    private fun filterAndDisplayData() {
        val filteredList = rawUserList
            .filter { it.role_id == currentSelectedRoleId }
            .sortedByDescending { it.is_active } // 💡 1 (Aktif) otomatis naik ke atas 0 (Nonaktif)

        userAdapter.submitList(filteredList)
    }

    /**
     * Fungsi pembantu untuk mengubah warna background dan text button filter
     * secara dinamis agar seragam dengan gaya desain tombol absensi.
     */
    private fun changeButtonState(activeButton: Button, inactiveButton: Button) {
        // State Aktif (Warna Biru Premium)
        activeButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4361EE"))
        activeButton.setTextColor(Color.WHITE)

        // State Non-Aktif (Warna Abu-Abu Transparan)
        inactiveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EEF2F6"))
        inactiveButton.setTextColor(Color.parseColor("#8D99AE"))
    }

    override fun onResume() {
        super.onResume()
        // Ambil data terbaru dari server Cloud MySQL saat fragment kembali terbuka
        val currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", 1)
        viewModel.loadUserByCompanyId(currentCompanyId)
    }

    private fun openUserOptionsBottomSheet(user: User, anchorView: View) {
        android.widget.Toast.makeText(
            context,
            "Mengelola opsi untuk: ${user.full_name}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}