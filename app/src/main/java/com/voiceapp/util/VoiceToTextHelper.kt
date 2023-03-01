package com.voiceapp.util

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import com.voiceapp.voiceappApplication
import com.voiceapp.Const
import com.voiceapp.R
import com.voiceapp.activities.BaseActivity
import timber.log.Timber
import java.util.*

class VoiceToTextHelper {

    // Look at this https://stackoverflow.com/questions/13670378/voice-recognition-stops-listening-after-a-few-seconds

    private var voiceResponseListener: VoiceResponseListener? = null

    companion object {
        const val REQUEST_RECOGNIZER: Int = 95
    }

    interface VoiceResponseListener {
        fun onResponse(text: String)
    }

    fun addListener(voiceResponseListener: VoiceResponseListener){
        this.voiceResponseListener = voiceResponseListener
    }

    fun getRecogniserIntent(
        language: String?,
        offline: Boolean
    ): Intent {
        val listenIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        listenIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, Const.TIMEOUT_VOICE_MILLIS)
        listenIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, Const.TIMEOUT_VOICE_MILLIS)
        listenIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000)
        listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, voiceappApplication.instance.getString(R.string.help_free_text))
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        listenIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
        listenIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, offline)
        listenIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        return listenIntent
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            BaseActivity.REQUEST_RECOGNIZER -> {
                Timber.d("Recogniser result: %d", resultCode)
                if (resultCode == Activity.RESULT_OK) {
                    val results = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                    if (results != null && results.size > 0) {
                        for (result in results) {
                            Timber.d("Recogniser result: %s", result)
                        }

                        voiceResponseListener?.onResponse(results[0].replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        })
                    }
                }
            }
        }
    }

}