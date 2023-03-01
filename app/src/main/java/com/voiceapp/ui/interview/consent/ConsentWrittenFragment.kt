package com.voiceapp.ui.interview.consent

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.voiceapp.R
import com.voiceapp.data.model.InterviewConsent
import com.voiceapp.databinding.FragmentConsentWrittenBinding
import com.voiceapp.fragments.BaseFragment
import com.voiceapp.project.Result
import dagger.android.support.AndroidSupportInjection
import java.lang.IllegalStateException
import javax.inject.Inject

class ConsentWrittenFragment : BaseFragment() {

    companion object {

        private const val ARG_PROJECT_ID = "projectId"
        private const val ARG_INTERVIEW_ID = "interviewId"
        private const val ARG_CONSENT_NO = "consentNo"
        private const val ARG_CONSENT_COUNT = "consentCount"

        fun newInstance(
            projectId: String,
            interviewId: String,
            consentNo: Int,
            consentCount: Int) = ConsentWrittenFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PROJECT_ID, projectId)
                putString(ARG_INTERVIEW_ID, interviewId)
                putInt(ARG_CONSENT_NO, consentNo)
                putInt(ARG_CONSENT_COUNT, consentCount)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ConsentWrittenFragmentViewModel by viewModels { viewModelFactory }

    private lateinit var callbacks: Callbacks
    private lateinit var binding: FragmentConsentWrittenBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (ignored: ClassCastException) {
            throw IllegalStateException("${context::class.qualifiedName} must implement " +
                    "${Callbacks::class.qualifiedName}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        arguments?.let {
            val projectId = it.getString(ARG_PROJECT_ID)
            val interviewId = it.getString(ARG_INTERVIEW_ID)
            val consentStage = it.getInt(ARG_CONSENT_NO, -1)
            val compositeId = if (projectId?.isNotEmpty() == true &&
                interviewId?.isNotEmpty() == true) {
                InterviewConsentId(projectId, interviewId, consentStage)
            } else {
                null
            }

            viewModel.setInterviewId(compositeId)
        } ?: run {
            viewModel.setInterviewId(null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?) =
        FragmentConsentWrittenBinding.inflate(layoutInflater).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.interviewIdLiveData.value?.consentStage == 1) {
            1
        } else {
            3
        }.let {
            val consentCount = arguments?.getInt(ARG_CONSENT_COUNT, 0) ?: 0
            binding.txtStepNumber.text = getString(R.string.consent_step, it, consentCount)
        }

        binding.btnAnswerQuestions.setOnClickListener {
            onAnswerQuestionClicked()
        }

        val viewLifecycle = viewLifecycleOwner
        viewModel.interview.observe(viewLifecycle, Observer(this::populateConsent))
    }

    private fun populateConsent(result: Result<InterviewConsent>) {
        when (result) {
            is Result.Success -> {
                populateConsentSuccess(result.success)
            }
            else -> populateConsentWithDefaults()
        }
    }

    private fun populateConsentSuccess(interviewConsent: InterviewConsent) {
        binding.txtDescription.text = interviewConsent.description
        binding.consentText.text = interviewConsent.script
    }

    private fun populateConsentWithDefaults() {
        binding.txtDescription.setText(R.string.consentwritten_subheading)

        if (viewModel.interviewIdLiveData.value?.consentStage == 1) {
            R.string.consentwritten_text_1
        } else {
            R.string.consentwritten_text_2
        }.let {
            binding.consentText.setText(it)
        }
    }

    private fun onAnswerQuestionClicked() {
        viewModel.interviewIdLiveData.value?.consentStage?.let(callbacks::onWrittenConsentComplete)
    }

    interface Callbacks {

        fun onWrittenConsentComplete(consentNo: Int)
    }
}