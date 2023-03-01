package com.voiceapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.voiceapp.data.FireStoreHelper
import com.voiceapp.data.model.Interview
import com.voiceapp.data.model.simple.SimpleTrackingHelper
import com.voiceapp.di.ForMainThread
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.concurrent.thread

class InterviewViewModel @Inject constructor(
    private val fireStoreHelper: FireStoreHelper,
    @ForMainThread private val mainExecutor: Executor): ViewModel() {

    private var interviews: MutableLiveData<List<Interview>> = MutableLiveData()

    fun getInterviews(projectId: String): MutableLiveData<List<Interview>> {
        fireStoreHelper.getInterviews(projectId,
            OnSuccessListener { interviews ->
                this.interviews.value = interviews
            },
            OnFailureListener { exception ->
                Timber.e(exception)
            }, live = false
        )
        return this.interviews
    }

    fun getInterviewsWithQuestions(projectId: String): MutableLiveData<List<Interview>> {
        fireStoreHelper.getInterviews(projectId,
            OnSuccessListener { interviews ->
                thread {
                    for (interview in interviews) {

                        interview.seen = SimpleTrackingHelper.getSeen(interview.id!!)
                        interview.responses_count = SimpleTrackingHelper.getResponseCount(interview.id!!)

                        runBlocking {
                            interview.questions = fireStoreHelper.getQuestionsSync(projectId, interview.id!!)
                        }
                    }

                    mainExecutor.execute {
                        this.interviews.value = interviews
                    }
                }
            },
            OnFailureListener { exception ->
                Timber.e(exception)
            }, live = false
        )
        return this.interviews
    }


}