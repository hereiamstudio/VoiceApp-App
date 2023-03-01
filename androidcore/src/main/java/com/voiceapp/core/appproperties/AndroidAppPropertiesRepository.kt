package com.voiceapp.core.appproperties

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.voiceapp.core.appproperties.AppPropertiesRepository
import javax.inject.Inject

internal class AndroidAppPropertiesRepository @Inject constructor(
    private val context: Context,
    private val packageManager: PackageManager) : AppPropertiesRepository {

    override val userVisibleVersion: String
        get() = getAndroidUserVisibleVersion()

    override val versionCode: Long
        get() = getAndroidVersionCode()

    private fun getAndroidUserVisibleVersion() = try {
        packageManager.getPackageInfoCompat(context.packageName, 0).versionName
    } catch (ignored: PackageManager.NameNotFoundException) {
        "version unknown"
    }

    private fun getAndroidVersionCode() = try {
        packageManager.getPackageInfoCompat(context.packageName, 0)
            .let(PackageInfoCompat::getLongVersionCode)
    } catch (ignored: PackageManager.NameNotFoundException) {
        0L
    }

    private fun PackageManager.getPackageInfoCompat(
        packageName: String,
        flags: Int): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION")
            getPackageInfo(packageName, flags)
        }
    }
}