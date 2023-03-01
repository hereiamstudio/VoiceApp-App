package com.voiceapp.core.dagger

import com.voiceapp.core.interview.FirebaseInterviewRepository
import com.voiceapp.core.interview.InterviewRepository
import dagger.Binds
import dagger.Module

@Module
internal interface InterviewModule {

    @Suppress("unused")
    @Binds
    fun bindInterviewRepository(firebaseInterviewRepository: FirebaseInterviewRepository):
            InterviewRepository
}