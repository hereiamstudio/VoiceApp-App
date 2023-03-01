package com.voiceapp.data.model

import androidx.annotation.Keep

@Keep
class Interview : BaseObject() {

    var questions: List<Question>? = null
    val title: String? = null
    val description: String? = null
    val is_archived = false
    var responses_count = 0
    val status = ""
    var seen = false
    val consent_step_1: InterviewConsent? = null
    val consent_step_2: InterviewConsent? = null
    val locale: String? = null
    val primary_language: String? = null
    val has_skip_logic = false

    fun getQuestionCount() : Int? {
        return questions?.size
    }
}