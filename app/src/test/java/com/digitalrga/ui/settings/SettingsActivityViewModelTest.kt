package com.voiceapp.ui.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.voiceapp.core.appproperties.AppPropertiesRepository
import com.voiceapp.core.user.UserRepository
import com.voiceapp.testutils.LiveDataTestObserver
import com.voiceapp.ui.settings.SettingsActivityViewModel
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SettingsActivityViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var appPropertiesRepository: AppPropertiesRepository

    private lateinit var viewModel: SettingsActivityViewModel

    private val showResetPinObserver = LiveDataTestObserver<Unit?>()
    private val showLanguagePackSettingsObserver = LiveDataTestObserver<Unit?>()
    private val showGuideObserver = LiveDataTestObserver<Unit?>()
    private val showLoginObserver = LiveDataTestObserver<Unit?>()
    private val versionNameObserver = LiveDataTestObserver<String>()

    @Before
    fun setUp() {
        viewModel = SettingsActivityViewModel(userRepository, appPropertiesRepository)
    }

    @Test
    fun showResetPinIsEmptyByDefault() {
        registerLiveDataObservers()

        showResetPinObserver.assertEmpty()
    }

    @Test
    fun onResetPinClickedCausesResetPinScreenToBeShown() {
        registerLiveDataObservers()
        viewModel.onResetPinClicked()

        showResetPinObserver.assertValues(null)
    }

    @Test
    fun showLanguagePackSettingsIsEmptyByDefault() {
        registerLiveDataObservers()

        showLanguagePackSettingsObserver.assertEmpty()
    }

    @Test
    fun onLanguagePacksClickedCausesLanguagePacksScreenToBeShown() {
        registerLiveDataObservers()
        viewModel.onLanguagePacksClicked()

        showLanguagePackSettingsObserver.assertValues(null)
    }

    @Test
    fun onGuideClickedCausesGuideToBeShown() {
        registerLiveDataObservers()
        viewModel.onGuideClicked()

        showGuideObserver.assertValues(null)
    }

    @Test
    fun showLoginIsEmptyByDefault() {
        registerLiveDataObservers()

        showLoginObserver.assertEmpty()
    }

    @Test
    fun onLogoutClickedSignsUserOutAndShowsLoginScreen() {
        registerLiveDataObservers()
        viewModel.onLogoutClicked()

        verify(userRepository)
            .signOut()
        showLoginObserver.assertValues(null)
    }

    @Test
    fun versionNameShowsVersionNameFromAppProperties() {
        whenever(appPropertiesRepository.userVisibleVersion)
            .thenReturn("1.2.3")

        registerLiveDataObservers()

        versionNameObserver.assertValues("1.2.3")
    }

    private fun registerLiveDataObservers() {
        viewModel.showResetPinLiveData.observeForever(showResetPinObserver)
        viewModel.showLanguagePackSettingsLiveData.observeForever(showLanguagePackSettingsObserver)
        viewModel.showGuideLiveData.observeForever(showGuideObserver)
        viewModel.showLoginLiveData.observeForever(showLoginObserver)
        viewModel.versionNameLiveData.observeForever(versionNameObserver)
    }
}