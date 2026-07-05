package com.example.myapplication.ui.staff

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityStaffBinding

class StaffActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffBinding

    private var currentUserId = -1
    private var currentCompanyId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = intent.getIntExtra("EXTRA_USER_ID", -1)
        currentCompanyId = intent.getIntExtra("EXTRA_COMPANY_ID", -1)

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.staff_nav_host_fragment
            ) as NavHostFragment

        val navController =
            navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // Inflate menu logout pada toolbar atas dan handle klik item untuk Staff
        binding.toolbar.inflateMenu(R.menu.staff_top_menu)
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