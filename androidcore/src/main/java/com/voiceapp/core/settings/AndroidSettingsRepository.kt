package com.voiceapp.core.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.voiceapp.core.settings.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AndroidSettingsRepository @Inject constructor(
    private val preferences: SharedPreferences) : SettingsRepository {

    companion object {

        private const val KEY_LAST_SEEN_VERSION = "lastSeenVersion"
    }

    override var lastSeenVersionCode: Long
        get() = preferences.getLong(KEY_LAST_SEEN_VERSION, 0)
        set(value) {
            preferences.edit {
                putLong(KEY_LAST_SEEN_VERSION, value)
            }
        }
}