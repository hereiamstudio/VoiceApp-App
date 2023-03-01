package com.voiceapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.IntDef
import com.voiceapp.Const
import com.voiceapp.R
import com.voiceapp.databinding.ActivityPincodeBinding
import com.voiceapp.ui.login.LoginActivity
import com.voiceapp.util.AnimationHelper
import com.voiceapp.util.MoveTextWatcher
import com.voiceapp.util.TextHelper
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

class PinCodeActivity : BaseActivity(false) {

    private lateinit var binding: ActivityPincodeBinding
    private var reset = false
    private var change = false

    override fun getRootView(): ViewGroup {
        return binding.root
    }

    @IntDef(
        PIN_SELECTION,    // Select a new pin
        PIN_CONFIRMATION, // Confirm a new pin
        PIN_VERIFICATION, // Verify current pin (login)
        PIN_RESET,        // Reset a pin (knowing account password)
        PIN_CHANGE        // Change a pin (knowing current pin)
    )
    annotation class PinType

    @PinType
    private var currentPinType = 0

    private var pinChars = listOf<EditText>()
    private var pinFirst: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPincodeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        pinChars = listOf(binding.pinChar1, binding.pinChar2, binding.pinChar3, binding.pinChar4, binding.pinChar5, binding.pinChar6)

        currentPinType = intent.getIntExtra(ARG_PIN_TYPE, PIN_VERIFICATION)

        val onPinCompleteListener: MoveTextWatcher.OnCompleteListener = object :
            MoveTextWatcher.OnCompleteListener{
            override fun onComplete() {
                pinComplete()
            }
        }

        for(i in 0..5){
            pinChars[i].addTextChangedListener(
                MoveTextWatcher(
                pinChars.getOrNull(i-1),
                pinChars[i],
                pinChars.getOrNull(i+1),
                onPinCompleteListener)
            )
        }

        pinChars[5].setOnEditorActionListener { _: TextView?, _: Int, _: KeyEvent? ->
            pinComplete()
            true
        }
        handler.postDelayed({ showKeyboard(pinChars[0]) }, 250)
        binding.btnForgotPin.setOnClickListener { forgotPinClicked() }
        binding.btnForgotPin.text = TextHelper.underlineText(getString(R.string.pincode_btn_forgot))

        // TODO: temporary workaround
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    override fun onResume() {
        super.onResume()
        moveToStage(currentPinType)
    }

    override fun onDestroy() {
        super.onDestroy()

        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }

