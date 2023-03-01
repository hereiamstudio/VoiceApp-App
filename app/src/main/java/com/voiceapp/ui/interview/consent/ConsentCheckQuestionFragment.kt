package com.voiceapp.ui.interview.consent

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.voiceapp.R
import com.voiceapp.databinding.FragmentConsentQuestionBinding
import com.voiceapp.fragments.BaseFragment
import com.voiceapp.views.QuestionLayoutFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ConsentCheckQuestionFragment : BaseFragment(), QuestionLayoutFactory.AnswerSelectedListener {

    companion object {

        private const val ARG_PROJECT_ID = "projectId"
        private const val ARG_INTERVIEW_ID = "interviewId"
        private const val ARG_CONSENT_NO = "consentNo"
        private const val ARG_STAGE_COUNT = "stageCount"
        private const val ARG_FAILURE_COUNT = "failureCount"

        fun newInstance(
            projectId: String,
            interviewId: String,
            consentStage: Int,
            stageCount: Int,
            failureCount: Int) = ConsentCheckQuestionFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PROJECT_ID, projectId)
                putString(ARG_INTERVIEW_ID, interviewId)
                putInt(ARG_CONSENT_NO, consentStage)
                putInt(ARG_STAGE_COUNT, stageCount)
                putInt(ARG_FAILURE_COUNT, failureCount)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ConsentCheckQuestionFragmentViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var callbacks: Callbacks
    private lateinit var binding: FragmentConsentQuestionBinding
    private lateinit var questionLayoutFactory: QuestionLayoutFactory

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
            viewModel.setFailureCount(it.getInt(ARG_FAILURE_COUNT, 0))
        } ?: run {
            viewModel.setInterviewId(null)
        }

        questionLayoutFactory = QuestionLayoutFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?) =
        FragmentConsentQuestionBinding.inflate(layoutInflater).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {
            viewModel.onNextClicked()
        }

        binding.btnBack.setOnClickListener {
            viewModel.onBackClicked()
        }

        fixLayoutParams(binding.root)

        val viewLifecycle = viewLifecycleOwner
        viewModel.questionsLiveData.observe(viewLifecycle, Observer(this::populateQuestion))
        viewModel.stageLiveData.observe(viewLifecycle, Observer(this::populateStage))
        viewModel.failureCountLiveData.observe(viewLifecycle, Observer(this::populateFailureCount))
        viewModel.backLiveData.observe(viewLifecycle, Observer(this::handleBack))
        viewModel.proceedLiveData.observe(viewLifecycle,
            Observer(callbacks::onConsentQuestionAnswered))
        viewModel.failedLiveData.observe(viewLifecycle) {
            callbacks.onConsentQuestionFailed()
        }
        viewModel.unansweredErrorLiveData.observe(viewLifecycle) {
            showUnansweredError()
        }
        viewModel.incorrectErrorLiveData.observe(viewLifecycle) {
            showIncorrectError()
        }
        viewModel.uncheckAllLiveData.observe(viewLifecycle) {
            recursiveUncheck(binding.questionHolder)
        }
    }

    override fun onAnswerSelected(questionId: Int, answer: String) {
        viewModel.setCurrentAnswer(answer)
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

    private fun populateQuestion(question: ConsentQuestion?) {
        question?.let {
            binding.titleText.text = it.questionText

            binding.questionHolder.addView(
                questionLayoutFactory.generateSingleCodeLayout(
                    1,
                    null,
                    it.answers,
                    null,
                    this)
            )
        }
    }

    private fun populateStage(stage: Int) {
        val stageCount = arguments?.getInt(ARG_STAGE_COUNT, 0) ?: 0
        binding.txtStepNumber.text = getString(R.string.consent_step, stage, stageCount)
    }

    private fun populateFailureCount(count: Int) {
        when (count) {
            1 -> {
                binding.attemptIcon1.setImageResource(R.drawable.ic_circle_error)
                binding.attemptIcon2.setImageResource(R.drawable.ic_icon_circle_filled)
            }
            2 -> {
                binding.attemptIcon1.setImageResource(R.drawable.ic_circle_error)
                binding.attemptIcon2.setImageResource(R.drawable.ic_circle_error)
            }
        }
    }

    private fun handleBack(params: ConsentBackParams) {
        callbacks.onConsentQuestionBack(params.questionNumber, params.failureCount)
    }

    private fun showUnansweredError() {
        toastMessage(getString(R.string.consentcheckquestion_error_please_select))
    }

    private fun showIncorrectError() {
        toastMessage(getString(R.string.consentcheckquestion_error_incorrect))
    }

    private fun recursiveUncheck(view: View) {
        when (view){
            is ViewGroup -> {
                for (child in view.children){
                    recursiveUncheck(child)
                }
            }
            is CompoundButton -> {
                view.isChecked = false
            }
        }
    }

    interface Callbacks {

        fun onConsentQuestionAnswered(questionNo: Int)

        fun onConsentQuestionFailed()

        fun onConsentQuestionBack(questionNo: Int, failureCount: Int)
    }
}