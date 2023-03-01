package com.voiceapp.core.sync

interface SyncTask {

    suspend fun performSync(firstSync: Boolean)
}