    private fun forgotPinClicked() {
        if (intent.getIntExtra(ARG_PIN_TYPE, PIN_VERIFICATION) == PIN_CHANGE) {
            logout()
        } else {
            Intent(this@PinCodeActivity, ResetPinActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .let(this::startActivity)
            finish()
        }
    }

    private fun pinComplete() {
        pinChars[0].requestFocus()
        val pin = enteredPin ?: return
        onPinComplete(pin)
    }

    override fun onBackPressed() {
        // Do nothing
    }

    private fun moveToStage(pinType: Int) {
        currentPinType = pinType
        binding.pinText.setText(R.string.pincode_blurb_create)

        when (currentPinType) {
            PIN_SELECTION -> binding.pinTitle.text = if (reset) getString(R.string.pincode_title_reset) else getString(R.string.pincode_title_create)
            PIN_CONFIRMATION -> binding.pinTitle.text = if (reset) getString(R.string.pincode_title_validate_new) else getString(R.string.pincode_title_validate_existing)
            PIN_VERIFICATION -> {
                binding.btnForgotPin.visibility = View.VISIBLE
                binding.pinTitle.setText(R.string.pincode_title_verification)
                binding.pinText.setText(R.string.pincode_blurb_verify)

                if (!pinHelper.hasPin()) {
                    moveToStage(PIN_SELECTION)
                }
            }
            PIN_RESET -> {
                reset = true
                moveToStage(PIN_SELECTION)
            }
            PIN_CHANGE -> {
                change = true
                moveToStage(PIN_VERIFICATION)
            }
            else -> throw IllegalArgumentException("Invalid PIN type $pinType")
        }
        reset()
    }

    // ERROR
    private val enteredPin: String?
        get() {
            val pin = StringBuilder()
            for (charEditText in pinChars) {
                val c = charEditText.text.toString()
                if (TextUtils.isEmpty(c)) {
                    // ERROR
                    return null
                }
                pin.append(c)
            }
            return pin.toString()
        }

    private fun onPinComplete(enteredPin: String) {
        when (currentPinType) {
            PIN_SELECTION -> {
                // We've selected our pin, move on to confirmation of pin
                pinFirst = enteredPin
                moveToStage(PIN_CONFIRMATION)
            }
            PIN_CONFIRMATION ->                 // Check pins match...
                if (pinFirst == enteredPin) {
                    pinHelper.savePin(enteredPin)
                    var intent: Intent? =
                        Intent(this@PinCodeActivity, LanguagePackActivity::class.java)
                    if (change) intent = null
                    var message = getString(R.string.pincode_message_created)
                    if (reset) message = getString(R.string.pincode_message_reset) else if (change) message = getString(R.string.pincode_message_changed)
                    showSuccessScreen(message, intent)
                } else {
                    toastMessage(getString(R.string.pincode_snackbar_verification_failure))
                    onFailureAnimation()
                    moveToStage(PIN_SELECTION)
                }
            PIN_VERIFICATION -> {
                val correctPin = pinHelper.testPin(enteredPin)
                Timber.d("Correct pin %s %s", correctPin, enteredPin)
                if (correctPin) {
                    if (change) {
                        moveToStage(PIN_SELECTION)
                    } else {
                        showSuccessScreen(getString(R.string.pincode_message_success), null)
                    }
                } else {
                    val attempts = pinHelper.attempts
                    toastMessage(getString(R.string.pincode_message_invalid, attempts, Const.MAX_PIN_ATTEMPTS))

                    if (attempts > Const.MAX_PIN_ATTEMPTS) {
                        logout()
                    } else {
                        onFailureAnimation()
                    }
                }
            }
            PIN_RESET, PIN_CHANGE -> throw IllegalArgumentException(
                "Invalid PIN type $currentPinType"
            )
            else -> throw IllegalArgumentException("Invalid PIN type $currentPinType")
        }
    }

    private fun reset() {
        pinChars.forEach { editText -> editText.setText("") }
        pinChars[0].requestFocus()
        pinChars[0].requestFocusFromTouch()
    }

    private fun onFailureAnimation() {
        AnimationHelper.shakeAnimation(pinChars)
        handler.postDelayed({ reset() }, 500)
    }

    private fun showSuccessScreen(title: String, intent: Intent?) {
        pinHelper.locked = false
        hideKeyboard()
        binding.pinSuccessTitle.text = title
        binding.pinSuccessScreen.visibility = View.VISIBLE
        handler.postDelayed({
            intent?.let { startActivity(it) }
            finish()
        }, Const.DELAY_PIN_SUCCESS)
    }

    private fun logout() {
        val user = FirebaseAuth.getInstance()
        pinHelper.clearAll()
        user.signOut()
    }

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        if (auth.currentUser == null) {
            Intent(this@PinCodeActivity, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .let(this@PinCodeActivity::startActivity)
            finish()
        }
    }

    companion object {
        const val ARG_PIN_TYPE = "pin_type"
        const val PIN_SELECTION = 0 // Choose a pin
        const val PIN_CONFIRMATION = 1 // Confirm chosen pin
        const val PIN_VERIFICATION = 2 // Verify pin code is correct (unlock app)
        const val PIN_RESET = 3 // Reset the pin following a password auth
        const val PIN_CHANGE = 4 // Change the pin after confirming current pin
        fun createLink(context: Context?, @PinType pinType: Int): Intent {
            val intent = Intent(context, PinCodeActivity::class.java)
            intent.putExtra(ARG_PIN_TYPE, pinType)
            return intent
        }
    }

}