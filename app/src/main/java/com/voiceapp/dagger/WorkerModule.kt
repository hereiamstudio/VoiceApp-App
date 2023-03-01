package com.voiceapp.dagger

import com.voiceapp.data.sync.SyncWorker
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface WorkerModule {

    @Suppress("unused")
    @ContributesAndroidInjector
    fun contributeSyncWorker(): SyncWorker
}