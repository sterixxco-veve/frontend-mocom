package com.example.myapplication.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private var currentUserId: Int = -1
    private var currentCompanyId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentUserId = intent.getIntExtra("EXTRA_USER_ID", -1)
        currentCompanyId = intent.getIntExtra("EXTRA_COMPANY_ID", -1)

// Hubungkan Bottom Navigation dengan Jetpack Navigation Controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        // Inflate menu logout pada toolbar atas dan handle klik item
        binding.toolbar.inflateMenu(R.menu.admin_top_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                // Hapus data sesi SharedPreferences
                val sharedPref = getSharedPreferences("EduStaffSession", android.content.Context.MODE_PRIVATE)
                sharedPref.edit().clear().apply()

                // Redirect ke MainActivity (Halaman Login) dan bersihkan tumpukan aktivitas
                val intent = android.content.Intent(this, com.example.myapplication.MainActivity::class.java).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                true
            } else {
                false
            }
        }
    }
}