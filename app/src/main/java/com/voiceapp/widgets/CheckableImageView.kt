package com.voiceapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import androidx.appcompat.widget.AppCompatImageView

class CheckableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : AppCompatImageView(context, attrs, defStyleAttr), Checkable {

    companion object {

        private val STATE_CHECKED = intArrayOf(android.R.attr.state_checked)
    }

    private var checkedState = false

    override fun setChecked(checked: Boolean) {
        if (this.checkedState != checked) {
            this.checkedState = checked
            refreshDrawableState()
        }
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