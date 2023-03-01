package com.voiceapp.util

import android.util.Base64
import javax.inject.Inject

class Base64Encoder @Inject constructor() {

    fun encodeToString(input: ByteArray?): String = Base64.encodeToString(input, Base64.NO_WRAP)
}