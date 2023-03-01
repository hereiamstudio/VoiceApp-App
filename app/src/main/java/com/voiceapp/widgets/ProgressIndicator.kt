package com.voiceapp.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.LinearLayout
import androidx.annotation.IntDef
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.core.view.plusAssign
import androidx.core.view.size
import com.voiceapp.R
import kotlin.math.abs
import kotlin.math.max

class ProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle) {

    companion object {

        @IntDef(
            INDICATOR_MODE_SINGLE,
            INDICATOR_MODE_CURRENT_AND_PREVIOUS
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class IndicatorMode

        const val INDICATOR_MODE_SINGLE = 0
        const val INDICATOR_MODE_CURRENT_AND_PREVIOUS = 1
    }

    init {
        attrs?.let {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ProgressIndicator,
                R.attr.progressIndicatorStyle,
                0)

            indicator = a.getDrawable(R.styleable.ProgressIndicator_indicator)
            space = a.getDimensionPixelSize(R.styleable.ProgressIndicator_space, 0)
            indicatorMode = a.getInteger(R.styleable.ProgressIndicator_indicatorMode,
                INDICATOR_MODE_SINGLE
            )
            indicatorCount = a.getInteger(R.styleable.ProgressIndicator_indicatorCount, 0)
            selectedIndex = a.getInteger(R.styleable.ProgressIndicator_selectedIndex, -1)

            a.recycle()
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var indicator: Drawable? = null
        set(value) {
            if (field !== value) {
                field = value?.mutate()

                forEach {
                    (it as? CheckableImageView)
                        ?.setImageDrawable(createIndicatorInstance())
                }

                requestLayout()
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var space = 0
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    @IndicatorMode
    var indicatorMode = INDICATOR_MODE_SINGLE
        set(value) {
            if (field != value) {
                field = value
                recalculateCheckedItems()
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var indicatorCount = 0
        set(value) {
            val newValue = max(0, value)

            if (field != newValue) {
                field = newValue

                setupIndicators()
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var selectedIndex = -1
        set(value) {
            if (field != value) {
                field = value
                recalculateCheckedItems()
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        forEachIndexed { index, view ->
            (view.layoutParams as LayoutParams).apply {
                val startMargin = if (index > 0) space / 2 else 0
                val endMargin = if (index < (size - 1)) space / 2 else 0

                if (layoutDirection == LAYOUT_DIRECTION_LTR) {
                    leftMargin = startMargin
                    rightMargin = endMargin
                } else {
                    leftMargin = endMargin
                    rightMargin = startMargin
                }
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun setupIndicators() {
        if (indicatorCount == 0) {
            // Do a fast-path check - if there's 0 indicators, then we shouldn't have any child
            // Views.
            removeAllViews()
            return
        }

        val childCount = size
        val diff = indicatorCount - childCount

        if (diff < 0) {
            val absDiff = abs(diff)
            removeViews(childCount - absDiff - 1, absDiff)
        } else if (diff > 0) {
            repeat(diff) {
                this += createImageView()
            }
        }

        recalculateCheckedItems()
    }

    private fun createImageView() = CheckableImageView(context).apply {
        layoutParams = generateDefaultLayoutParams()
        setImageDrawable(createIndicatorInstance())
    }

    private fun createIndicatorInstance() = indicator?.constantState?.newDrawable()

    private fun recalculateCheckedItems() {
        forEachIndexed { index, view ->
            (view as? Checkable)?.isChecked = when (indicatorMode) {
                INDICATOR_MODE_CURRENT_AND_PREVIOUS -> index <= selectedIndex
                else -> index == selectedIndex
            }
        }
    }
}