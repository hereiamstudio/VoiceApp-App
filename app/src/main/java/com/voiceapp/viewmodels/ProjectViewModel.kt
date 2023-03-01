package com.voiceapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.voiceapp.data.FireStoreHelper
import com.voiceapp.data.model.Project
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import timber.log.Timber
import javax.inject.Inject

class ProjectViewModel @Inject constructor(
    private val fireStoreHelper: FireStoreHelper
): ViewModel() {

    private var projectLive: MutableLiveData<Project> = MutableLiveData()

    fun getProject(projectId: String): LiveData<Project> {
        fireStoreHelper.getProject(projectId,
            OnSuccessListener { project ->
                this.projectLive.value = project
            },
            OnFailureListener { exception ->
                Timber.e(exception)
            }, live = false)
        return projectLive
    }
}