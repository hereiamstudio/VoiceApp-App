package com.voiceapp.core.pin

import com.voiceapp.core.di.ForGlobalCoroutineScope
import com.voiceapp.core.user.UserRepository
import com.voiceapp.core.user.UserStatus
import com.voiceapp.core.util.TimestampUtils
import com.voiceapp.util.PinHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegacyPinRepository @Inject constructor(
    private val pinHelper: PinHelper,
    private val userRepository: UserRepository,
    private val timestampUtils: TimestampUtils,
    @ForGlobalCoroutineScope private val globalCoroutineScope: CoroutineScope) : PinRepository {

    init {
        globalCoroutineScope.launch {
            observeLogoutEvents()
        }
    }

    override val hasPin: Boolean
        get() = pinHelper.hasPin()

    override val isLocked: Boolean
        get() = pinHelper.locked

    override fun recordUserEnterScreen() {
        pinHelper.resumedTime = timestampUtils.timeSinceBootRealtime
    }

    override fun recordUserLeaveScreen() {
        pinHelper.leaveTime = timestampUtils.timeSinceBootRealtime
    }

    private suspend fun observeLogoutEvents() {
        userRepository.userStatusFlow
            .filter { it == UserStatus.SIGNED_OUT }
            .collect {
                pinHelper.clearAll()
            }
    }
}