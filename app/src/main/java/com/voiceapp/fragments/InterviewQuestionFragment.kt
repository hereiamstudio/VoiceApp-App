package com.voiceapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.voiceapp.Const
import com.voiceapp.R
import com.voiceapp.activities.ConsentActivity
import com.voiceapp.data.model.Answer
import com.voiceapp.data.model.Question
import com.voiceapp.databinding.FragmentInterviewQuestionBinding
import com.voiceapp.util.LanguagePackHelper
import com.voiceapp.util.VoiceToTextHelper
import com.voiceapp.views.QuestionLayoutFactory
import kotlin.math.max

class InterviewQuestionFragment : BaseFragment(), QuestionLayoutFactory.AnswerSelectedListener,
    ConfirmGoBackDialogFragment.Callbacks {

    private lateinit var callbacks: Callbacks

    private var question: Question? = null
    private var answerStarted: Boolean = false
    private var allowAdd: Boolean = true
    private var voiceToTextHelper: VoiceToTextHelper = VoiceToTextHelper()
    private var totalQuestions: Int = 0
    private lateinit var questionId: String
    private val currentAnswer: Answer = Answer()

    private val viewBinding get() = _viewBinding!!
    private var _viewBinding: FragmentInterviewQuestionBinding? = null

    companion object {

        const val ARG_QUESTION_ID = "questionid"
        const val ARG_ALLOW_ADD = "allow_add"
        const val ARG_TOTAL_QUESTIONS = "total_questions"
        private const val ARG_INTERVIEW_LOCALE = "interviewLocale"

        private const val DIALOG_CONFIRM_GO_BACK = "dialogConfirmGoBack"

        fun newInstance(
            questionId: String,
            totalQuestions: Int,
            interviewLocale: String,
            allowAdd: Boolean = true) =
            InterviewQuestionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUESTION_ID, questionId)
                    putBoolean(ARG_ALLOW_ADD, allowAdd)
                    putInt(ARG_TOTAL_QUESTIONS, totalQuestions)
                    putString(ARG_INTERVIEW_LOCALE, interviewLocale)
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (ignored: ClassCastException) {
            throw IllegalStateException("${context::class.qualifiedName} does not implement " +
                    "${Callbacks::class.qualifiedName}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().apply {
            questionId = getString(ARG_QUESTION_ID)!!
            totalQuestions = getInt(ARG_TOTAL_QUESTIONS)
            allowAdd = getBoolean(ARG_ALLOW_ADD, true)
        }

        currentAnswer.questionId = questionId
        question = (activity as ConsentActivity).getQuestion(questionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _viewBinding = FragmentInterviewQuestionBinding.inflate(layoutInflater).apply {
            fixLayoutParams(root)
        }

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onQuestionLoaded(question)

        viewBinding.btnContinue.setOnClickListener { onNextClicked() }
        viewBinding.btnAdd.setOnClickListener { onAddQuestionClicked() }
        viewBinding.btnAdd.visibility = if (allowAdd) View.VISIBLE else View.GONE

        viewBinding.btnBack.visibility = if (question?.order != 100) View.VISIBLE else View.GONE
        viewBinding.btnBack.setOnClickListener {
            showConfirmGoBackDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    private fun onAddQuestionClicked() {
        (activity as ConsentActivity).onAddQuestion(answerStarted)
    }

    private fun onQuestionLoaded(question: Question?) {
        if (question == null) {
            throw NullPointerException("Question is null")
        }

        populateProgressIndicator(question)
                
        currentAnswer.order = question.order

        viewBinding.titleText.text = question.title

        question.description?.ifEmpty { null }?.let {
            viewBinding.txtSubtitle.text = it
            viewBinding.txtSubtitle.visibility = View.VISIBLE
        } ?: run {
            viewBinding.txtSubtitle.visibility = View.GONE
        }

        val qlf = QuestionLayoutFactory(requireContext())
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        params.weight = 1.0f

        viewBinding.questionHolder.addView(
            qlf.generateLayout( question, this, voiceToTextHelper), params
        )

        when (question.type) {
            Question.MULTI_CODE, Question.SINGLE_CODE -> {
                viewBinding.questionHolder.setBackgroundResource(R.color.white)
                viewBinding.layoutActionButtonHolder.visibility = View.GONE
            }
            Question.FREE_TEXT -> {
                viewBinding.layoutActionButtonHolder.visibility = View.VISIBLE
            }
        }
    }

    private fun populateProgressIndicator(question: Question?) {
        val progress = if (totalQuestions > 0) {
            question?.let {
                // This is ugly and needs fixed, but the model gives us the question number as a
                // string.
                val questionNumber = it.questionNumberText?.toFloatOrNull() ?: 0f
                (max(questionNumber - 1, 0f) / totalQuestions.toFloat()) * 100f
            } ?: 0
        } else {
            0
        }

        viewBinding.progressIndicator.setProgressCompat(progress.toInt(), false)
    }

    private fun onNextClicked() {
        if (!currentAnswer.hasAnswer()) {
            // Are you sure you want to skip?
            skipConfirmDialog()
        } else {
            currentAnswer.flagged = viewBinding.btnFlag.isChecked
            currentAnswer.starred = viewBinding.btnStar.isChecked
            (activity as ConsentActivity).onQuestionAnswered(currentAnswer)
        }
    }

    override fun onAnswerSelected(questionId: Int, answer: String) {
        when (question!!.type) {
            Question.FREE_TEXT -> {
                if (answer.isEmpty()) {
                    answerStarted = false
                    currentAnswer.removeAllAnswers()
                } else {
                    answerStarted = true
                    currentAnswer.removeAllAnswers()
                    currentAnswer.addAnswer(answer)
                }
            }
            Question.SINGLE_CODE -> {
                answerStarted = true
                currentAnswer.removeAllAnswers()
                currentAnswer.addAnswer(answer)
            }
            Question.MULTI_CODE -> {
                answerStarted = true
                currentAnswer.addAnswer(answer)
            }
        }
    }

    override fun onAnswerRemoved(questionId: Int, answer: String) {
        currentAnswer.removeAnswer(answer)
    }

    override fun onVoiceInputFinished(speechAsText: String?) {
        currentAnswer.transcribedAnswer = speechAsText
    }

    private fun skipConfirmDialog() {
        if (context == null) return

        AlertDialog.Builder(requireContext(), R.style.LightDialog)
            .setTitle(R.string.confirmskipquestion_title)
            .setMessage(R.string.confirmskipquestion_blurb)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                currentAnswer.skipped = true
                currentAnswer.flagged = false
                currentAnswer.starred = false
                currentAnswer.removeAllAnswers()
                (activity as ConsentActivity).onQuestionAnswered(currentAnswer)
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        voiceToTextHelper.onActivityResult(requestCode, resultCode, data)
    }

    override fun onVoiceInputRequested() {
        currentAnswer.usedMicrophone = true
        (activity as ConsentActivity).pinHelper.nextTimeoutMillis = Const.APP_LOCK_TIMEOUT_VOICE_INPUT

        val language = arguments?.getString(ARG_INTERVIEW_LOCALE)
            ?: LanguagePackHelper.getDefaultLanguage()
        startActivityForResult(voiceToTextHelper.getRecogniserIntent(language, false),
            VoiceToTextHelper.REQUEST_RECOGNIZER)
    }

    override fun onConfirmGoBack() {
        callbacks.onQuestionBackRequested()
    }

    private fun showConfirmGoBackDialog() {
        ConfirmGoBackDialogFragment().also {
            it.setTargetFragment(this, 0)
            it.show(parentFragmentManager, DIALOG_CONFIRM_GO_BACK)
        }
    }

    interface Callbacks {

        fun onQuestionBackRequested()
    }
}