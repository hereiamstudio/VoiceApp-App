package com.voiceapp.data.model.upload

import androidx.annotation.Keep
import com.voiceapp.data.model.BaseObject

/**

id
title
order
is_custom
is_flagged
is_skipped
[answers]
 */

@Keep
class ResponseQuestion(
    id: String?,
    var title: String?,
    var order: Int?,
    var type: String?,
    var probing_question: Boolean?,
    var skipped: Boolean?,
    var flagged: Boolean?,
    var starred: Boolean?,
    var transcription: Boolean?,
    var answers: MutableList<String>?,
    var transcribed_answer: String?
) : BaseObject() {

    init {
        super.id = id
    }

    override fun toString(): String {
        // Make it json style for easier reading

        var answerText = ""

        answers?.forEach {
            if (answerText.isNotEmpty()){
                answerText+=","
            }
            answerText+="\"$it\"" }

        return "{" +
                "\"id\":\"$id\"," +
                "\"title\":\"$title\"," +
                "\"order\":$order," +
                "\"type\":\"$type\"," +
                "\"probing_question\":$probing_question," +
                "\"flagged\":$flagged," +
                "\"starred\":$starred," +
                "\"skipped\":$skipped," +
                "\"transcription\":$transcription," +
                "\"answers\":[" + answerText + "]," +
                "\"transcribed_answer\":\"$transcribed_answer\"}"
    }
}