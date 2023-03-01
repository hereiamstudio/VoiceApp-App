package com.voiceapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.voiceapp.data.FireStoreHelper
import com.voiceapp.data.model.Template
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import timber.log.Timber
import javax.inject.Inject

class TemplateViewModel @Inject constructor(
    private val fireStoreHelper: FireStoreHelper
): ViewModel() {

    private var templates: MutableLiveData<List<Template>> = MutableLiveData()

    fun getTemplates(): MutableLiveData<List<Template>> {
        fireStoreHelper.getTemplates(
            OnSuccessListener { templates ->
                this.templates.value = templates
            },
            OnFailureListener { exception ->
                Timber.e(exception)
            }, live = false)
        return this.templates
    }
}