package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.ReplacementItem
import com.google.gson.annotations.SerializedName

data class ReplacementListJson(

    val id: Int,

    val assignment_id: Int,

    val requester_name: String,

    val replacement_name: String?,

    val reason: String,

    val status: String,

    val title: String,

    val location: String,

    val start_time: String,

    val end_time: String,

    val created_at: String

){
    fun toReplacementItem(): ReplacementItem {

        return ReplacementItem(

            id = id,

            assignmentId = assignment_id,

            requesterName = requester_name,

            replacementName = replacement_name ?: "-",

            reason = reason,

            status = status,

            startTime = start_time,

            endTime = end_time,

            title = title,

            location = location,

            createdAt = created_at

        )
    }
}