package com.voiceapp.dagger.consent

import com.voiceapp.ui.interview.consent.ConsentCheckQuestionFragment
import com.voiceapp.ui.interview.consent.ConsentWrittenFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ConsentActivityModule {

    @Suppress("unused")
    @ContributesAndroidInjector
    fun contributeConsentWrittenFragment(): ConsentWrittenFragment

    @Suppress("unused")
    @ContributesAndroidInjector
    fun contributeConsentCheckQuestionFragment(): ConsentCheckQuestionFragment
}