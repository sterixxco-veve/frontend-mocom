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

    }

}