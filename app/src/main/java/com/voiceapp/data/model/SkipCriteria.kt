package com.voiceapp.data.model

import com.google.firebase.firestore.PropertyName

data class SkipCriteria(
    @PropertyName("type") val type: String? = null,
    @PropertyName("values") val values: List<String>? = null,
    @PropertyName("action") val action: String? = null,
    @PropertyName("questionId") val questionId: String? = null)