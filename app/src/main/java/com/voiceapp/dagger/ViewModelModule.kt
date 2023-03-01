package com.voiceapp.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.voiceapp.ui.interview.consent.ConsentCheckQuestionFragmentViewModel
import com.voiceapp.ui.interview.consent.ConsentWrittenFragmentViewModel
import com.voiceapp.ui.login.LoginActivityViewModel
import com.voiceapp.ui.project.ProjectListActivityViewModel
import com.voiceapp.ui.resetpassword.ResetPasswordActivityViewModel
import com.voiceapp.ui.settings.SettingsActivityViewModel
import com.voiceapp.ui.whatsnew.WhatsNewActivityViewModel
import com.voiceapp.viewmodels.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(LoginActivityViewModel::class)
    fun bindLoginActivityViewModel(viewModel: LoginActivityViewModel): ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(ResetPasswordActivityViewModel::class)
    fun bindResetPasswordActivityViewModel(viewModel: ResetPasswordActivityViewModel): ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(ProjectListActivityViewModel::class)
    fun bindProjectListActivityViewModel(viewModel: ProjectListActivityViewModel): ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(SettingsActivityViewModel::class)
    fun bindSettingsActivityViewModel(viewModel: SettingsActivityViewModel): ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(InterviewViewModel::class)
    fun bindInterviewViewModel(viewModel: InterviewViewModel): ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(ProjectViewModel::class)
    fun bindProjectViewModel(viewModel: ProjectViewModel): ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(QuestionViewModel::class)
    fun bindQuestionViewModel(viewModel: QuestionViewModel): ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(ResponseViewModel::class)
    fun bindResponseViewModel(viewModel: ResponseViewModel): ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(TemplateViewModel::class)
    fun bindTemplateViewModel(viewModel: TemplateViewModel): ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(ConsentWrittenFragmentViewModel::class)
    fun bindConsentWrittenFragmentViewModel(viewModel: ConsentWrittenFragmentViewModel): ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(ConsentCheckQuestionFragmentViewModel::class)
    fun bindConsentCheckQuestionFragmentViewModel(viewModel: ConsentCheckQuestionFragmentViewModel)
            : ViewModel

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(WhatsNewActivityViewModel::class)
    fun bindWhatsNewActivityViewModel(viewModel: WhatsNewActivityViewModel): ViewModel

    @Suppress("unused")
    @Binds
    fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}