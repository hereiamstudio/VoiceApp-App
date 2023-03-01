package com.voiceapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.voiceapp.data.FireStoreHelper
import com.voiceapp.data.model.Question
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import timber.log.Timber
import javax.inject.Inject

class QuestionViewModel @Inject constructor(
    private val fireStoreHelper: FireStoreHelper
): ViewModel() {

    private var questions: MutableLiveData<List<Question>> = MutableLiveData()
    private var question: MutableLiveData<Question> = MutableLiveData()

    fun getQuestions(projectId: String, interviewId: String): MutableLiveData<List<Question>> {
        fireStoreHelper.getQuestions(projectId, interviewId,
            OnSuccessListener { questions ->
                this.questions.value = questions
            },
            OnFailureListener { exception ->
                Timber.e(exception)
            }, live = false)
        return this.questions
    }

    fun getQuestion(projectId: String, interviewId: String, questionId: String): MutableLiveData<Question> {
        fireStoreHelper.getQuestion(projectId, interviewId, questionId,
            OnSuccessListener { question ->
                this.question.value = question
            },
            OnFailureListener { exception ->
                Timber.e(exception)
            }, live = false)
        return this.question
    }

}