package com.voiceapp.dagger

import android.app.Application
import android.content.Context
import android.os.Build
import com.voiceapp.analytics.AnalyticsManager
import com.voiceapp.analytics.FirebaseAnalyticsManager
import com.voiceapp.concurrency.AndroidMainThreadExecutor
import com.voiceapp.core.pin.LegacyPinRepository
import com.voiceapp.core.pin.PinRepository
import com.voiceapp.core.sync.LegacySyncTask
import com.voiceapp.core.sync.SyncTask
import com.voiceapp.data.model.upload.JsonResponseSerialiser
import com.voiceapp.data.model.upload.ResponseSerialiser
import com.voiceapp.di.ForMainThread
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import javax.inject.Singleton

@Module(includes = [
    ActivityModule::class,
    ApplicationModule.Bindings::class,
    CoroutineModule::class,
    FirebaseModule::class,
    ViewModelModule::class,
    WorkerModule::class
])
class ApplicationModule {

    @ForMainThread
    @Singleton
    @Provides
    fun provideMainThreadExecutor(context: Context): Executor {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.mainExecutor
        } else {
            AndroidMainThreadExecutor(context)
        }
    }

    @Singleton
    @Provides
    fun provideGson() = Gson()

    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindApplicationContext(application: Application): Context

        @Suppress("unused")
        @Binds
        fun bindResponseSerialiser(jsonResponseSerialiser: JsonResponseSerialiser)
                : ResponseSerialiser

        @Suppress("unused")
        @Binds
        fun bindAnalyticsManager(firebaseAnalyticsManager: FirebaseAnalyticsManager)
                : AnalyticsManager

        // TODO: replace with proper implementation.
        @Suppress("unused")
        @Binds
        fun bindSyncTask(legacySyncTask: LegacySyncTask): SyncTask

        // TODO: replace with proper implementation.
        @Suppress("unused")
        @Binds
        fun bindPinRepository(legacyPinRepository: LegacyPinRepository): PinRepository
    }
}