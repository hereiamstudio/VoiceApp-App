package com.voiceapp.dagger

import android.content.Context
import androidx.work.Worker
import dagger.android.HasAndroidInjector

fun inject(worker: Worker, context: Context) {
    val application = context.applicationContext

    (application as? HasAndroidInjector)
            ?.let { inject(worker, it) }
}

private fun inject(target: Any, hasAndroidInjector: HasAndroidInjector) {
    hasAndroidInjector.androidInjector()
            .inject(target)
}