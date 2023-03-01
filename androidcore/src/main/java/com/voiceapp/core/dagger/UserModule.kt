package com.voiceapp.core.dagger

import com.voiceapp.core.user.FirebaseUserRepository
import com.voiceapp.core.user.UserRepository
import dagger.Binds
import dagger.Module

@Module
internal interface UserModule {

    @Suppress("unused")
    @Binds
    fun bindUserRepository(firebaseUserRepository: FirebaseUserRepository): UserRepository
}