package com.voiceapp.core.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.voiceapp.core.network.ConnectivityRepository
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class AndroidConnectivityRepository @Inject constructor(
    private val connectivityManager: ConnectivityManager): ConnectivityRepository {

    override val hasInternetConnectivity get() =
        connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false

    override val hasInternetConnectivityFlow get() = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                launch {
                    handleNetworkEventForConnectivity(channel, network)
                }
            }

            override fun onLost(network: Network) {
                launch {
                    channel.send(false)
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        handleNetworkEventForConnectivity(channel, connectivityManager.activeNetwork)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()

    private suspend fun handleNetworkEventForConnectivity(
        channel: SendChannel<Boolean>,
        network: Network?) {
        val connected = network?.let {
            connectivityManager.getNetworkCapabilities(it)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false

        channel.send(connected)
    }
}