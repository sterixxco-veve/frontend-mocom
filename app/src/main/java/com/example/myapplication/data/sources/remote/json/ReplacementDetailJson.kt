package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.ReplacementDetail

data class ReplacementDetailJson(

    val id: Int,
    val assignment_id: Int,
    val requested_by: Int,
    val requester_name: String,
    val requester_email: String,
    val replacement_user_id: Int,
    val replacement_name: String,
    val replacement_email: String,
    val reason: String,
    val status: String,
    val approved_by: Int?,
    val approved_by_name: String?,
    val schedule_id: Int,
    val title: String,
    val description: String,
    val location: String,
    val start_time: String,
    val end_time: String,
    val created_at: String

){

    fun toReplacementDetail(): ReplacementDetail{

        return ReplacementDetail(

            id = id,

            assignmentId = assignment_id,

            requestedBy = requested_by,

            requesterName = requester_name,

            requesterEmail = requester_email,

            replacementUserId = replacement_user_id,

            replacementName = replacement_name,

            replacementEmail = replacement_email,

            reason = reason,

            status = status,

            approvedBy = approved_by,

            approvedByName = approved_by_name,

            scheduleId = schedule_id,

            title = title,

            description = description,

            location = location,

            startTime = start_time,

            endTime = end_time,

            createdAt = created_at
        )
    }
}

