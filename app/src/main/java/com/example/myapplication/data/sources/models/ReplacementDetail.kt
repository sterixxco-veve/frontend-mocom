package com.example.myapplication.data.sources.models

data class ReplacementDetail(

    val id: Int,

    val assignmentId: Int,

    val requestedBy: Int,

    val requesterName: String,

    val requesterEmail: String,

    val replacementUserId: Int,

    val replacementName: String,

    val replacementEmail: String,

    val reason: String,

    val status: String,

    val approvedBy: Int?,

    val approvedByName: String?,

    val scheduleId: Int,

    val title: String,

    val description: String,

    val location: String,

    val startTime: String,

    val endTime: String,

    val createdAt: String
)