package com.voiceapp.core.dagger

import com.voiceapp.core.appproperties.AndroidAppPropertiesRepository
import com.voiceapp.core.appproperties.AppPropertiesRepository
import com.voiceapp.core.settings.AndroidSettingsRepository
import com.voiceapp.core.settings.SettingsRepository
import com.voiceapp.core.util.AndroidTimestampUtils
import com.voiceapp.core.util.TimestampUtils
import dagger.Binds
import dagger.Module

@Module(includes = [
    AndroidModule::class,
    CoreModule.Bindings::class,
    FirebaseModule::class,
    InterviewModule::class,
    NetworkModule::class,
    ProjectModule::class,
    UserModule::class
])
class CoreModule {

    @Module
    internal interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindTimestampUtils(androidTimestampUtils: AndroidTimestampUtils): TimestampUtils

        @Suppress("unused")
        @Binds
        fun bindAppPropertiesRepository(
            androidAppPropertiesRepository: AndroidAppPropertiesRepository
        ): AppPropertiesRepository

        @Suppress("unused")
        @Binds
        fun bindSettingsRepository(androidSettingsRepository: AndroidSettingsRepository):
                SettingsRepository
    }
}