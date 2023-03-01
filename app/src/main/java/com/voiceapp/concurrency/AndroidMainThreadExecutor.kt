package com.voiceapp.concurrency

import android.content.Context
import android.os.Handler
import java.util.concurrent.Executor

class AndroidMainThreadExecutor(context: Context) : Executor {

    private val handler = Handler(context.mainLooper)

    override fun execute(command: Runnable) {
        handler.post(command)
    }
}