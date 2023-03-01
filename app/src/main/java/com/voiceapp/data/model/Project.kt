package com.voiceapp.data.model

import androidx.annotation.Keep

@Keep
class Project : BaseObject(){
    var interviews: List<Interview> = mutableListOf()
    val title: String? = null
    val description: String? = null
    val is_active = true
    val is_archived = false
    val interviews_count = 0

    fun getInterviewCount(): Int {
        return interviews.size
    }
}