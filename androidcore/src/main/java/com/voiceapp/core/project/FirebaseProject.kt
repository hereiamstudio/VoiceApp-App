package com.voiceapp.core.project

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

internal data class FirebaseProject(
    @DocumentId val id: String = "",
    @PropertyName("title") val title: String? = null,
    @PropertyName("description") val description: String? = null)