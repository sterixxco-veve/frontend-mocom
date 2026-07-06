package com.example.myapplication.ui.staff.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.App
import com.example.myapplication.data.repositories.DefaultUserRepository
import com.example.myapplication.databinding.FragmentChangePasswordBinding
import com.example.myapplication.ui.staff.ChangePasswordViewModel
import com.example.myapplication.ui.staff.ChangePasswordViewModelFactory
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChangePasswordViewModel

    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository =
            (requireActivity().application as App).userRepository

        viewModel = ViewModelProvider(
            this,
            ChangePasswordViewModelFactory(repository)
        )[ChangePasswordViewModel::class.java]

        val pref = requireContext().getSharedPreferences(
            "EduStaffSession",
            android.content.Context.MODE_PRIVATE
        )

        userId = pref.getInt("LOGIN_USER_ID", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChangePasswordBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSavePassword.setOnClickListener {

            val password = binding.etNewPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()

            binding.tilNewPassword.error = null
            binding.tilConfirmPassword.error = null

            when {

                password.isEmpty() -> {
                    binding.tilNewPassword.error = "Password baru wajib diisi"
                }

                confirm.isEmpty() -> {
                    binding.tilConfirmPassword.error = "Konfirmasi password wajib diisi"
                }

                password.length < 6 -> {
                    binding.tilNewPassword.error = "Password minimal 6 karakter"
                }

                password != confirm -> {
                    binding.tilConfirmPassword.error = "Password tidak sama"
                }

                else -> {

                    lifecycleScope.launch {

                        try {

                            viewModel.changePassword(
                                userId,
                                password
                            )

                            Toast.makeText(
                                requireContext(),
                                "Password berhasil diubah",
                                Toast.LENGTH_SHORT
                            ).show()

                            findNavController().popBackStack()

                        } catch (e: Exception) {

                            Toast.makeText(
                                requireContext(),
                                "Gagal mengubah password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                }

            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}