package com.voiceapp.data.model

import androidx.annotation.Keep

@Keep
class Answer : BaseObject() {

    var answerOptions: MutableList<String> = mutableListOf()

    var questionId: String? = null
    var order: Int = 0
    var skipped: Boolean = false
    var flagged: Boolean = false
    var starred: Boolean = false
    var usedMicrophone: Boolean = false
    var transcribedAnswer: String? = null

    fun addAnswer(answer: String) {
        answerOptions.add(answer)
    }

    fun removeAnswer(answer: String) {
        answerOptions.remove(answer)
    }

    fun removeAllAnswers() {
        answerOptions.clear()
    }

    fun hasAnswer(): Boolean {
        return answerOptions.size > 0
    }
}