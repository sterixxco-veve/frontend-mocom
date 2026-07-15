package com.example.myapplication.ui.staff

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.AssignmentRepository
import com.example.myapplication.data.sources.models.MySchedule
import kotlinx.coroutines.launch

class MyScheduleViewModel(

    private val assignmentRepository: AssignmentRepository

) : ViewModel() {

    private val _myScheduleList = ArrayList<MySchedule>()

    private val _mySchedules = MutableLiveData(_myScheduleList.toList())

    val mySchedules: LiveData<List<MySchedule>>
        get() = _mySchedules

    private val _confirmStatusSuccess = MutableLiveData<Boolean>()
    val confirmStatusSuccess: LiveData<Boolean> get() = _confirmStatusSuccess

    fun confirmAssignmentStatus(assignmentId: Int, status: String, userId: Int) {
        viewModelScope.launch {
            val success = assignmentRepository.confirmAssignmentStatus(assignmentId, status)
            _confirmStatusSuccess.value = success
            if (success) {
                loadMySchedule(userId)
            }
        }
    }

    fun loadMySchedule(userId: Int) {

        Log.d("TRACK_MY_SCHEDULE", "========================================")
        Log.d("TRACK_MY_SCHEDULE", "Mengambil jadwal milik User ID : $userId")

        viewModelScope.launch {

            try {

                val result =
                    assignmentRepository.getMySchedule(userId)

                Log.d(
                    "TRACK_MY_SCHEDULE",
                    "Jumlah data : ${result.size}"
                )

                if (result.isNotEmpty()) {

                    result.forEachIndexed { index, item ->

                        Log.d(
                            "TRACK_MY_SCHEDULE",
                            "[$index] ${item.title} | ${item.location}"
                        )

                    }

                } else {

                    Log.w(
                        "TRACK_MY_SCHEDULE",
                        "Tidak ada jadwal."
                    )

                }

                _mySchedules.postValue(result)

            } catch (e: Exception) {

                Log.e(
                    "TRACK_MY_SCHEDULE",
                    "Error : ${e.message}"
                )

                e.printStackTrace()

                _mySchedules.postValue(emptyList())

            }

            Log.d("TRACK_MY_SCHEDULE", "========================================")

        }

    }

}