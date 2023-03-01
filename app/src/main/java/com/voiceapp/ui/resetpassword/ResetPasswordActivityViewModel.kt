package com.voiceapp.ui.resetpassword

import androidx.lifecycle.*
import com.voiceapp.core.user.PasswordResetState
import com.voiceapp.core.user.UserRepository
import com.voiceapp.core.util.TimestampUtils
import com.voiceapp.livedata.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ResetPasswordActivityViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val timestampUtils: TimestampUtils
) : ViewModel() {

    private val request = MutableStateFlow<ResetPasswordRequest?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val passwordResetState = request.map { it?.email }
        .flatMapLatest(this::performResetPasswordRequest)
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.IDLE)

    val showProgressLiveData = passwordResetState.map {
        it == UiState.PROGRESS
    }.distinctUntilChanged().asLiveData(viewModelScope.coroutineContext)

    val emailFieldEnabledLiveData = showProgressLiveData.map { !it }

    val resetButtonEnabledLiveData = showProgressLiveData.map { !it }

    val showEmailErrorLiveData: LiveData<Boolean> get() = showEmailError
    private val showEmailError = MutableLiveData(false)

    val showSuccessLiveData = passwordResetState.filter {
        it == UiState.SUCCESS
    }.map {
        Event(Unit)
    }.asLiveData(viewModelScope.coroutineContext)

    val showErrorLiveData = passwordResetState.filter {
        it == UiState.ERROR
    }.map {
        Event(Unit)
    }.asLiveData(viewModelScope.coroutineContext)

    fun onResetPasswordClicked(email: String) {
        val emailHasError = email.isEmpty()
        showEmailError.value = emailHasError

        if (!emailHasError) {
            request.value = ResetPasswordRequest(timestampUtils.timeNow, email)
        }
    }

    private fun performResetPasswordRequest(email: String?) = email?.let {
        userRepository.requestPasswordResetFlow(it)
            .map(this::mapPasswordResetStateToUiState)
    } ?: flowOf(UiState.IDLE)

    private fun mapPasswordResetStateToUiState(passwordResetState: PasswordResetState): UiState {
        return when (passwordResetState) {
            PasswordResetState.PROGRESS -> UiState.PROGRESS
            PasswordResetState.SUCCESS -> UiState.SUCCESS
            PasswordResetState.ERROR -> UiState.ERROR
        }
    }

    private data class ResetPasswordRequest(
        val timeNow: Long,
        val email: String)
}