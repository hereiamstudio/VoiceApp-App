package com.voiceapp.core.pin

interface PinRepository {

    val hasPin: Boolean

    val isLocked: Boolean

    fun recordUserEnterScreen()

    fun recordUserLeaveScreen()
}