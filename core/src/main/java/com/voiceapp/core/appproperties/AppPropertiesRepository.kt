package com.voiceapp.core.appproperties

interface AppPropertiesRepository {

    val userVisibleVersion: String

    val versionCode: Long
}