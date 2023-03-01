package com.voiceapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import androidx.appcompat.widget.AppCompatImageView

class CheckableImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.style.Widget_ImageButton)
    : AppCompatImageView(context, attrs, defStyleAttr), Checkable {

    companion object {

        private val STATE_CHECKED = intArrayOf(android.R.attr.state_checked)
    }

    private var checkedState = false

    init {
        setOnClickListener {
            toggle()
        }
    }

    override fun setChecked(checked: Boolean) {
        this.checkedState = checked
        refreshDrawableState()
    }

    override fun isChecked() = checkedState

    override fun toggle() {
        isChecked = !isChecked
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)

        if (isChecked) {
            mergeDrawableStates(drawableState, STATE_CHECKED)
        }

        return drawableState
    }
}