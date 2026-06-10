package com.example.myapplication.ui.admin.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.databinding.FragmentAdminUserManagementBinding // 💡 Import kelas binding hasil generate XML-mu
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.example.myapplication.ui.admin.adapters.UserAdapter

class AdminUserManagementFragment : Fragment() {

    private val viewModel: AdminViewModel by viewModels {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository)
    }

    private lateinit var userAdapter: UserAdapter

    // 💡 Variabel Binding dengan penanganan null agar terhindar dari Memory Leak
    private var _binding: FragmentAdminUserManagementBinding? = null
    private val binding get() = _binding!!

    private var currentSelectedRoleId: Int = 2 // Default: 2 (Staff / Asisten)
    private var rawUserList: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 💡 INSTANSIASI VIEW BINDING
        _binding = FragmentAdminUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", 1)

        // 1. Setup RecyclerView & Adapter dengan Lambda Callback Aksi Menu
        userAdapter = UserAdapter(emptyList()) { selectedUser, actionId ->
            when (actionId) {
                1 -> {
                    // 📝 JALUR EDIT: Tampilkan BottomSheet Edit / Detail User
                    openEditUserBottomSheet(selectedUser)
                }
                2 -> {
                    // 🗑️ JALUR HAPUS: Jalankan fungsi konfirmasi hapus data
                    showDeleteConfirmationDialog(selectedUser)
                }
            }
        }

        // 💡 AKSES VIEW VIA BINDING (Sangat bersih tanpa findViewById)
        binding.rvAdminUserManagement.layoutManager = LinearLayoutManager(context)
        binding.rvAdminUserManagement.adapter = userAdapter

        // 2. Amati LiveData dari ViewModel
        viewModel.users.observe(viewLifecycleOwner) { userList ->
            rawUserList = userList
            filterAndDisplayData()

            // 💡 Matikan loading spinner via binding jika sedang berputar
            if (binding.swipeRefreshUser.isRefreshing) {
                binding.swipeRefreshUser.isRefreshing = false
            }
        }

        // 3. Listener klik untuk Tombol Staff / Asisten (Role 2) via Binding
        binding.btnFilterStaff.setOnClickListener {
            currentSelectedRoleId = 2
            changeButtonState(activeButton = binding.btnFilterStaff, inactiveButton = binding.btnFilterMember)
            filterAndDisplayData()
        }

        // 4. Listener klik untuk Tombol Member / Mahasiswa (Role 3) via Binding
        binding.btnFilterMember.setOnClickListener {
            currentSelectedRoleId = 3
            changeButtonState(activeButton = binding.btnFilterMember, inactiveButton = binding.btnFilterStaff)
            filterAndDisplayData()
        }

        // 5. Listener Swipe Refresh via Binding
        binding.swipeRefreshUser.setOnRefreshListener {
            viewModel.loadUserByCompanyId(currentCompanyId)
        }

        // 6. Listener FAB Tambah User via Binding
        binding.fabAddUser.setOnClickListener {
            val addUserModal = AddUserBottomSheetFragment()
            addUserModal.show(childFragmentManager, "ADD_USER_BOTTOM_SHEET")
        }
    }

    private fun filterAndDisplayData() {
        val filteredList = rawUserList
            .filter { it.role_id == currentSelectedRoleId }
            .sortedByDescending { it.is_active } // User Aktif otomatis naik ke atas

        userAdapter.submitList(filteredList)
    }

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
        val currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", 1)
        viewModel.loadUserByCompanyId(currentCompanyId)
    }

    private fun openEditUserBottomSheet(user: User) {
        val editUserModal = AddUserBottomSheetFragment.newInstance(user)
        editUserModal.show(childFragmentManager, "EDIT_USER_BOTTOM_SHEET")
    }

    private fun showDeleteConfirmationDialog(user: User) {
        val currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", 1)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pengguna")
            .setMessage("Apakah Anda yakin ingin menghapus ${user.full_name} dari sistem EduStaff Pro?")
            .setPositiveButton("Hapus") { dialog, _ ->

                // 1. Tembak fungsi delete ke ViewModel
                viewModel.deleteUser(user.id) { success ->
                    if (success) {
                        Toast.makeText(context, "${user.full_name} berhasil dihapus!", Toast.LENGTH_SHORT).show()

                        // 2. Refresh ulang list user di layar agar baris yang dihapus langsung hilang
                        viewModel.loadUserByCompanyId(currentCompanyId)
                    } else {
                        Toast.makeText(context, "Gagal menghapus ${user.full_name} dari server", Toast.LENGTH_LONG).show()
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 💡 WAJIB: Bersihkan binding ketika view hancur untuk mencegah kebocoran memori (leak)
        _binding = null
    }
}