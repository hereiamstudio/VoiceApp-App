package com.voiceapp.ui.resetpassword

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.voiceapp.R
import com.voiceapp.databinding.ActivityResetPasswordBinding
import com.voiceapp.livedata.Event
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import javax.inject.Inject

class ResetPasswordActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ResetPasswordActivityViewModel by viewModels { viewModelFactory }

    private lateinit var viewBinding: ActivityResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        viewBinding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.btnResetPassword.setOnClickListener {
            handleResetPasswordClicked()
        }

        viewModel.showProgressLiveData.observe(this, this::handleShowProgress)
        viewModel.emailFieldEnabledLiveData.observe(this, this::handleEmailFieldEnabled)
        viewModel.resetButtonEnabledLiveData.observe(this, this::handleResetButtonEnabled)
        viewModel.showEmailErrorLiveData.observe(this, this::handleEmailErrorState)
        viewModel.showSuccessLiveData.observe(this, this::handleShowSuccess)
        viewModel.showErrorLiveData.observe(this, this::handleShowError)
    }

    private fun handleResetPasswordClicked() {
        viewModel.onResetPasswordClicked(viewBinding.email.text.toString())
    }

    private fun handleShowProgress(showProgress: Boolean) {
        viewBinding.progressLayout.progressViewHolder.visibility = if (showProgress) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun handleEmailFieldEnabled(enabled: Boolean) {
        viewBinding.email.isEnabled = enabled
    }

    private fun handleResetButtonEnabled(enabled: Boolean) {
        viewBinding.btnResetPassword.isEnabled = enabled
    }

    private fun handleEmailErrorState(hasError: Boolean) {
        viewBinding.emailLayout.error = if (hasError) getString(R.string.required) else null
    }

    private fun handleShowSuccess(event: Event<Unit>?) {
        if (event?.getContentIfNotHandled() != null) {
            Snackbar.make(
                viewBinding.root,
                R.string.resetpassword_message_success,
                Snackbar.LENGTH_LONG)
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .show()
        }
    }

    private fun handleShowError(event: Event<Unit>?) {
        if (event?.getContentIfNotHandled() != null) {
            Snackbar.make(
                viewBinding.root,
                R.string.resetpassword_message_error,
                Snackbar.LENGTH_LONG)
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .show()
        }
    }
}