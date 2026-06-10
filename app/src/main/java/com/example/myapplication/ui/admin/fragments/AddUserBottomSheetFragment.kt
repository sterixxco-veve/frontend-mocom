package com.example.myapplication.ui.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.myapplication.App
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.databinding.FragmentAddUserBinding
import com.example.myapplication.ui.admin.AdminViewModel
import com.example.myapplication.ui.admin.AdminViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddUserBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: AdminViewModel by viewModels({ requireParentFragment() }) {
        val app = requireActivity().application as App
        AdminViewModelFactory(app.scheduleRepository, app.userRepository)
    }

    private var _binding: FragmentAddUserBinding? = null
    private val binding get() = _binding!!

    // 💡 Properti penampung data untuk mendeteksi Mode Edit
    private var editUser: User? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            editUser = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_USER, User::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable(ARG_USER)
            }
            isEditMode = editUser != null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentCompanyId = requireActivity().intent.getIntExtra("EXTRA_COMPANY_ID", 1)

        // 1. Setup Dropdown Pilihan Role
        val rolesArray = arrayOf("Staff", "Member")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, rolesArray)
        binding.actvRole.setAdapter(adapter)

        // 2. 💡 KONDISIONAL MODE: Jika Mode Edit, isi form dengan data lama (Pre-fill)
        if (isEditMode) {
            binding.tvHeaderTitle.text = "Ubah Data Pengguna"
            binding.btnSaveUser.text = "Simpan Perubahan"

            editUser?.let { user ->
                binding.etFullName.setText(user.full_name)
                binding.etUsername.setText(user.username)
                binding.etEmail.setText(user.email)
                binding.etPassword.setText(user.password) // Tampilkan password mentah sesuai request-mu kemarin
                binding.switchActiveStatus.isChecked = user.is_active == 1

                val roleText = if (user.role_id == 2) "Staff / Asisten Laboratorium" else "Member / Mahasiswa"
                binding.actvRole.setText(roleText, false)
            }
        } else {
            binding.tvHeaderTitle.text = "Tambah Pengguna Baru"
            binding.btnSaveUser.text = "Daftarkan Pengguna"
        }

        // 3. Event Tombol Simpan Klik (Berfungsi Ganda)
        binding.btnSaveUser.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val selectedRoleText = binding.actvRole.text.toString()

            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || selectedRoleText.isEmpty()) {
                Toast.makeText(context, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val roleId = if (selectedRoleText == "Staff / Asisten Laboratorium") 2 else 3
            val isActiveStatus = if (binding.switchActiveStatus.isChecked) 1 else 0

            if (isEditMode) {
                // =========================================================================
                // 📝 JALUR AKSES A: JALANKAN PROSES UPDATE USER
                // =========================================================================
                val updatedUser = editUser!!.copy(
                    role_id = roleId,
                    company_id = currentCompanyId,
                    full_name = fullName,
                    username = username,
                    email = email,
                    password = password,
                    is_active = isActiveStatus
                )

                viewModel.updateUser(updatedUser) { success ->
                    if (success) {
                        Toast.makeText(context, "Data pengguna berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        viewModel.loadUserByCompanyId(currentCompanyId) // Auto-refresh list
                        dismiss()
                    } else {
                        Toast.makeText(context, "Gagal memperbarui data user ke server Cloud", Toast.LENGTH_LONG).show()
                    }
                }

            } else {
                // =========================================================================
                // ➕ JALUR AKSES B: JALANKAN PROSES TAMBAH USER BARU (INSERT)
                // =========================================================================
                val newUser = User(
                    id = 0,
                    company_id = currentCompanyId,
                    role_id = roleId,
                    full_name = fullName,
                    username = username,
                    email = email,
                    password = password,
                    is_active = isActiveStatus
                )

                viewModel.addUser(newUser, currentCompanyId) { success ->
                    if (success) {
                        Toast.makeText(context, "User baru sukses didaftarkan!", Toast.LENGTH_SHORT).show()
                        viewModel.loadUserByCompanyId(currentCompanyId) // Auto-refresh list
                        dismiss()
                    } else {
                        Toast.makeText(context, "Gagal menyimpan user baru ke server Cloud", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        private const val ARG_USER = "arg_user"

        // 💡 Fungsi pemicu khusus untuk Mode Edit (Membawa Objek User)
        fun newInstance(user: User): AddUserBottomSheetFragment {
            return AddUserBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_USER, user)
                }
            }
        }
    }
}