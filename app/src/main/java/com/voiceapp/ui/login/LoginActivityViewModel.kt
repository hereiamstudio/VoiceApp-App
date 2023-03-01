package com.voiceapp.ui.login

import androidx.lifecycle.*
import com.voiceapp.core.pin.PinRepository
import com.voiceapp.core.user.Credentials
import com.voiceapp.core.user.SignInState
import com.voiceapp.core.user.UserRepository
import com.voiceapp.core.util.TimestampUtils
import com.voiceapp.livedata.Event
import com.voiceapp.livedata.SingleLiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class LoginActivityViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pinRepository: PinRepository,
    private val timestampUtils: TimestampUtils
) : ViewModel() {

    private val credentials = MutableStateFlow<AuthRequest?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val signInState = credentials
        .flatMapLatest(this::performSignIn)
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.IDLE)

    val showProgressLiveData = signInState.map {
        it == UiState.PROGRESS
    }.distinctUntilChanged().asLiveData(viewModelScope.coroutineContext)

    val showErrorLiveData = signInState.filter {
        it == UiState.ERROR
    }.map {
        Event(Unit)
    }.asLiveData(viewModelScope.coroutineContext)

    val loginButtonEnabledLiveData = showProgressLiveData.map { !it }

    val showProjectListLiveData = signInState.filter {
        it == UiState.ALREADY_SIGNED_IN && pinRepository.hasPin
    }.map {
        Event(Unit)
    }.asLiveData(viewModelScope.coroutineContext)

    val showCreatePinLiveData = signInState.filter {
        it == UiState.SUCCESS || (it == UiState.ALREADY_SIGNED_IN && !pinRepository.hasPin)
    }.map {
        Event(Unit)
    }.asLiveData(viewModelScope.coroutineContext)

    val showResetPasswordLiveData: LiveData<Nothing> get() = showResetPassword
    private val showResetPassword = SingleLiveEvent<Nothing>()

    val showEmailErrorLiveData: LiveData<Boolean> get() = showEmailError
    private val showEmailError = MutableLiveData(false)

    val showPasswordErrorLiveData: LiveData<Boolean> get() = showPasswordError
    private val showPasswordError = MutableLiveData(false)

    fun onLoginClicked(email: String, password: String) {
        val emailHasError = email.isEmpty()
        val passwordHasError = password.isEmpty()

        showEmailError.value = emailHasError
        showPasswordError.value = passwordHasError

        if (!emailHasError && !passwordHasError) {
            credentials.value = AuthRequest(
                timestampUtils.timeNow, // This is done to uniquely identify the request
                Credentials(email, password)
            )
        }
    }

    fun onResetPasswordClicked() {
        showResetPassword.call()
    }

    private fun performSignIn(request: AuthRequest?) = if (userRepository.isUserLoggedIn) {
        flowOf(UiState.ALREADY_SIGNED_IN)
    } else {
        request?.let {
            userRepository.loginFlow(it.credentials)
                .map(this::mapSignInStateToUiState)
        } ?: flowOf(UiState.IDLE)
    }

    private fun mapSignInStateToUiState(signInState: SignInState) = when (signInState) {
        SignInState.PROGRESS -> UiState.PROGRESS
        SignInState.SUCCESS -> UiState.SUCCESS
        SignInState.ERROR -> UiState.ERROR
    }

    private data class AuthRequest(
        val timeNow: Long,
        val credentials: Credentials
    )
}