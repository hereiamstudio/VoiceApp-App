package com.voiceapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.voiceapp.core.appproperties.AppPropertiesRepository
import com.voiceapp.core.user.UserRepository
import com.voiceapp.livedata.SingleLiveEvent
import javax.inject.Inject

class SettingsActivityViewModel @Inject constructor(
    private val userRepository: UserRepository,
    appPropertiesRepository: AppPropertiesRepository
) : ViewModel() {

    val showResetPinLiveData: LiveData<Unit> get() = showResetPin
    private val showResetPin = SingleLiveEvent<Unit>()

    val showLanguagePackSettingsLiveData: LiveData<Unit> get() = showLanguagePackSettings
    private val showLanguagePackSettings = SingleLiveEvent<Unit>()

    val showGuideLiveData: LiveData<Unit> get() = showGuide
    private val showGuide = SingleLiveEvent<Unit>()

    val showLoginLiveData: LiveData<Unit> get() = showLogin
    private val showLogin = SingleLiveEvent<Unit>()

    val versionNameLiveData: LiveData<String> get() = versionName
    private val versionName by lazy { MutableLiveData(appPropertiesRepository.userVisibleVersion) }

    fun onResetPinClicked() {
        showResetPin.call()
    }

    fun onLanguagePacksClicked() {
        showLanguagePackSettings.call()
    }

    fun onGuideClicked() {
        showGuide.call()
    }

    fun onLogoutClicked() {
        userRepository.signOut()
        showLogin.call()
    }
}