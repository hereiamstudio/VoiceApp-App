package com.voiceapp.dagger

import com.voiceapp.core.di.ForDefaultDispatcher
import com.voiceapp.core.di.ForGlobalCoroutineScope
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.*
import javax.inject.Singleton

@Module
internal class CoroutineModule {

    @OptIn(DelicateCoroutinesApi::class)
    @Singleton
    @Provides
    @ForGlobalCoroutineScope
    fun provideGlobalScope(): CoroutineScope = GlobalScope

    @Singleton
    @Provides
    @ForDefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}