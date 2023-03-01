package com.voiceapp.data.model

import androidx.annotation.Keep

@Keep
class InterviewConsent : BaseObject() {

    val script: String? = null
    val description: String? = null
    val confirmation_question: String? = null
    val confirmation_options: List<ConsentAnswer>? = null
}