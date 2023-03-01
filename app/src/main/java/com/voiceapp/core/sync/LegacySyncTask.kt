package com.voiceapp.core.sync

import com.voiceapp.data.sync.SyncService
import com.voiceapp.core.di.ForDefaultDispatcher
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LegacySyncTask @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val syncService: SyncService,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : SyncTask {

    override suspend fun performSync(firstSync: Boolean) = withContext(defaultDispatcher) {
        if (!firstSync) {
            suspendCoroutine<Unit> { continuation ->
                firestore.waitForPendingWrites().addOnCompleteListener {
                    continuation.resume(Unit)
                }
            }
        }

        suspendCoroutine<Unit> { continuation ->
            syncService.syncEverything {
                continuation.resume(Unit)
            }
        }
    }
}