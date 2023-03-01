package com.voiceapp.project

import com.voiceapp.data.FireStoreHelper
import com.voiceapp.data.model.Interview
import com.voiceapp.data.model.InterviewConsent
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val fireStoreHelper: FireStoreHelper
) {

    fun getInterviewConsentFlow(
        projectId: String,
        interviewId: String,
        consentStage: Int): Flow<Result<InterviewConsent>> = callbackFlow {
        val successListener = OnSuccessListener<List<Interview>> {
            launch {
                getInterviewFromProject(it, interviewId)?.let {
                    getConsentForStage(it, consentStage)?.let { consent ->
                        channel.send(Result.Success(consent))
                    }
                } ?: channel.send(Result.Error)
            }
        }

        val failureListener = OnFailureListener {
            launch {
                channel.send(Result.Error)
            }
        }

        fireStoreHelper.getInterviews(projectId, successListener, failureListener, false)

        awaitClose()
    }

    private fun getInterviewFromProject(interviews: List<Interview>, interviewId: String) =
        interviews.firstOrNull {
            interviewId == it.id
        }

    private fun getConsentForStage(interview: Interview, stage: Int): InterviewConsent? {
        return when (stage) {
            1 -> interview.consent_step_1
            2 -> interview.consent_step_2
            else -> null
        }
    }
}