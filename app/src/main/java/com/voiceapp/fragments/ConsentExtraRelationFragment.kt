package com.voiceapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.voiceapp.Const
import com.voiceapp.R
import com.voiceapp.activities.ConsentActivity
import com.voiceapp.databinding.FragmentConsentExtraRelationBinding
import com.voiceapp.util.LanguagePackHelper
import com.voiceapp.util.VoiceToTextHelper

class ConsentExtraRelationFragment : BaseFragment() {

    private lateinit var binding: FragmentConsentExtraRelationBinding
    private var voiceToTextHelper: VoiceToTextHelper = VoiceToTextHelper()

    companion object{

        private const val ARG_INTERVIEW_LOCALE = "interviewLocale"

        fun newInstance(
            interviewLocale: String?) = ConsentExtraRelationFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_INTERVIEW_LOCALE, interviewLocale)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentConsentExtraRelationBinding.inflate(layoutInflater)
        val view = binding.root

        binding.btnContinue.setOnClickListener { onNextClicked() }
        binding.btnCancel.visibility = View.GONE

        binding.relationTextlayout.setEndIconOnClickListener {
            onVoiceInputRequested()
        }

        voiceToTextHelper.addListener(object : VoiceToTextHelper.VoiceResponseListener {
            override fun onResponse(text: String) {
                if (!binding.relationText.text.isNullOrEmpty()) {
                    binding.relationText.append("\n")
                }
                binding.relationText.append(text)
            }
        })

        fixLayoutParams(binding.root)

        return view
    }

    private fun onNextClicked(){
        val relation = binding.relationText.text.toString()

        if (relation.isEmpty()){
            binding.relationTextlayout.error = getString(R.string.required)
            return
        }

        (activity as ConsentActivity).onConsentExtraRelation(relation)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        voiceToTextHelper.onActivityResult(requestCode, resultCode, data)
    }

    private fun onVoiceInputRequested() {
        (activity as ConsentActivity).pinHelper.nextTimeoutMillis = Const.APP_LOCK_TIMEOUT_VOICE_INPUT
        val language = arguments?.getString(ARG_INTERVIEW_LOCALE)
            ?: LanguagePackHelper.getDefaultLanguage()
        startActivityForResult(voiceToTextHelper.getRecogniserIntent(language, false), VoiceToTextHelper.REQUEST_RECOGNIZER)
    }
}