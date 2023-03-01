package com.voiceapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.voiceapp.R
import com.voiceapp.activities.PinCodeActivity
import com.voiceapp.databinding.ActivityLoginBinding
import com.voiceapp.livedata.Event
import com.voiceapp.ui.onboarding.OnboardingActivity
import com.voiceapp.ui.project.ProjectListActivity
import com.voiceapp.ui.resetpassword.ResetPasswordActivity
import com.voiceapp.util.TextHelper
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LoginActivityViewModel by viewModels { viewModelFactory }

    private lateinit var viewBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.btnLogin.setOnClickListener {
            handleLoginClicked()
        }
        viewBinding.btnResetPassword.setOnClickListener {
            viewModel.onResetPasswordClicked()
        }
        // TODO: try to underline in XML rather than code
        viewBinding.btnResetPassword.text =
            TextHelper.underlineText(getString(R.string.resetpassword_btn_reset_password))

        viewModel.showResetPasswordLiveData.observe(this) {
            launchResetPassword()
        }
        viewModel.showEmailErrorLiveData.observe(this, this::handleEmailErrorState)
        viewModel.showPasswordErrorLiveData.observe(this, this::handlePasswordErrorState)
        viewModel.showProgressLiveData.observe(this, this::handleShowProgress)
        viewModel.showErrorLiveData.observe(this, this::handleShowError)
        viewModel.loginButtonEnabledLiveData.observe(this, this::handleLoginButtonEnabled)
        viewModel.showProjectListLiveData.observe(this, this::handleShowProjectList)
        viewModel.showCreatePinLiveData.observe(this, this::handleShowCreatePin)
    }

    private fun handleLoginClicked() {
        viewModel.onLoginClicked(
            viewBinding.email.text.toString(),
            viewBinding.password.text.toString())
    }

    private fun launchResetPassword() {
        startActivity(Intent(this, ResetPasswordActivity::class.java))
    }

    private fun handleEmailErrorState(hasError: Boolean) {
        viewBinding.emailLayout.error = if (hasError) {
            getString(R.string.required)
        } else {
            null
        }
    }

    private fun handlePasswordErrorState(hasError: Boolean) {
        viewBinding.passwordLayout.error = if (hasError) {
            getString(R.string.required)
        } else {
            null
        }
    }

    private fun handleShowProgress(showProgress: Boolean) {
        viewBinding.progressLayout.progressViewHolder.visibility = if (showProgress) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun handleShowError(event: Event<Unit>?) {
        if (event?.getContentIfNotHandled() != null) {
            Snackbar.make(viewBinding.root, R.string.loginactivity_snackbar_auth_failed,
                Snackbar.LENGTH_LONG)
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .show()
        }
    }

    private fun handleLoginButtonEnabled(enabled: Boolean) {
        viewBinding.btnLogin.isEnabled = enabled
    }

    private fun handleShowProjectList(event: Event<Unit>?) {
        if (event?.getContentIfNotHandled() != null) {
            Intent(this, ProjectListActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .let(this::startActivity)
            finish()
        }
    }

    private fun handleShowCreatePin(event: Event<Unit>?) {
        if (event?.getContentIfNotHandled() != null) {
            startActivities(
                arrayOf(
                    Intent(this, PinCodeActivity::class.java)
                        .putExtra(PinCodeActivity.ARG_PIN_TYPE, PinCodeActivity.PIN_SELECTION)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    Intent(this, OnboardingActivity::class.java)))

            finish()
        }
    }
}