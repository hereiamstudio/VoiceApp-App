package com.voiceapp.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.voiceapp.R
import com.voiceapp.activities.LanguagePackActivity
import com.voiceapp.activities.PinCodeActivity
import com.voiceapp.dagger.ViewModelFactory
import com.voiceapp.databinding.ActivitySettingsBinding
import com.voiceapp.ui.login.LoginActivity
import com.voiceapp.ui.onboarding.OnboardingActivity
import dagger.android.AndroidInjection
import javax.inject.Inject

class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: SettingsActivityViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        val viewBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.appBar.toolbar)

        supportActionBar?.apply {
            title = getString(R.string.settings_title)
            setDisplayHomeAsUpEnabled(true)
        }

        viewBinding.btnPinCode.setOnClickListener { viewModel.onResetPinClicked() }
        viewBinding.btnLanguagePacks.setOnClickListener { viewModel.onLanguagePacksClicked() }
        viewBinding.btnGuide.setOnClickListener { viewModel.onGuideClicked() }
        viewBinding.btnLogout.setOnClickListener { viewModel.onLogoutClicked() }

        viewModel.versionNameLiveData.observe(this) {
            viewBinding.textAppVersion.text = getString(R.string.settings_version, it)
        }
        viewModel.showResetPinLiveData.observe(this) {
            handleShowResetPin()
        }
        viewModel.showLanguagePackSettingsLiveData.observe(this) {
            handleShowLanguagePackSettings()
        }
        viewModel.showGuideLiveData.observe(this) {
            handleShowGuide()
        }
        viewModel.showLoginLiveData.observe(this) {
            handleShowLogin()
        }
    }

    private fun handleShowResetPin() {
        Intent(this, PinCodeActivity::class.java)
            .putExtra(PinCodeActivity.ARG_PIN_TYPE, PinCodeActivity.PIN_CHANGE)
            .let(this::startActivity)
        finish()
    }

    private fun handleShowLanguagePackSettings() {
        startActivity(Intent(this, LanguagePackActivity::class.java))
    }

    private fun handleShowGuide() {
        startActivity(Intent(this, OnboardingActivity::class.java))
    }

    private fun handleShowLogin() {
        Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let(this::startActivity)
    }
}