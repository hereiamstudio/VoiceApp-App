package com.voiceapp.util

import android.text.Html
import android.text.Spanned

object TextHelper{

    fun underlineText(text:String): Spanned {
        return Html.fromHtml("<u>$text</u>", Html.FROM_HTML_MODE_COMPACT)
    }

}