package com.voiceapp.util

import android.view.View
import android.view.animation.*
import com.voiceapp.Const

object AnimationHelper{

    fun shakeAnimation(views: List<View>, duration: Long = Const.SHAKE_DURATION_MILLIS) {
        val shake = TranslateAnimation(0f, 10f, 0f, 0f)
        shake.duration = duration
        shake.interpolator = CycleInterpolator(Const.SHAKE_COUNT)
        views.forEach { view -> view.startAnimation(shake) }
    }
}