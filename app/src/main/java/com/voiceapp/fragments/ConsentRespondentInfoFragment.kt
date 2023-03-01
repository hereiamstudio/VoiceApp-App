package com.voiceapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import com.voiceapp.R
import com.voiceapp.data.model.RespondentInfo
import com.voiceapp.databinding.FragmentConsentRespondentInfoBinding
import com.voiceapp.views.QuestionLayoutFactory
import com.voiceapp.views.QuestionLayoutFactory.AnswerSelectedListener

class ConsentRespondentInfoFragment : BaseFragment(), AnswerSelectedListener,
    AgePickerDialogFragment.Callbacks {

    companion object {

        fun newInstance(): ConsentRespondentInfoFragment {
            return ConsentRespondentInfoFragment()
        }

        const val QUESTION_EXTRA_CONSENT = 1
        const val QUESTION_BENEFICIARY = 2
        const val QUESTION_GENDER = 3

        private const val DIALOG_AGE_PICKER = "dialogAgePicker"

        private const val MANDATORY_CONSENT_CUTOFF_AGE = 18
    }

    private lateinit var callbacks: Callbacks
    private lateinit var binding: FragmentConsentRespondentInfoBinding

    private var respondentInfo: RespondentInfo = RespondentInfo()

    private lateinit var consentRadioGroup: RadioGroup

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (ignored: ClassCastException) {
            throw IllegalStateException("${context::class.qualifiedName} must implement " +
                    "${Callbacks::class.qualifiedName}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View =
        FragmentConsentRespondentInfoBinding.inflate(layoutInflater).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {
            onContinueClicked()
        }

        binding.btnAgeDropdown.setOnClickListener {
            onAgeClicked()
        }

        populateQuestions()
    }

    override fun onAnswerSelected(questionId: Int, answer: String) {
        when (questionId) {
            QUESTION_BENEFICIARY -> {
                respondentInfo.benificiary = answer == getString(R.string.yes)
            }
            QUESTION_EXTRA_CONSENT -> {
                respondentInfo.extraConsent = answer == getString(R.string.yes)
            }
            QUESTION_GENDER -> {
                respondentInfo.gender = answer
            }
        }
    }

    override fun onAnswerRemoved(questionId: Int, answer: String) {
        // Ignored
    }

    override fun onVoiceInputRequested() {
        // Ignored
    }

    override fun onVoiceInputFinished(speechAsText: String?) {
        // Ignored
    }

    override fun onAgeChosen(age: Int) {
        respondentInfo.age = age
        binding.btnAgeDropdown.text =
            getString(R.string.consent_respondent_info_age_question_with_age, respondentInfo.age)
        updateAdditionalConsentStatus()
    }

    private fun populateQuestions() {
        val qlf = QuestionLayoutFactory(requireContext())

        val consentQuestionView = qlf.generateSingleCodeLayout(
            QUESTION_EXTRA_CONSENT,
            getString(R.string.consent_respondent_info_question_additional_consent),
            listOf(getString(R.string.yes), getString(R.string.no)),
            getString(R.string.consent_respondent_info_help_additional_consent),
            this)
        consentRadioGroup = consentQuestionView.findViewById(R.id.questionGroup)

        binding.questionHolder.addView(consentQuestionView)

        binding.questionHolder.addView(
            qlf.generateSingleCodeLayout(
                QUESTION_BENEFICIARY,
                getString(R.string.consent_respondent_info_question_beneficiary),
                listOf(getString(R.string.yes), getString(R.string.no)),
                getString(R.string.consent_respondent_info_help_beneficiary),
                this))

        binding.questionHolder.addView(
            qlf.generateSingleCodeLayout(
                QUESTION_GENDER,
                getString(R.string.consent_respondent_info_question_gender),
                listOf(
                    getString(R.string.gender_male),
                    getString(R.string.gender_female),
                    getString(R.string.gender_other)),
                getString(R.string.consent_respondent_info_help_gender),
                this))

        updateAdditionalConsentStatus()
    }

    private fun onContinueClicked() {
        if (respondentInfo.isComplete()) {
            callbacks.onRespondentInfoComplete(respondentInfo)
        } else {
            toastMessage(getString(R.string.consent_respondent_info_error_incomplete))
        }
    }

    private fun onAgeClicked() {
        val age = respondentInfo.age

        if (age >= 0) {
            AgePickerDialogFragment.newInstance(age)
        } else {
            AgePickerDialogFragment.newInstance()
        }.also {
            it.setTargetFragment(this, 0)
            it.show(parentFragmentManager, DIALOG_AGE_PICKER)
        }
    }

    private fun updateAdditionalConsentStatus() {
        val age = respondentInfo.age

        if (age >= MANDATORY_CONSENT_CUTOFF_AGE || age < 0) {
            consentRadioGroup.forEach {
                it.isEnabled = true
            }
        } else {
            consentRadioGroup.forEachIndexed { index, view ->
                view.isEnabled = false

                if (index == 0) {
                    (view as? RadioButton)?.isChecked = true
                }
            }

            respondentInfo.extraConsent = true
        }
    }

    interface Callbacks {

        fun onRespondentInfoComplete(info: RespondentInfo)
    }
}