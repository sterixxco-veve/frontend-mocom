package com.example.myapplication.data.sources.remote.json

import com.example.myapplication.data.sources.models.NotificationReplacement

data class NotificationReplacementJson(

    val id:Int,

    val assignment_id:Int,

    val requester_name:String,

    val replacement_name:String?,

    val approved_by_name:String?,

    val reason:String,

    val status:String,

    val title:String,

    val location:String,

    val start_time:String,

    val end_time:String,

    val created_at:String

){

    fun toNotificationReplacement():NotificationReplacement{

        return NotificationReplacement(

            id=id,

            assignmentId=assignment_id,

            requesterName=requester_name,

            replacementName=replacement_name,

            approvedByName=approved_by_name,

            reason=reason,

            status=status,

            title=title,

            location=location,

            startTime=start_time,

            endTime=end_time,

            createdAt=created_at

        )

    }

}