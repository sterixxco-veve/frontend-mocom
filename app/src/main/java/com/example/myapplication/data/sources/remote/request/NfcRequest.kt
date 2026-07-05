package com.example.myapplication.data.sources.remote.request

import com.google.gson.annotations.SerializedName

data class NfcRequest(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("nfc_uid")
    val nfcUid: String
)