package com.voiceapp.dagger

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FirebaseModule {

    @Singleton
    @Provides
    fun provideFirebaseAnalytics(context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)
}