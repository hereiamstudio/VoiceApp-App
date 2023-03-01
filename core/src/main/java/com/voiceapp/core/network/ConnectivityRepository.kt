package com.voiceapp.core.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityRepository {

    val hasInternetConnectivity: Boolean

    val hasInternetConnectivityFlow: Flow<Boolean>
}