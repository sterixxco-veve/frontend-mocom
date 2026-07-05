package com.example.myapplication.data.sources.remote.json

import com.google.gson.annotations.SerializedName

data class ReplacementRequest(
    @SerializedName("assignment_id") val assignmentId: Int,
    @SerializedName("requested_by") val requestedBy: Int,
    @SerializedName("replacement_user_id") val replacementUserId: Int,
    @SerializedName("reason") val reason: String
)

data class GeneralResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)