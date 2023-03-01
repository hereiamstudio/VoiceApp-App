package com.voiceapp.ui.resetpassword

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.voiceapp.core.user.PasswordResetState
import com.voiceapp.core.user.UserRepository
import com.voiceapp.core.util.TimestampUtils
import com.voiceapp.coroutines.MainCoroutineRule
import com.voiceapp.livedata.Event
import com.voiceapp.testutils.LiveDataTestObserver
import com.voiceapp.ui.resetpassword.ResetPasswordActivityViewModel
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ResetPasswordActivityViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var timestampUtils: TimestampUtils

    private lateinit var viewModel: ResetPasswordActivityViewModel

    private val showProgressObserver = LiveDataTestObserver<Boolean>()
    private val emailFieldEnabledObserver = LiveDataTestObserver<Boolean>()
    private val resetButtonEnabledObserver = LiveDataTestObserver<Boolean>()
    private val showEmailErrorObserver = LiveDataTestObserver<Boolean>()
    private val showSuccessObserver = LiveDataTestObserver<Event<Unit>?>()
    private val showErrorObserver = LiveDataTestObserver<Event<Unit>?>()

    @Before
    fun setUp() {
        viewModel = ResetPasswordActivityViewModel(userRepository, timestampUtils)
    }

    @Test
    fun showProgressIsFalseByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showProgressObserver.assertValues(false)
    }

    @Test
    fun showProgressWithSuccessFlow() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flow {
                emit(PasswordResetState.PROGRESS)
                delay(100L)
                emit(PasswordResetState.SUCCESS)
            })

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        showProgressObserver.assertValues(false, true, false)
    }

    @Test
    fun showProgressWithErrorFlow() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flow {
                emit(PasswordResetState.PROGRESS)
                delay(100L)
                emit(PasswordResetState.ERROR)
            })

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        showProgressObserver.assertValues(false, true, false)
    }

    @Test
    fun emailFieldEnabledIsTrueByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        emailFieldEnabledObserver.assertValues(true)
    }

    @Test
    fun emailFieldEnabledWithSuccessFlow() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flow {
                emit(PasswordResetState.PROGRESS)
                delay(100L)
                emit(PasswordResetState.SUCCESS)
            })

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        emailFieldEnabledObserver.assertValues(true, false, true)
    }

    @Test
    fun emailFieldEnabledWithErrorFlow() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flow {
                emit(PasswordResetState.PROGRESS)
                delay(100L)
                emit(PasswordResetState.ERROR)
            })

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        emailFieldEnabledObserver.assertValues(true, false, true)
    }

    @Test
    fun resetButtonEnabledIsTrueByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        resetButtonEnabledObserver.assertValues(true)
    }

    @Test
    fun resetButtonEnabledWithSuccessFlow() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flow {
                emit(PasswordResetState.PROGRESS)
                delay(100L)
                emit(PasswordResetState.SUCCESS)
            })

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        resetButtonEnabledObserver.assertValues(true, false, true)
    }

    @Test
    fun resetButtonEnabledWithErrorFlow() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flow {
                emit(PasswordResetState.PROGRESS)
                delay(100L)
                emit(PasswordResetState.ERROR)
            })

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        resetButtonEnabledObserver.assertValues(true, false, true)
    }

    @Test
    fun showEmailErrorIsFalseByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showEmailErrorObserver.assertValues(false)
    }

    @Test
    fun showEmailErrorIsTrueWhenInvalidStringIsUsedForEmail() {
        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("")
        coroutineRule.scope.advanceUntilIdle()

        showEmailErrorObserver.assertValues(false, true)
    }

    @Test
    fun showEmailIsFalseWhenValidStringIsUsedForEmail() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flowOf(PasswordResetState.PROGRESS))

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        showEmailErrorObserver.assertValues(false, false)
    }

    @Test
    fun showSuccessDoesNotShowSuccessByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showSuccessObserver.assertEmpty()
    }

    @Test
    fun showSuccessDoesNotShowSuccessOnError() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flow {
                emit(PasswordResetState.PROGRESS)
                delay(100L)
                emit(PasswordResetState.ERROR)
            })

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        showSuccessObserver.assertEmpty()
    }

    @Test
    fun showSuccessShowsSuccessOnSuccess() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flow {
                emit(PasswordResetState.PROGRESS)
                delay(100L)
                emit(PasswordResetState.SUCCESS)
            })

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        assertEquals(1, showSuccessObserver.observedValues.size)
    }

    @Test
    fun showErrorDoesNotShowErrorByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showErrorObserver.assertEmpty()
    }

    @Test
    fun showErrorDoesNotShowErrorOnSuccess() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flow {
                emit(PasswordResetState.PROGRESS)
                delay(100L)
                emit(PasswordResetState.SUCCESS)
            })

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        showErrorObserver.assertEmpty()
    }

    @Test
    fun showErrorShowsErrorOnError() {
        whenever(userRepository.requestPasswordResetFlow("em@il.com"))
            .thenReturn(flow {
                emit(PasswordResetState.PROGRESS)
                delay(100L)
                emit(PasswordResetState.ERROR)
            })

        registerLiveDataObservers()
        viewModel.onResetPasswordClicked("em@il.com")
        coroutineRule.scope.advanceUntilIdle()

        assertEquals(1, showErrorObserver.observedValues.size)
    }

    private fun registerLiveDataObservers() {
        viewModel.showProgressLiveData.observeForever(showProgressObserver)
        viewModel.emailFieldEnabledLiveData.observeForever(emailFieldEnabledObserver)
        viewModel.resetButtonEnabledLiveData.observeForever(resetButtonEnabledObserver)
        viewModel.showEmailErrorLiveData.observeForever(showEmailErrorObserver)
        viewModel.showSuccessLiveData.observeForever(showSuccessObserver)
        viewModel.showErrorLiveData.observeForever(showErrorObserver)
    }
}