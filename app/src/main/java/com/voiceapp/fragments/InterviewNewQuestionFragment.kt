package com.voiceapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.voiceapp.Const
import com.voiceapp.R
import com.voiceapp.activities.ConsentActivity
import com.voiceapp.data.model.Question
import com.voiceapp.databinding.FragmentInterviewNewQuestionBinding
import com.voiceapp.util.LanguagePackHelper
import com.voiceapp.util.VoiceToTextHelper
import java.util.*

class InterviewNewQuestionFragment : BaseFragment() {

    private var order: Int? = null
    private var voiceToTextHelper: VoiceToTextHelper = VoiceToTextHelper()
    private lateinit var binding: FragmentInterviewNewQuestionBinding

    companion object{

        private const val ARG_ORDER = "order"
        private const val ARG_INTERVIEW_LOCALE = "interviewLocale"

        fun newInstance(
            order: Int,
            interviewLocale: String?) = InterviewNewQuestionFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ORDER, order)
                putString(ARG_INTERVIEW_LOCALE, interviewLocale)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = FragmentInterviewNewQuestionBinding.inflate(layoutInflater)
        val view = binding.root

        val b = requireArguments()
        order = b.getInt(ARG_ORDER)

        binding.btnContinue.setOnClickListener { onNextClicked() }
        binding.btnCancel.setOnClickListener { onCancelClicked() }

        fixLayoutParams(binding.root)

        binding.newQuestionTextInput.setEndIconOnClickListener {
            onVoiceInputRequested()
        }

        voiceToTextHelper.addListener(object : VoiceToTextHelper.VoiceResponseListener {
            override fun onResponse(text: String) {
                if (!binding.newQuestionText.text.isNullOrEmpty()) {
                    binding.newQuestionText.append("\n")
                }
                binding.newQuestionText.append(text)
            }
        })

        return view
    }

    private fun onCancelClicked() {
        (activity as ConsentActivity).onQuestionAdded(null)
    }

    private fun onNextClicked(){

        val questionText = binding.newQuestionText.text.toString()

        if (questionText.isEmpty()) {
            binding.newQuestionTextInput.error = getString(R.string.required)
            return
        }

        val question = Question()
        val ord = order!!
        val subquestion = ord % 100
        val questionNo = (ord - subquestion)/100
        question.order = ord
        question.questionNumberText = "$questionNo.$subquestion"
        question.title = questionText
        question.id = UUID.randomUUID().toString()
        question.is_probing_question = true
        question.type = Question.FREE_TEXT
        (activity as ConsentActivity).onQuestionAdded(question)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        voiceToTextHelper.onActivityResult(requestCode, resultCode, data)
    }

    private fun onVoiceInputRequested() {
        (activity as ConsentActivity).pinHelper.nextTimeoutMillis = Const.APP_LOCK_TIMEOUT_VOICE_INPUT
        val language = arguments?.getString(ARG_INTERVIEW_LOCALE)
            ?: LanguagePackHelper.getDefaultLanguage()
        startActivityForResult(voiceToTextHelper.getRecogniserIntent(language, false),
            VoiceToTextHelper.REQUEST_RECOGNIZER)
    }
}