package com.voiceapp.core.di

import javax.inject.Qualifier
import kotlin.annotation.MustBeDocumented
import kotlin.annotation.Retention

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ForDefaultDispatcher
