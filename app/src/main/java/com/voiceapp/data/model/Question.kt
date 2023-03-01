package com.voiceapp.data.model

import androidx.annotation.Keep
import androidx.annotation.StringDef

@Keep
class Question : BaseObject() {

    companion object {
        @StringDef(SINGLE_CODE, MULTI_CODE, FREE_TEXT)
        @Retention(AnnotationRetention.SOURCE)
        annotation class QuestionType

        const val SINGLE_CODE = "single_code"
        const val MULTI_CODE = "multi_code"
        const val FREE_TEXT = "free_text"
    }

    var title: String? = null
    val description: String? = null
    @QuestionType
    var type: String? = null
    val is_archived = false
    var is_probing_question = false
    var order = 0
    var questionNumberText: String? = null
    val options: List<String>? = null
    val skip_logic: List<SkipCriteria>? = null
}