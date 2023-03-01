package com.voiceapp.core.util

import android.os.SystemClock
import com.voiceapp.core.util.TimestampUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AndroidTimestampUtils @Inject constructor() : TimestampUtils {

    override val timeNow: Long
        get() = System.currentTimeMillis()

    override val timeSinceBootRealtime: Long
        get() = SystemClock.elapsedRealtime()
}