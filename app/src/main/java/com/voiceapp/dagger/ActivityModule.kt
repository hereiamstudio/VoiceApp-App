package com.voiceapp.dagger

import com.voiceapp.dagger.consent.ConsentActivityModule
import com.voiceapp.ui.login.LoginActivity
import com.voiceapp.ui.project.ProjectListActivity
import com.voiceapp.ui.resetpassword.ResetPasswordActivity
import com.voiceapp.ui.settings.SettingsActivity
import com.voiceapp.activities.ConsentActivity
import com.voiceapp.activities.InterviewListActivity
import com.voiceapp.ui.whatsnew.WhatsNewActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ActivityModule {

    @Suppress("unused")
    @ContributesAndroidInjector
    fun contributeLoginActivity(): LoginActivity

    @Suppress("unused")
    @ContributesAndroidInjector
    fun contributeResetPasswordActivity(): ResetPasswordActivity

    @Suppress("unused")
    @ContributesAndroidInjector
    fun contributeProjectListActivity(): ProjectListActivity

    @Suppress("unused")
    @ContributesAndroidInjector
    fun contributeSettingsActivity(): SettingsActivity

    @Suppress("unused")
    @ContributesAndroidInjector(modules = [
        ConsentActivityModule::class
    ])
    fun contributeConsentActivity(): ConsentActivity

    @Suppress("unused")
    @ContributesAndroidInjector
    fun contributeInterviewListActivity(): InterviewListActivity

    @Suppress("unused")
    @ContributesAndroidInjector
    fun contributeWhatsNewActivity(): WhatsNewActivity
}