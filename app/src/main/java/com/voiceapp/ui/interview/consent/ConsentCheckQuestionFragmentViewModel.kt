package com.voiceapp.ui.interview.consent

import androidx.lifecycle.*
import com.voiceapp.data.model.InterviewConsent
import com.voiceapp.livedata.DistinctLiveData
import com.voiceapp.livedata.SingleLiveEvent
import com.voiceapp.project.ProjectRepository
import com.voiceapp.project.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ConsentCheckQuestionFragmentViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val interviewId = DistinctLiveData<InterviewConsentId?>()

    private val interviewConsent = interviewId.switchMap {
        it?.let {
            projectRepository.getInterviewConsentFlow(it.projectId, it.interviewId, it.consentStage)
                .flowOn(Dispatchers.IO)
                .asLiveData()
        } ?: MutableLiveData(Result.Error)
    }

    val questionsLiveData = interviewConsent.map {
        if (it is Result.Success) {
            mapToConsentQuestion(it.success)
        } else {
            null
        }
    }

    val stageLiveData = interviewId.map {
        when (it?.consentStage) {
            1 -> 2
            2 -> 4
            else -> -1
        }
    }

    private val back = SingleLiveEvent<ConsentBackParams>()
    val backLiveData: LiveData<ConsentBackParams> get() = back

    private val proceed = SingleLiveEvent<Int>()
    val proceedLiveData: LiveData<Int> get() = proceed

    private val failed = SingleLiveEvent<Nothing>()
    val failedLiveData: LiveData<Nothing> get() = failed

    private val unansweredError = SingleLiveEvent<Nothing>()
    val unansweredErrorLiveData: LiveData<Nothing> get() = unansweredError

    private val incorrectError = SingleLiveEvent<Nothing>()
    val incorrectErrorLiveData: LiveData<Nothing> get() = incorrectError

    private val failureCount = MutableLiveData(0)
    val failureCountLiveData: LiveData<Int> get() = failureCount

    private val uncheckAll = SingleLiveEvent<Nothing>()
    val uncheckAllLiveData: LiveData<Nothing> get() = uncheckAll

    private var currentAnswer: String? = null

    fun setInterviewId(consentId: InterviewConsentId?) {
        interviewId.setValue(consentId)
    }

    fun setFailureCount(count: Int) {
        failureCount.value = count
    }

    fun setCurrentAnswer(answer: String) {
        currentAnswer = answer
    }

    fun onNextClicked() {
        when (currentAnswer) {
            null -> {
                unansweredError.call()
            }
            getCorrectAnswer() -> {
                interviewId.value?.let {
                    proceed.value = it.consentStage
                }
            }
            else -> {
                incorrectError.call()
                val count = (failureCount.value ?: 0) + 1
                failureCount.value = count

                if (count >= 2) {
                    failed.call()
                }

                currentAnswer = null
                uncheckAll.call()
            }
        }
    }

    fun onBackClicked() {
        interviewId.value?.let {
            back.value = ConsentBackParams(it.consentStage, failureCount.value ?: 0)
        }
    }

    private fun mapToConsentQuestion(interviewConsent: InterviewConsent): ConsentQuestion?  {
        val questionText = interviewConsent.confirmation_question
        val answers = interviewConsent.confirmation_options?.mapNotNull {
            it.label
        }?.shuffled()

        return if (questionText?.isNotEmpty() == true &&
                answers?.isNotEmpty() == true) {
            ConsentQuestion(questionText, answers)
        } else {
            null
        }
    }

    private fun getCorrectAnswer(): String? {
        val result = interviewConsent.value

        return if (result is Result.Success) {
            result.success.confirmation_options?.firstOrNull {
                it.is_correct == true
            }?.label
        } else {
            null
        }
    }
}