package com.voiceapp.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.voiceapp.core.pin.PinRepository
import com.voiceapp.core.user.Credentials
import com.voiceapp.core.user.SignInState
import com.voiceapp.core.user.UserRepository
import com.voiceapp.core.util.TimestampUtils
import com.voiceapp.coroutines.MainCoroutineRule
import com.voiceapp.livedata.Event
import com.voiceapp.testutils.LiveDataTestObserver
import com.voiceapp.ui.login.LoginActivityViewModel
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
import java.util.concurrent.atomic.AtomicLong

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginActivityViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var pinRepository: PinRepository
    @Mock
    private lateinit var timestampUtils: TimestampUtils

    private lateinit var viewModel: LoginActivityViewModel

    private val showProgressObserver = LiveDataTestObserver<Boolean>()
    private val showErrorObserver = LiveDataTestObserver<Event<Unit?>>()
    private val loginButtonEnabledObserver = LiveDataTestObserver<Boolean>()
    private val showProjectListObserver = LiveDataTestObserver<Event<Unit?>>()
    private val showCreatePinObserver = LiveDataTestObserver<Event<Unit>?>()
    private val showResetPasswordObserver = LiveDataTestObserver<Nothing?>()
    private val showEmailErrorObserver = LiveDataTestObserver<Boolean>()
    private val showPasswordErrorObserver = LiveDataTestObserver<Boolean>()

    private val timestampProvider = AtomicLong()

    @Before
    fun setUp() {
        viewModel = LoginActivityViewModel(userRepository, pinRepository, timestampUtils)

        whenever(timestampUtils.timeNow)
            .thenReturn(timestampProvider.incrementAndGet())
    }

    @Test
    fun showProgressIsFalseByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showProgressObserver.assertValues(false)
    }

    @Test
    fun showProgressWithSuccessFlow() {
        val credentials = Credentials("em@il.com", "123456")
        whenever(userRepository.loginFlow(credentials))
            .thenReturn(flow {
                emit(SignInState.PROGRESS)
                delay(100L)
                emit(SignInState.SUCCESS)
            })

        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "123456")
        coroutineRule.scope.advanceUntilIdle()

        showProgressObserver.assertValues(false, true, false)
    }

    @Test
    fun showProgressWithErrorFlow() {
        val credentials = Credentials("em@il.com", "123456")
        whenever(userRepository.loginFlow(credentials))
            .thenReturn(flow {
                emit(SignInState.PROGRESS)
                delay(100L)
                emit(SignInState.ERROR)
            })

        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "123456")
        coroutineRule.scope.advanceUntilIdle()

        showProgressObserver.assertValues(false, true, false)
    }

    @Test
    fun onResetPasswordClickedRequestsShowResetPassword() {
        registerLiveDataObservers()

        viewModel.onResetPasswordClicked()
        coroutineRule.scope.advanceUntilIdle()

        showResetPasswordObserver.assertValues(null)
    }

    @Test
    fun showErrorIsEmptyByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showErrorObserver.assertEmpty()
    }

    @Test
    fun showErrorDoesNotEmitWhenLoginIsSuccessful() {
        val credentials = Credentials("em@il.com", "123456")
        whenever(userRepository.loginFlow(credentials))
            .thenReturn(flowOf(SignInState.PROGRESS, SignInState.SUCCESS))

        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "123456")
        coroutineRule.scope.advanceUntilIdle()

        showErrorObserver.assertEmpty()
    }

    @Test
    fun showErrorEmitsWhenLoginHasError() {
        val credentials = Credentials("em@il.com", "123456")
        whenever(userRepository.loginFlow(credentials))
            .thenReturn(flowOf(SignInState.PROGRESS, SignInState.ERROR))

        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "123456")
        coroutineRule.scope.advanceUntilIdle()

        assertEquals(1, showErrorObserver.observedValues.size)
    }

    @Test
    fun loginButtonIsEnabledByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        loginButtonEnabledObserver.assertValues(true)
    }

    @Test
    fun loginButtonIsEnabledWithSuccessFlow() {
        val credentials = Credentials("em@il.com", "123456")
        whenever(userRepository.loginFlow(credentials))
            .thenReturn(flow {
                emit(SignInState.PROGRESS)
                delay(100L)
                emit(SignInState.SUCCESS)
            })

        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "123456")
        coroutineRule.scope.advanceUntilIdle()

        loginButtonEnabledObserver.assertValues(true, false, true)
    }

    @Test
    fun loginButtonIsEnabledWithErrorFlow() {
        val credentials = Credentials("em@il.com", "123456")
        whenever(userRepository.loginFlow(credentials))
            .thenReturn(flow {
                emit(SignInState.PROGRESS)
                delay(100L)
                emit(SignInState.ERROR)
            })

        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "123456")
        coroutineRule.scope.advanceUntilIdle()

        loginButtonEnabledObserver.assertValues(true, false, true)
    }

    @Test
    fun showProjectListDoesNotShowWhenNotSignedInAndPinNotSet() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showProjectListObserver.assertEmpty()
    }

    @Test
    fun showProjectListDoesNotShowWhenSignedInAndPinNotSet() {
        whenever(userRepository.isUserLoggedIn)
            .thenReturn(true)

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showProjectListObserver.assertEmpty()
    }

    @Test
    fun showProjectListShowsProjectListWhenSignedInAndPinSet() {
        whenever(userRepository.isUserLoggedIn)
            .thenReturn(true)
        whenever(pinRepository.hasPin)
            .thenReturn(true)

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        assertEquals(1, showProjectListObserver.observedValues.size)
    }

    @Test
    fun showCreatePinDoesNotShowCreatePinWhenUserIsNotSignedIn() {
        whenever(userRepository.isUserLoggedIn)
            .thenReturn(false)

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showCreatePinObserver.assertEmpty()
    }

    @Test
    fun showCreatePinDoesNotShowCreatePinWhenUserIsSignedInAndPinAlreadySet() {
        whenever(userRepository.isUserLoggedIn)
            .thenReturn(true)
        whenever(pinRepository.hasPin)
            .thenReturn(true)

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showCreatePinObserver.assertEmpty()
    }

    @Test
    fun showCreatePinShowsCreatePinWhenUserIsSignedInAndPinNotSet() {
        whenever(userRepository.isUserLoggedIn)
            .thenReturn(true)
        whenever(pinRepository.hasPin)
            .thenReturn(false)

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        assertEquals(1, showCreatePinObserver.observedValues.size)
    }

    @Test
    fun showCreatePinDoesNotShowCreatePinDuringSignInFlowWhenSignInStateIsError() {
        val credentials = Credentials("em@il.com", "123456")
        whenever(userRepository.loginFlow(credentials))
            .thenReturn(flowOf(SignInState.PROGRESS, SignInState.ERROR))

        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "123456")
        coroutineRule.scope.advanceUntilIdle()

        showCreatePinObserver.assertEmpty()
    }

    @Test
    fun showCreatePinDoesShowsCreatePinOnSignInSuccess() {
        val credentials = Credentials("em@il.com", "123456")
        whenever(userRepository.loginFlow(credentials))
            .thenReturn(flowOf(SignInState.PROGRESS, SignInState.SUCCESS))

        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "123456")
        coroutineRule.scope.advanceUntilIdle()

        assertEquals(1, showCreatePinObserver.observedValues.size)
    }

    @Test
    fun showResetPasswordShowsResetPasswordWhenCalled() {
        registerLiveDataObservers()
        viewModel.onResetPasswordClicked()
        coroutineRule.scope.advanceUntilIdle()

        showResetPasswordObserver.assertValues(null)
    }

    @Test
    fun showEmailErrorIsFalseByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showEmailErrorObserver.assertValues(false)
    }

    @Test
    fun showEmailErrorIsTrueWhenEmailIsSubmittedAsEmpty() {
        registerLiveDataObservers()
        viewModel.onLoginClicked("", "123456")
        coroutineRule.scope.advanceUntilIdle()

        showEmailErrorObserver.assertValues(false, true)
    }

    @Test
    fun showEmailErrorIsFalseWhenEmailIsSubmittedAsPopulated() {
        val credentials = Credentials("em@il.com", "123456")
        whenever(userRepository.loginFlow(credentials))
            .thenReturn(flowOf(SignInState.PROGRESS))

        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "123456")
        coroutineRule.scope.advanceUntilIdle()

        showEmailErrorObserver.assertValues(false, false)
    }

    @Test
    fun showPasswordErrorIsFalseByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showPasswordErrorObserver.assertValues(false)
    }

    @Test
    fun showPasswordErrorIsTrueWhenPasswordIsSubmittedAsEmpty() {
        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "")
        coroutineRule.scope.advanceUntilIdle()

        showPasswordErrorObserver.assertValues(false, true)
    }

    @Test
    fun showPasswordErrorIsFalseWhenPasswordIsSubmittedAsPopulated() {
        val credentials = Credentials("em@il.com", "123456")
        whenever(userRepository.loginFlow(credentials))
            .thenReturn(flowOf(SignInState.PROGRESS))

        registerLiveDataObservers()
        viewModel.onLoginClicked("em@il.com", "123456")
        coroutineRule.scope.advanceUntilIdle()

        showPasswordErrorObserver.assertValues(false, false)
    }

    private fun registerLiveDataObservers() {
        viewModel.showProgressLiveData.observeForever(showProgressObserver)
        viewModel.showErrorLiveData.observeForever(showErrorObserver)
        viewModel.loginButtonEnabledLiveData.observeForever(loginButtonEnabledObserver)
        viewModel.showProjectListLiveData.observeForever(showProjectListObserver)
        viewModel.showCreatePinLiveData.observeForever(showCreatePinObserver)
        viewModel.showResetPasswordLiveData.observeForever(showResetPasswordObserver)
        viewModel.showEmailErrorLiveData.observeForever(showEmailErrorObserver)
        viewModel.showPasswordErrorLiveData.observeForever(showPasswordErrorObserver)
    }
}