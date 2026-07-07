package com.example.myapplication.data.sources.models

data class ReplacementItem(

    val id: Int,

    val assignmentId: Int,

    val requesterName: String,

    val replacementName: String,

    val title: String,

    val location: String,

    val reason: String,

    val status: String,

    val createdAt: String
)