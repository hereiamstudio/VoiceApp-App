package com.voiceapp.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsManager @Inject constructor(
    private val analytics: FirebaseAnalytics) : AnalyticsManager {

    companion object {

        // Event names
        private const val EVENT_INTERVIEW_STARTED = "interview_started"
        private const val EVENT_INTERVIEW_COMPLETED = "interview_completed"
        private const val EVENT_INTERVIEW_TERMINATED = "interview_terminated"
        private const val EVENT_QUESTION_ANSWER_SUBMITTED = "question_answer_submitted"

        // Additional data properties
        private const val INTERVIEW_ID = "interview_id"
        private const val QUESTION_ID = "question_id"
        private const val INTERVIEW_TERMINATION_POINT = "interview_termination_point"
        private const val VOICE_TO_TEXT_USED = "voice_to_text_used"
        private const val VOICE_TO_TEXT_RESULT_EDITED = "voice_to_text_result_edited"

        // Termination point enumeration as strings
        private const val TERMINATION_POINT_DURING_INTERVIEW = "during_interview"
        private const val TERMINATION_POINT_END_INTERVIEW = "end_interview"
        private const val TERMINATION_POINT_CONSENT_NOT_GRANTED_START_RESPONDENT =
            "consent_not_granted_start_respondent"
        private const val TERMINATION_POINT_CONSENT_NOT_GRANTED_ADDITIONAL_CONSENT =
            "consent_not_granted_start_additional"
        private const val TERMINATION_POINT_CONSENT_NOT_GRANTED_END =
            "consent_not_granted_end"
    }

    override fun onInterviewStarted(event: InterviewStartedEvent) {
        Bundle().apply {
            putString(INTERVIEW_ID, event.interviewId)
        }.let {
            analytics.logEvent(EVENT_INTERVIEW_STARTED, it)
        }
    }

    override fun onInterviewCompleted(event: InterviewCompletedEvent) {
        Bundle().apply {
            putString(INTERVIEW_ID, event.interviewId)
        }.let {
            analytics.logEvent(EVENT_INTERVIEW_COMPLETED, it)
        }
    }

    override fun onInterviewTerminated(event: InterviewTerminatedEvent) {
        Bundle().apply {
            putString(INTERVIEW_ID, event.interviewId)
            putString(
                INTERVIEW_TERMINATION_POINT,
                mapTerminationPointToString(event.terminationPoint))
        }.let {
            analytics.logEvent(EVENT_INTERVIEW_TERMINATED, it)
        }
    }

    override fun onQuestionAnswerSubmitted(event: AnswerSubmitEvent) {
        Bundle().apply {
            putString(INTERVIEW_ID, event.interviewId)
            putString(QUESTION_ID, event.questionId)
            putBoolean(VOICE_TO_TEXT_USED, event.voiceToTextUsed)
            putBoolean(VOICE_TO_TEXT_RESULT_EDITED, event.voiceToTextResultEdited)
        }.let {
            analytics.logEvent(EVENT_QUESTION_ANSWER_SUBMITTED, it)
        }
    }

    private fun mapTerminationPointToString(terminationPoint: InterviewTerminationPoint): String {
        return when (terminationPoint) {
            InterviewTerminationPoint.DURING_INTERVIEW -> TERMINATION_POINT_DURING_INTERVIEW
            InterviewTerminationPoint.END_INTERVIEW -> TERMINATION_POINT_END_INTERVIEW
            InterviewTerminationPoint.CONSENT_NOT_GRANTED_START_RESPONDENT ->
                TERMINATION_POINT_CONSENT_NOT_GRANTED_START_RESPONDENT
            InterviewTerminationPoint.CONSENT_NOT_GRANTED_START_ADDITIONAL_CONSENT ->
                TERMINATION_POINT_CONSENT_NOT_GRANTED_ADDITIONAL_CONSENT
            InterviewTerminationPoint.CONSENT_NOT_GRANTED_END ->
                TERMINATION_POINT_CONSENT_NOT_GRANTED_END
        }
    }
}