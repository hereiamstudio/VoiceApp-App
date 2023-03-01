package com.voiceapp.viewmodels

import androidx.lifecycle.ViewModel
import com.voiceapp.data.FireStoreHelper
import com.voiceapp.data.model.Answer
import com.voiceapp.data.model.Question
import com.voiceapp.data.model.RespondentInfo
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ResponseViewModel @Inject constructor(
    private val fireStoreHelper: FireStoreHelper
) : ViewModel() {

    fun uploadResponse(
        startTime: Date,
        projectId: String,
        interviewId: String,
        respondentInfo: RespondentInfo,
        questions: MutableList<Question>,
        answers: HashMap<String, Answer>,
        enumeratorNotes: String?) {

        val response = fireStoreHelper.createUploadResponse(
            startTime,
            projectId,
            interviewId,
            respondentInfo,
            questions,
            answers,
            enumeratorNotes)

        fireStoreHelper.saveResponse(response,
            {
                Timber.d("Upload win")
            },
            { exception ->
                Timber.e(exception)
            })
    }
}