package com.voiceapp.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.voiceapp.R
import com.voiceapp.data.model.Question
import com.voiceapp.databinding.ViewQuestionFreetextLayoutBinding
import com.voiceapp.databinding.ViewQuestionMultiCodeLayoutBinding
import com.voiceapp.databinding.ViewQuestionSingleCodeLayoutBinding
import com.voiceapp.util.VoiceToTextHelper

class QuestionLayoutFactory(val context: Context) {

    interface AnswerSelectedListener {
        fun onAnswerSelected(questionId: Int, answer: String)
        fun onAnswerRemoved(questionId: Int, answer: String)
        fun onVoiceInputRequested()
        fun onVoiceInputFinished(speechAsText: String?)
    }

    fun generateLayout(
        question: Question,
        answerSelectedListener: AnswerSelectedListener?,
        voiceToTextHelper: VoiceToTextHelper? = null): View {
        return when (question.type) {
            Question.MULTI_CODE -> {
                generateMultiCodeLayout(0, null, question.options!!, answerSelectedListener)
            }
            Question.SINGLE_CODE -> {
                generateSingleCodeLayout(0, null, question.options!!, null, answerSelectedListener)
            }
            Question.FREE_TEXT -> {
                generateFreeTextLayout(0, null, answerSelectedListener, voiceToTextHelper)
            }
            else -> {
                throw IllegalArgumentException("Invalid question type " + question.type)
            }
        }
    }

    fun generateSingleCodeLayout(
        questionId: Int,
        questionText: String?,
        answers: List<String>,
        helpText: String?,
        answerSelectedListener: AnswerSelectedListener?): View {
        val binding = ViewQuestionSingleCodeLayoutBinding.inflate(LayoutInflater.from(context))

        if (questionText == null) {
            binding.question1Text.visibility = View.GONE
        } else {
            binding.question1Text.text = questionText
        }

        val shouldShowHelp = helpText?.isNotEmpty() == true
        binding.txtHelp.text = helpText
        binding.layoutTitle.isEnabled = shouldShowHelp
        binding.imgHelp.visibility = if (shouldShowHelp) View.VISIBLE else View.GONE
        binding.expandCollapseIndicator.visibility = if (shouldShowHelp) View.VISIBLE else View.GONE

        binding.layoutTitle.setOnClickListener {
            if (binding.txtHelp.visibility == View.VISIBLE) {
                binding.expandCollapseIndicator.collapse(true)
                binding.txtHelp.animate()
                    .setInterpolator(LinearInterpolator())
                    .setDuration(context.resources.getInteger(
                        android.R.integer.config_mediumAnimTime).toLong())
                    .alpha(0f)
                    .withEndAction {
                        binding.txtHelp.visibility = View.GONE
                    }
            } else {
                binding.expandCollapseIndicator.expand(true)
                binding.txtHelp.animate()
                    .withStartAction {
                        binding.txtHelp.visibility = View.VISIBLE
                    }
                    .setInterpolator(LinearInterpolator())
                    .setDuration(context.resources.getInteger(
                        android.R.integer.config_mediumAnimTime).toLong())
                    .alpha(1f)
            }
        }

        for (answer in answers) {
            val radioButton =
                RadioButton(ContextThemeWrapper(context, R.style.SingleCodeRadioButton), null, 0)
            radioButton.text = answer
            radioButton.isChecked = false
            radioButton.textAlignment = View.TEXT_ALIGNMENT_INHERIT
            val layoutParams =
                RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            layoutParams.topMargin = context.resources.getDimensionPixelSize(R.dimen.small_padding)
            radioButton.layoutParams = layoutParams

            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    answerSelectedListener?.onAnswerSelected(questionId, answer)
                }
            }

            binding.questionGroup.addView(radioButton)
        }

        return binding.root
    }

    private fun generateMultiCodeLayout(
        questionId: Int,
        questionText: String?,
        answers: List<String>,
        answerSelectedListener: AnswerSelectedListener?
    ): View {

        val binding = ViewQuestionMultiCodeLayoutBinding.inflate(LayoutInflater.from(context))

        if (questionText == null) {
            binding.question1Text.visibility = View.GONE
        } else {
            binding.question1Text.text = questionText
        }

        for (answer in answers) {
            val checkbox =
                CheckBox(ContextThemeWrapper(context, R.style.MultiCodeCheckbox), null, 0)
            checkbox.text = answer
            checkbox.isChecked = false
            checkbox.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            val layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            layoutParams.topMargin = context.resources.getDimensionPixelSize(R.dimen.small_padding)
            checkbox.layoutParams = layoutParams

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    answerSelectedListener?.onAnswerSelected(questionId, answer)
                } else {
                    answerSelectedListener?.onAnswerRemoved(questionId, answer)
                }
            }

            binding.questionGroup.addView(checkbox)
        }

        return binding.root
    }

    private fun generateFreeTextLayout(
        questionId: Int,
        questionText: String?,
        answerSelectedListener: AnswerSelectedListener?,
        voiceToTextHelper: VoiceToTextHelper?
    ): View {

        val binding = ViewQuestionFreetextLayoutBinding.inflate(LayoutInflater.from(context))

        if (questionText == null) {
            binding.question1Text.visibility = View.GONE
        } else {
            binding.question1Text.text = questionText
        }

        binding.freeTextAnswer.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                answerSelectedListener?.onAnswerSelected(questionId, p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.freeTextAnswerInput.setEndIconOnClickListener {
            answerSelectedListener?.onVoiceInputRequested()
        }

        voiceToTextHelper?.addListener(object : VoiceToTextHelper.VoiceResponseListener {
            override fun onResponse(text: String) {
                if (!binding.freeTextAnswer.text.isNullOrEmpty()) {
                    binding.freeTextAnswer.append("\n")
                }
                binding.freeTextAnswer.append(text)
                answerSelectedListener?.onVoiceInputFinished(binding.freeTextAnswer.text.toString())
            }
        })

        return binding.root
    }
}