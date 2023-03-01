package com.voiceapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class ScreenReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == null) {
            return
        }
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            Timber.i("Screen turned off, locking app")
            PinHelper(context).locked = true
        }
    }
}