package com.example.myapplication.data.sources.remote.request

import com.google.gson.annotations.SerializedName

data class NfcCheckInRequest(
    @SerializedName("nfc_uid")
    val nfcUid: String
)