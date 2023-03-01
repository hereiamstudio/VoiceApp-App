package com.voiceapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView
import com.voiceapp.R

class ExpandCollapseIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.expandCollapseIndicatorStyle)
    : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {

        private const val STATE_COLLAPSED = 0
        private const val STATE_EXPANDED = 1

        private const val COLLAPSED_DEGREES = 0f
        private const val EXPANDED_DEGREES = 180f
    }

    private val interpolator = LinearInterpolator()
    private val animationDuration = context.resources.getInteger(
        android.R.integer.config_shortAnimTime).toLong()
    private var collapsedContentDescription: CharSequence? = null
    private var expandedContentDescription: CharSequence? = null

    private var currentState = STATE_COLLAPSED

    init {
        attrs?.also {
            val a = context.theme.obtainStyledAttributes(it, R.styleable.ExpandCollapseIndicator,
                defStyleAttr, 0)
            setState(a.getInt(R.styleable.ExpandCollapseIndicator_expandedState, STATE_COLLAPSED),
                false)
            setCollapsedContentDescription(
                a.getText(R.styleable.ExpandCollapseIndicator_collapsedContentDescription))
            setExpandedContentDescription(
                a.getText(R.styleable.ExpandCollapseIndicator_expandedContentDescription))
            a.recycle()
        } ?: run {
            setState(STATE_COLLAPSED, false)
            applyContentDescription()
        }
    }

    fun expand(animated: Boolean) {
        setState(STATE_EXPANDED, animated)
    }

    fun collapse(animated: Boolean) {
        setState(STATE_COLLAPSED, animated)
    }

    private fun setState(state: Int, animated: Boolean) {
        when (state) {
            STATE_COLLAPSED -> {
                if (animated) {
                    performCollapseArrowAnimation()
                } else {
                    rotation = COLLAPSED_DEGREES
                    applyCollapsedState()
                }
            }
            STATE_EXPANDED -> {
                if (animated) {
                    performExpandArrowAnimation()
                } else {
                    rotation = EXPANDED_DEGREES
                    applyExpandedState()
                }
            }
        }
    }

    private fun setCollapsedContentDescription(contentDescription: CharSequence?) {
        collapsedContentDescription = contentDescription
        applyContentDescription()
    }

    private fun setExpandedContentDescription(contentDescription: CharSequence?) {
        expandedContentDescription = contentDescription
        applyContentDescription()
    }

    private fun applyContentDescription() {
        contentDescription = when (currentState) {
            STATE_COLLAPSED -> collapsedContentDescription
            STATE_EXPANDED -> expandedContentDescription
            else -> null
        }
    }

    private fun performExpandArrowAnimation() {
        if (rotation != EXPANDED_DEGREES) {
            animate().setInterpolator(interpolator)
                .setDuration(animationDuration)
                .rotation(EXPANDED_DEGREES)
                .withEndAction {
                    applyExpandedState()
                }
        }
    }

    private fun performCollapseArrowAnimation() {
        if (rotation != COLLAPSED_DEGREES) {
            animate().setInterpolator(interpolator)
                .setDuration(animationDuration)
                .rotation(COLLAPSED_DEGREES)
                .withEndAction {
                    applyCollapsedState()
                }
        }
    }

    private fun applyCollapsedState() {
        currentState = STATE_COLLAPSED
        applyContentDescription()
    }

    private fun applyExpandedState() {
        currentState = STATE_EXPANDED
        applyContentDescription()
    }
}