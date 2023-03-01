package com.voiceapp.data.sync

import androidx.annotation.StringDef

const val SYNC_SYNCING: String = "syncing"
const val SYNC_COMPLETE: String = "complete"
const val SYNC_WAITING: String = "waiting"

@Target(AnnotationTarget.TYPE)
@StringDef(SYNC_SYNCING, SYNC_COMPLETE, SYNC_WAITING)
@Retention(AnnotationRetention.SOURCE)
annotation class SyncState
