package com.voiceapp.analytics

data class InterviewTerminatedEvent(
    val interviewId: String,
    val terminationPoint: InterviewTerminationPoint
)