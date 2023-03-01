package com.voiceapp.ui.interview.consent

import androidx.lifecycle.*
import com.voiceapp.livedata.DistinctLiveData
import com.voiceapp.project.ProjectRepository
import com.voiceapp.project.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ConsentWrittenFragmentViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
): ViewModel() {

    private val interviewId = DistinctLiveData<InterviewConsentId?>()
    val interviewIdLiveData: LiveData<InterviewConsentId?> get() = interviewId

    val interview = interviewId.switchMap {
        it?.let {
            projectRepository.getInterviewConsentFlow(it.projectId, it.interviewId, it.consentStage)
                .flowOn(Dispatchers.IO)
                .asLiveData()
        } ?: MutableLiveData(Result.Error)
    }

    fun setInterviewId(consentId: InterviewConsentId?) {
        interviewId.setValue(consentId)
    }
}