package com.voiceapp

import android.Manifest

object Const {

    // DEBUG VARS
    val SKIP_PIN = BuildConfig.DEBUG && false
    val SKIP_CONSENT = BuildConfig.DEBUG && false

    val ENCRYPTION_ENABLED = false

    const val DEFAULT_LANGUAGE = "en"
    const val DEFAULT_LOCALE = "en-GB"

    // Sync settings
    const val SYNC_PERIOD_MINUTES: Long = 15

    // Password / Pin settings
    const val MAX_PIN_ATTEMPTS: Int = 5
    const val MIN_PASS_LENGTH: Long = 6

    // Grace period for returning to app without entering pin code
    const val APP_LOCK_TIMEOUT: Long = 30 * 1000 // 30s
    const val APP_LOCK_TIMEOUT_LANGUAGE_PACK: Long = 60 * 1000 // 1 min
    const val APP_LOCK_TIMEOUT_VOICE_INPUT: Long = 5 * 60 * 1000 // 5 mins

    // Timeout / delay times
    const val TIMEOUT_VOICE_MILLIS: Int = 6000

    // Animation settings
    const val SHAKE_DURATION_MILLIS: Long = 600
    const val SHAKE_COUNT: Float = 6f
    const val ROTATION_DURATION_MILLIS: Long = 1500

    // Animation timing / screen delays
    const val DELAY_MINIMUM_ANIMATION_TIME: Long = 1500
    const val DELAY_PIN_SUCCESS: Long = 1500
    const val DELAY_INTERVIEW_SUCCESS: Long = 2500
    const val DELAY_LANGUAGE_DETECT: Long = 1500

    // Permissions the app needs
    val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO)
}