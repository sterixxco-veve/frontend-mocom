package com.example.myapplication.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repositories.AssignmentRepository

class MyScheduleViewModelFactory(

    private val assignmentRepository: AssignmentRepository

) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                MyScheduleViewModel::class.java
            )
        ) {

            return MyScheduleViewModel(
                assignmentRepository
            ) as T

        }

        throw IllegalArgumentException(
            "Kelas ViewModel tidak dikenal"
        )

    }

}