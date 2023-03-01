package com.voiceapp.startup

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.voiceapp.activities.PinCodeActivity
import com.voiceapp.activities.ResetPinActivity
import com.voiceapp.core.pin.PinRepository
import com.voiceapp.ui.login.LoginActivity
import com.voiceapp.ui.onboarding.OnboardingActivity
import com.voiceapp.ui.resetpassword.ResetPasswordActivity
import javax.inject.Inject

class ActivityLifecycleListener @Inject constructor(
    private val pinRepository: PinRepository
) : Application.ActivityLifecycleCallbacks {

    private val pinExemptActivities = setOf(
        LoginActivity::class,
        OnboardingActivity::class,
        PinCodeActivity::class,
        ResetPasswordActivity::class,
        ResetPinActivity::class
    )

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        pinRepository.recordUserEnterScreen()

        if (!pinExemptActivities.contains(activity::class) && pinRepository.isLocked) {
            Intent(activity, PinCodeActivity::class.java)
                .putExtra(PinCodeActivity.ARG_PIN_TYPE, PinCodeActivity.PIN_VERIFICATION)
                .let(activity::startActivity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        pinRepository.recordUserLeaveScreen()
    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }
}