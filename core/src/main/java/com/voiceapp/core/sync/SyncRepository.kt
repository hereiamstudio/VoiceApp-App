package com.voiceapp.core.sync

import com.voiceapp.core.network.ConnectivityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject internal constructor(
    private val syncTask: SyncTask,
    private val connectivityRepository: ConnectivityRepository
) {

    private val syncMutex = Mutex()
    private val isSyncingFlow = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val syncStateFlow: Flow<SyncState> get() = connectivityRepository.hasInternetConnectivityFlow
        .flatMapLatest(this::getSyncStateFlow)

    suspend fun performSync(firstSync: Boolean = false) {
        if (syncMutex.isLocked) {
            // No point carrying out a sync if there is already one in progress.
            return
        }

        // Only allow one sync at a time.
        syncMutex.withLock {
            if (connectivityRepository.hasInternetConnectivity) {
                isSyncingFlow.value = true
                syncTask.performSync(firstSync)
                isSyncingFlow.value = false
            }
        }
    }

    private fun getSyncStateFlow(isOnline: Boolean) = if (isOnline) {
        isSyncingFlow.map {
            if (it) SyncState.SYNCING else SyncState.IDLE
        }
    } else {
        flowOf(SyncState.OFFLINE)
    }
}