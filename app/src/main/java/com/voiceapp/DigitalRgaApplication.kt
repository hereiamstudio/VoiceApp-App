package com.voiceapp

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.voiceapp.dagger.DaggerApplicationComponent
import com.voiceapp.data.sync.SyncSetupHelper
import com.voiceapp.startup.ActivityLifecycleListener
import com.voiceapp.util.PinHelper
import com.voiceapp.util.ScreenReceiver
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

class voiceappApplication : Application(), HasAndroidInjector {

    companion object{
        lateinit var instance: voiceappApplication
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var activityLifecycleListener: ActivityLifecycleListener

    override fun onCreate() {
        super.onCreate()

        instance = this

        DaggerApplicationComponent.factory()
            .newApplicationComponent(this)
            .inject(this)

        initCrashlytics()
        initLogging()
        initFirebase()
        lockPin()
        initSyncWorker()
        initScreenTracking()

        registerActivityLifecycleCallbacks(activityLifecycleListener)
    }

    override fun androidInjector() = dispatchingAndroidInjector

    private fun initScreenTracking() {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        val mReceiver: BroadcastReceiver = ScreenReceiver()
        registerReceiver(mReceiver, filter)
    }

    private fun initCrashlytics() {
        FirebaseCrashlytics.getInstance().setCustomKey("debug", BuildConfig.DEBUG)
    }

    private fun initSyncWorker() {
        SyncSetupHelper().setupSyncWorker(this)
    }

    private fun initFirebase() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    private fun lockPin() {
        // Pin lock when a new application is created
        PinHelper(this).locked = true
    }

    private fun initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        else{
            Timber.plant(CrashReportingTree())
        }
    }

    private inner class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority >= Log.DEBUG) {
                FirebaseCrashlytics.getInstance().log(message)
                if (t != null) {
                    FirebaseCrashlytics.getInstance().recordException(t)
                }
            } else return
        }
    }

}