package com.voiceapp.ui.whatsnew

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.voiceapp.core.appproperties.AppPropertiesRepository
import com.voiceapp.livedata.SingleLiveEvent
import javax.inject.Inject

class WhatsNewActivityViewModel @Inject constructor(
    appPropertiesRepository: AppPropertiesRepository
) : ViewModel() {

    val versionNameLiveData: LiveData<String> get() = versionName
    private val versionName by lazy { MutableLiveData(appPropertiesRepository.userVisibleVersion) }

    val closeLiveData: LiveData<Nothing> get() = close
    private val close = SingleLiveEvent<Nothing>()

    fun onDoneClicked() {
        close.call()
    }
}