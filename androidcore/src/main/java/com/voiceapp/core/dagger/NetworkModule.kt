package com.voiceapp.core.dagger

import com.voiceapp.core.network.AndroidConnectivityRepository
import com.voiceapp.core.network.ConnectivityRepository
import dagger.Binds
import dagger.Module

@Module
internal interface NetworkModule {

    @Suppress("unused")
    @Binds
    fun bindConnectivityRepository(androidConnectivityRepository: AndroidConnectivityRepository):
            ConnectivityRepository
}