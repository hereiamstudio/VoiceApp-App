package com.voiceapp.data.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.voiceapp.Const
import java.util.concurrent.TimeUnit

class SyncSetupHelper {

    companion object {
        const val WORKER_NAME = "rga-sync-worker"
    }

    /**
     * Setup the sync worker to run periodically every X minutes as defined Const
     */
    fun setupSyncWorker(context: Context){
        val uploadWorkRequest: PeriodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(Const.SYNC_PERIOD_MINUTES, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORKER_NAME, ExistingPeriodicWorkPolicy.KEEP, uploadWorkRequest)
    }

}