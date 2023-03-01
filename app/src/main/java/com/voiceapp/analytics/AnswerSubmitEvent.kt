package com.voiceapp.analytics

data class AnswerSubmitEvent(
    val interviewId: String,
    val questionId: String,
    val voiceToTextUsed: Boolean,
    val voiceToTextResultEdited: Boolean)