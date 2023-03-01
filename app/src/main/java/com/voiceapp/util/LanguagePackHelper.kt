package com.voiceapp.util

import android.app.Activity
import android.content.*
import android.provider.Settings
import com.voiceapp.voiceappApplication
import com.voiceapp.Const
import timber.log.Timber

object LanguagePackHelper {

    private const val SHARED_PREFS = "language"
    private const val KEY_DEFAULT_LANG = "default_language"

    private fun getSharedPrefs(): SharedPreferences {
        return voiceappApplication.instance.getSharedPreferences(SHARED_PREFS, Activity.MODE_PRIVATE)
    }

    fun getDefaultLanguage(): String {
        return getSharedPrefs().getString(KEY_DEFAULT_LANG, Const.DEFAULT_LOCALE) ?: Const.DEFAULT_LOCALE
    }

    fun launchDownloadLanguagePackSettings(context: Context): Boolean {

        //startActivityForResult(Intent(Settings.ACTION_LOCALE_SETTINGS), 0)
        val components = arrayOf(
            ComponentName(
                "com.google.android.googlequicksearchbox",
                "com.google.android.apps.gsa.settingsui.VoiceSearchPreferences"
            ),
            ComponentName(
                "com.google.android.voicesearch",
                "com.google.android.voicesearch.VoiceSearchPreferences"
            ),
            ComponentName(
                "com.google.android.googlequicksearchbox",
                "com.google.android.voicesearch.VoiceSearchPreferences"
            ),
            ComponentName(
                "com.google.android.googlequicksearchbox",
                "com.google.android.apps.gsa.velvet.ui.settings.VoiceSearchPreferences"
            )
        )

        // Trying all the components to get directly to settings
        for (componentName in components) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                intent.component = componentName
                context.startActivity(intent)
                return true
            } catch (ex: ActivityNotFoundException) {
                Timber.e("Failed to install language data, no activity found for $intent)")
            }
        }

        // If we can't this setting will do...
        val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            Timber.d("Installing voice data: %s", intent.toUri(0))
            context.startActivity(intent)
            return true
        } catch (ex: ActivityNotFoundException) {
            Timber.e("Failed to install language data, no activity found for $intent)")
        }
        return false
    }
}