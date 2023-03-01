package com.voiceapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.voiceapp.R
import com.voiceapp.databinding.ActivityLoginBinding
import com.voiceapp.ui.resetpassword.ResetPasswordActivity
import com.voiceapp.util.TextHelper
import com.google.firebase.auth.FirebaseAuth

abstract class LoginActivity : BaseActivity(false) {

    lateinit var auth: FirebaseAuth

    lateinit var binding: ActivityLoginBinding

    override fun getRootView(): ViewGroup {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        requestPermissions()

        // If logged in skip...
        skipIfAlreadyLoggedIn()

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { loginClicked() }
        binding.btnResetPassword.setOnClickListener { resetClicked() }
        binding.btnResetPassword.text = TextHelper.underlineText(getString(R.string.resetpassword_btn_reset_password))
    }

    abstract fun skipIfAlreadyLoggedIn()

    private fun loginClicked() {
        val email: String = binding.email.text.toString()
        val password: String = binding.password.text.toString()

        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.error = getString(R.string.required)
            return
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.error = getString(R.string.required)
            return
        }

        binding.progressLayout.progressViewHolder.visibility = View.VISIBLE

        authenticate(email, password)
    }

    private fun authenticate(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@LoginActivity) { task ->
                if (!task.isSuccessful) {
                    binding.progressLayout.progressViewHolder.visibility = View.GONE
                    toastMessage(getString(R.string.loginactivity_snackbar_auth_failed))
                } else {
                    onAuthSuccess()
                }
            }
    }

    abstract fun onAuthSuccess()

    private fun resetClicked() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        startActivity(intent)
    }
}