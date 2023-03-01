package com.voiceapp.data.sync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.voiceapp.core.sync.SyncRepository
import com.voiceapp.dagger.inject
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

class SyncWorker(
    context: Context,
    params: WorkerParameters) : Worker(context, params) {

    @Inject
    lateinit var syncRepository: SyncRepository

    init {
        inject(this, context)
    }

    override fun doWork(): Result {
        Timber.d("Sync working starting...")

        // TODO: properly convert this to coroutines.
        runBlocking {
            syncRepository.performSync()
        }

        return Result.success()
    }
}