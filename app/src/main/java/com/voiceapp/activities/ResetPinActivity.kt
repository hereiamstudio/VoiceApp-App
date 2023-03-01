package com.voiceapp.activities

import android.os.Bundle
import android.view.View
import com.voiceapp.R

class ResetPinActivity : LoginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.divider.visibility = View.GONE
        binding.btnLogin.setText(R.string.resetpin_btn_reset)
    }

    override fun skipIfAlreadyLoggedIn(){}

    override fun onAuthSuccess(){
        startActivity(
            PinCodeActivity.createLink(
                this,
                PinCodeActivity.PIN_RESET
            )
        )
        finish()
    }

}