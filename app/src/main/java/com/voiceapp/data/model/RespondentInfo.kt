package com.voiceapp.data.model

import androidx.annotation.Keep

@Keep
class RespondentInfo: BaseObject(){
    var extraConsent: Boolean? = null
    var benificiary: Boolean? = null
    var gender: String? = null
    var consent_relationship: String? = null
    var age: Int = -1

    fun isComplete() : Boolean{
        return age != -1 && extraConsent != null && benificiary != null && gender != null
    }
}