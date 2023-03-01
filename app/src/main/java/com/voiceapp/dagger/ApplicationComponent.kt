package com.voiceapp.dagger

import android.app.Application
import com.voiceapp.voiceappApplication
import com.voiceapp.core.dagger.CoreModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    ApplicationModule::class,
    CoreModule::class
])
interface ApplicationComponent {

    fun inject(application: voiceappApplication)

    @Component.Factory
    interface Factory {

        fun newApplicationComponent(
            @BindsInstance application: Application): ApplicationComponent
    }
}