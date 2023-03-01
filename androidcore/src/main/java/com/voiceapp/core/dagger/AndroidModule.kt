package com.voiceapp.core.dagger

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import dagger.Module
import dagger.Provides

@Module
internal class AndroidModule {

    companion object {

        private const val PREFERENCES_FILE_NAME = "preferences"
    }

    @Provides
    fun provideConnectivityManager(context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    fun providePackageManager(context: Context): PackageManager = context.packageManager

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
}