package com.voiceapp.activities

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.voiceapp.Const
import com.voiceapp.R
import com.voiceapp.util.PinHelper
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

abstract class BaseActivity(private val needsLogin:Boolean) : AppCompatActivity() {

    companion object {
        const val REQUEST_RECOGNIZER: Int = 95
        const val REQUEST_PERMISSIONS: Int = 100
    }

    var pinHelper: PinHelper = PinHelper(this)
    var running = true
    val handler = Handler(Looper.getMainLooper())

    abstract fun getRootView(): ViewGroup?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("Oncreate %s", this.javaClass.simpleName)
    }

    fun requestPermissions(){
        ActivityCompat.requestPermissions(
            this,
            Const.PERMISSIONS,
            REQUEST_PERMISSIONS
        )
    }

    override fun onPause() {
        super.onPause()
        running = false
    }

    open fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focus = currentFocus ?: return
        val windowToken = focus.windowToken ?: return
        imm.hideSoftInputFromWindow(
            windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    open fun showKeyboard(view: View) {
        view.requestFocus()
        view.requestFocusFromTouch()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    open fun toastMessage(message: String?) {
        if (message == null) return

        val view: ViewGroup? = getRootView()

        if (view == null) {
            Timber.e("Error root view is null... %s", message)
            return
        }

        Timber.i(message)

        val snackbar: Snackbar = Snackbar.make(view, message.toString(), Snackbar.LENGTH_LONG)
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackbar.show()
    }

    fun showTemporaryDialog(message:String, runnable:Runnable, delay: Long = Const.DELAY_INTERVIEW_SUCCESS){
        val dialog = Dialog(this, R.style.FullScreenDialog)
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_full_pink, null)
        dialogView.findViewById<TextView>(R.id.dialogTitle).text = message
        dialog.setContentView(dialogView)
        dialog.show()
        handler.postDelayed({dialog.dismiss()}, delay+20)
        handler.postDelayed(runnable, delay)
    }

}