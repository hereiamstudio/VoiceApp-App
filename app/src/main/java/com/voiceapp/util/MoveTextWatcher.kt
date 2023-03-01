package com.voiceapp.util

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText

class MoveTextWatcher(
    private val prev: EditText?,
    self: EditText,
    private val next: EditText?,
    private val onCompleteListener: OnCompleteListener
) : TextWatcher {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var prevLength = 0

    interface OnCompleteListener {
        fun onComplete()
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

    override fun afterTextChanged(editable: Editable) {
        val text = editable.toString()
        val l = text.length
        if (l == 1 && next != null) {
            next.requestFocus()
        }
        if (l == 1 && next == null) {
            onCompleteListener.onComplete()
        }
        prevLength = editable.toString().length
    }

    init {
        if (prev != null) {
            self.setOnKeyListener { _: View?, keyCode: Int, _: KeyEvent? ->
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //Perform action for backspace
                    handler.postDelayed({ prev.requestFocus() }, 50)
                    return@setOnKeyListener false
                }
                false
            }
        }
    }
}