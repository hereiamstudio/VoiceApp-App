package com.voiceapp.fragments

import com.voiceapp.data.model.Answer
import com.voiceapp.data.model.Question

interface ConsentInterface {

    fun onConsentConfirmed(confirmed: Boolean, extra:Boolean)
    fun onConsentExtraRelation(relation: String)

    fun onQuestionAnswered(answer: Answer)
    fun onAddQuestion(currentAnswerHasProgress:Boolean)
    fun onQuestionAdded(question: Question?)
    fun onQuestionRetake(questionId: String)
    fun onInterviewOverviewConfirmed()
    fun onInterviewCompleted()
}