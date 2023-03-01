package com.voiceapp.analytics

interface AnalyticsManager {

    fun onInterviewStarted(event: InterviewStartedEvent)

    fun onInterviewCompleted(event: InterviewCompletedEvent)

    fun onInterviewTerminated(event: InterviewTerminatedEvent)

    fun onQuestionAnswerSubmitted(event: AnswerSubmitEvent)
}