package com.voiceapp.ui.whatsnew

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.voiceapp.core.appproperties.AppPropertiesRepository
import com.voiceapp.testutils.LiveDataTestObserver
import com.voiceapp.ui.whatsnew.WhatsNewActivityViewModel
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class WhatsNewActivityViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var appPropertiesRepository: AppPropertiesRepository

    private lateinit var viewModel: WhatsNewActivityViewModel

    private val versionNameObserver = LiveDataTestObserver<String>()
    private val closeObserver = LiveDataTestObserver<Nothing?>()

    @Before
    fun setUp() {
        viewModel = WhatsNewActivityViewModel(appPropertiesRepository)
    }

    @Test
    fun versionNameEmitsAppVersion() {
        whenever(appPropertiesRepository.userVisibleVersion)
            .thenReturn("1.2.3")

        registerLiveDataObservers()

        versionNameObserver.assertValues("1.2.3")
    }

    @Test
    fun closeIsEmptyByDefault() {
        registerLiveDataObservers()

        closeObserver.assertEmpty()
    }

    @Test
    fun closeEmitsCloseWhenDoneClicked() {
        registerLiveDataObservers()
        viewModel.onDoneClicked()

        closeObserver.assertValues(null)
    }

    private fun registerLiveDataObservers() {
        viewModel.versionNameLiveData.observeForever(versionNameObserver)
        viewModel.closeLiveData.observeForever(closeObserver)
    }
}