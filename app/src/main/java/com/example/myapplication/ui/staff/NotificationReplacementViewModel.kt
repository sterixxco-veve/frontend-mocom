package com.example.myapplication.ui.staff

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.ReplacementRepository
import com.example.myapplication.data.sources.models.NotificationReplacement
import kotlinx.coroutines.launch

class NotificationReplacementViewModel( private val replacementRepository: ReplacementRepository): ViewModel() {
    private val _replacementNotifications =
        MutableLiveData<List<NotificationReplacement>>()

    val replacementNotifications :
            LiveData<List<NotificationReplacement>>
            = _replacementNotifications

    fun loadReplacementNotifications(

        userId:Int

    ){

        viewModelScope.launch {

            try{

                _replacementNotifications.value =

                    replacementRepository
                        .getReplacementNotifications(userId)

            }catch(e:Exception){

                e.printStackTrace()

                _replacementNotifications.value =
                    emptyList()

            }

        }

    }
}