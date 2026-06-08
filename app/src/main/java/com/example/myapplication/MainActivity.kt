package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.sources.models.User
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.admin.AdminActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // PERBAIKAN 1: Deklarasikan properti ApiService
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // REVISI SINKRONISASI: Panggil properti apiService dari RetrofitClient
        apiService = RetrofitClient.apiService

        // PERBAIKAN 3: Amankan padding asli XML agar tetap aesthetic di emulator
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val originalPaddingLeft = v.paddingLeft
            val originalPaddingRight = v.paddingRight
            val originalPaddingBottom = v.paddingBottom

            v.setPadding(
                originalPaddingLeft + systemBars.left,
                systemBars.top, // Biar header logo tidak menabrak status bar atas
                originalPaddingRight + systemBars.right,
                originalPaddingBottom + systemBars.bottom
            )
            insets
        }

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val emailOrUsername = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        var isValid = true
        if (emailOrUsername.isEmpty()) {
            binding.tilEmail.error = "Email atau Username wajib diisi"
            isValid = false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Kata sandi wajib diisi"
            isValid = false
        }

        if (isValid) {
            val userRequest = User(
                email = emailOrUsername,
                password = password
            )
            performNetworkLogin(userRequest)
        }
    }

    private fun performNetworkLogin(userRequest: User) {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Memuat..."

        lifecycleScope.launch {
            try {
                val response = apiService.login(userRequest)

                android.util.Log.d("LOGIN_DEBUG", "Code: ${response.code()}")
                android.util.Log.d("LOGIN_DEBUG", "Message: ${response.message()}")
                android.util.Log.d("LOGIN_DEBUG", "Body: ${response.body()}")
                android.util.Log.d("LOGIN_DEBUG", "ErrorBody: ${response.errorBody()?.string()}")

                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Masuk"

                if (response.isSuccessful && response.body() != null) {
                    val loggedInUser = response.body()!!

                    val staffCompanyId = loggedInUser.company_id
                    val staffName = loggedInUser.fullname ?: loggedInUser.username ?: "Staff"

                    Toast.makeText(
                        this@MainActivity,
                        "Selamat datang, $staffName!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Skenario Pindah Halaman:
                     val intent = Intent(this@MainActivity, AdminActivity::class.java).apply {
                         putExtra("EXTRA_COMPANY_ID", staffCompanyId)
                     }
                     startActivity(intent)
                     finish()

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Email atau kata sandi salah.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Masuk"

                // Tambahkan log ini untuk melihat pesan error asli di tab Logcat Android Studio
                android.util.Log.e("LOGIN_DEBUG", "Penyebab Gagal:", e)

                Toast.makeText(
                    this@MainActivity,
                    "Kesalahan: ${e.localizedMessage}", // Menampilkan pesan error asli di layar HP
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}