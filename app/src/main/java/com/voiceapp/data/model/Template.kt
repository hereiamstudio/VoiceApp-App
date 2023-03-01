package com.voiceapp.data.model

import androidx.annotation.Keep

@Keep
class Template : BaseObject() {
    var question_title: String? = null
    var question_description: String? = null
    var primary_language: String? = null
}