package com.example.myapplication.data.sources.models


data class NotificationReplacement(

    val id:Int,

    val assignmentId:Int,

    val requesterName:String,

    val replacementName:String?,

    val approvedByName:String?,

    val reason:String,

    val status:String,

    val title:String,

    val location:String,

    val startTime:String,

    val endTime:String,

    val createdAt:String

)