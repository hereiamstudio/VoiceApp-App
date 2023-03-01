package com.voiceapp.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.annotation.IntDef
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.voiceapp.Const
import com.voiceapp.R
import com.voiceapp.adapters.TemplateAdapter
import com.voiceapp.data.model.Answer
import com.voiceapp.data.model.Question
import com.voiceapp.data.model.RespondentInfo
import com.voiceapp.data.model.Template
import com.voiceapp.databinding.ActivityConsentBinding
import com.voiceapp.databinding.DialogTemplatesBinding
import com.voiceapp.ui.interview.QuestionSelector
import com.voiceapp.ui.interview.consent.ConsentCheckQuestionFragment
import com.voiceapp.ui.interview.consent.ConsentWrittenFragment
import com.voiceapp.ui.interview.skiplogicinfo.SkipLogicInfoFragment
import com.voiceapp.viewmodels.QuestionViewModel
import com.voiceapp.analytics.*
import com.voiceapp.fragments.*
import com.voiceapp.viewmodels.ResponseViewModel
import com.voiceapp.viewmodels.TemplateViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

/**
 * This is activity contains the consent and interview process
 */
class ConsentActivity : BaseActivity(true), ConsentInterface,
    SkipLogicInfoFragment.Callbacks,
    ConsentPreCheckFragment.Callbacks,
    ConsentRespondentInfoFragment.Callbacks,
    ConsentWrittenFragment.Callbacks,
    ConsentCheckQuestionFragment.Callbacks,
    InterviewQuestionFragment.Callbacks,
    InterviewEnumeratorNotesFragment.Callbacks,
    HasAndroidInjector {

    companion object {
        // Stages of consent / questions
        const val STAGE_NONE: Int = 0
        const val STAGE_SKIP_LOGIC_INFO = 1
        const val STAGE_PRE_CHECK: Int = 2
        const val STAGE_RESPONDENT_INFO: Int = 3
        const val STAGE_WRITTEN_CONSENT_1: Int = 4
        const val STAGE_CONSENT_QUESTION_1: Int = 5
        const val STAGE_WRITTEN_CONSENT_2: Int = 6
        const val STAGE_CONSENT_QUESTION_2: Int = 7
        const val STAGE_RESPONDENT_CONFIRM: Int = 10

        const val STAGE_RESPONDENT_EXTRA_RELATION: Int = 11
        const val STAGE_RESPONDENT_EXTRA_CONFIRM: Int = 12

        const val STAGE_INTERVIEW_NEXT_QUESTION: Int = 101

        const val STAGE_INTERVIEW_OVERVIEW: Int = 1001
        const val STAGE_INTERVIEW_CONFIRM_CONSENT: Int = 1002
        const val STAGE_INTERVIEW_COMPLETE: Int = 1003
        const val STAGE_INTERVIEW_ENUMERATOR_NOTES = 1004

        const val STAGE_INTERVIEW_RETAKE_QUESTION: Int = 2001
        const val STAGE_INTERVIEW_NEW_QUESTION: Int = 2002
        const val STAGE_INTERVIEW_BACK_QUESTION = 2003

        const val ARG_INTERVIEW_ID = "interview_id"
        const val ARG_PROJECT_ID = "project_id"
        const val ARG_INTERVIEW_TITLE = "interview_title"
        const val ARG_INTERVIEW_LOCALE = "interview_locale"
        const val ARG_INTERVIEW_PRIMARY_LANGUAGE = "interview_primary_language"
        const val ARG_HAS_CONSENT_STEP_2 = "has_consent_step_2"
        const val ARG_HAS_SKIP_LOGIC = "has_skip_logic"

        private const val DEFAULT_INTERVIEW_LOCALE = Const.DEFAULT_LOCALE
        private const val DEFAULT_INTERVIEW_PRIMARY_LANGUAGE = Const.DEFAULT_LANGUAGE

        @Target(AnnotationTarget.TYPE)
        @IntDef(
            STAGE_NONE,
            STAGE_SKIP_LOGIC_INFO,
            STAGE_PRE_CHECK,
            STAGE_RESPONDENT_INFO,
            STAGE_WRITTEN_CONSENT_1,
            STAGE_CONSENT_QUESTION_1,
            STAGE_WRITTEN_CONSENT_2,
            STAGE_CONSENT_QUESTION_2,
            STAGE_RESPONDENT_CONFIRM,
            STAGE_RESPONDENT_EXTRA_RELATION,
            STAGE_RESPONDENT_EXTRA_CONFIRM,
            STAGE_INTERVIEW_NEXT_QUESTION,
            STAGE_INTERVIEW_OVERVIEW,
            STAGE_INTERVIEW_CONFIRM_CONSENT,
            STAGE_INTERVIEW_COMPLETE,
            STAGE_INTERVIEW_ENUMERATOR_NOTES,
            STAGE_INTERVIEW_RETAKE_QUESTION,
            STAGE_INTERVIEW_NEW_QUESTION,
            STAGE_INTERVIEW_BACK_QUESTION
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class InterviewStage
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var questionSelector: QuestionSelector
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    private val questionViewModel: QuestionViewModel by viewModels { viewModelFactory }
    private val responseViewModel: ResponseViewModel by viewModels { viewModelFactory }
    private val templateViewModel: TemplateViewModel by viewModels { viewModelFactory }

    private var reverseNextAnimation: Boolean = false
    private var retakeQuestionId: String? = null
    private val answers = HashMap<String, Answer>()
    private var currentStage: @InterviewStage Int = 0
    private var questions = mutableListOf<Question>()
    private var templates = listOf<Template>()
    private var startTime = Calendar.getInstance().time
    private var consentFailureCount = 0
    private var enumeratorNotes: String? = null

    private lateinit var respondentInfo: RespondentInfo
    private lateinit var interviewTitle: String
    private lateinit var projectId: String
    private lateinit var interviewId: String
    private lateinit var interviewLocale: String
    private lateinit var interviewPrimaryLanguage: String
    private var hasConsentStep2 = false

    var hasSkipLogic = false
        private set

    private lateinit var binding: ActivityConsentBinding

    override fun getRootView(): ViewGroup {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        interviewTitle = intent.getStringExtra(ARG_INTERVIEW_TITLE)!!
        projectId = intent.getStringExtra(ARG_PROJECT_ID)!!
        interviewId = intent.getStringExtra(ARG_INTERVIEW_ID)!!
        interviewLocale = intent.getStringExtra(ARG_INTERVIEW_LOCALE) ?: DEFAULT_INTERVIEW_LOCALE
        interviewPrimaryLanguage = intent.getStringExtra(ARG_INTERVIEW_PRIMARY_LANGUAGE)
            ?: DEFAULT_INTERVIEW_PRIMARY_LANGUAGE
        hasConsentStep2 = intent.getBooleanExtra(ARG_HAS_CONSENT_STEP_2, false)
        hasSkipLogic = intent.getBooleanExtra(ARG_HAS_SKIP_LOGIC, false)

        binding = ActivityConsentBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.appBar.toolbar)

        if (savedInstanceState == null) {
            if (hasSkipLogic) {
                navigateToStage(STAGE_SKIP_LOGIC_INFO)
            } else {
                navigateToStage(STAGE_PRE_CHECK)
            }
        }

        questionViewModel.getQuestions(projectId, interviewId).observe(this) { questions ->
            onQuestionsLoaded(questions)
        }

        templateViewModel.getTemplates().observe(this) { templates ->
            this.templates = templates.filter { it.primary_language == interviewPrimaryLanguage }
        }
    }

    override fun onSkipLogicInfoComplete() {
        navigateToStage(STAGE_PRE_CHECK)
    }

    override fun onConsentPreCheckComplete() {
        navigateToStage(STAGE_RESPONDENT_INFO)
    }

    override fun onRespondentInfoComplete(info: RespondentInfo) {
        respondentInfo = info
        navigateToStage(STAGE_WRITTEN_CONSENT_1)
    }

    override fun onWrittenConsentComplete(consentNo: Int) {
        if (consentNo == 1) {
            navigateToStage(STAGE_CONSENT_QUESTION_1)
        } else {
            navigateToStage(STAGE_CONSENT_QUESTION_2)
        }
    }

    override fun onConsentQuestionAnswered(questionNo: Int) {
        consentFailureCount = 0

        if (questionNo == 1 && hasConsentStep2) {
            navigateToStage(STAGE_WRITTEN_CONSENT_2)
        } else {
            navigateToStage(STAGE_RESPONDENT_CONFIRM)
        }
    }

    override fun onConsentQuestionFailed() {
        showTemporaryDialog(getString(R.string.consent_question_failed), {
            finish()
        })
    }

    override fun onConsentQuestionBack(questionNo: Int, failureCount: Int) {
        consentFailureCount = failureCount

        if (questionNo == 2) {
            navigateToStage(STAGE_WRITTEN_CONSENT_2, true)
        } else {
            navigateToStage(STAGE_WRITTEN_CONSENT_1, true)
        }
    }

    override fun onQuestionBackRequested() {
        navigateToStage(STAGE_INTERVIEW_BACK_QUESTION)
    }

    override fun androidInjector() = dispatchingAndroidInjector

    private fun onQuestionsLoaded(questions: List<Question>?) {
        Timber.v("Questions loaded: %d", questions?.size)
        var no = 1
        for (question in questions!!.sortedBy { it.order }) {
            question.questionNumberText = "$no"
            no++
        }

        this.questions.clear()
        this.questions.addAll(questions)
    }

    private fun navigateToStage(
        stage: @InterviewStage Int,
        reverseAnimation: Boolean = false) {
        currentStage = stage
        when (stage) {
            STAGE_SKIP_LOGIC_INFO -> {
                showtoolbar(false)
                loadFragment(
                    SkipLogicInfoFragment.newInstance(), "SkipLogicInfo", false,
                    reverseAnimation)
            }
            STAGE_PRE_CHECK -> {
                showtoolbar(false)
                loadFragment(
                    ConsentPreCheckFragment.newInstance(), "ConsentPreCheck", false,
                    reverseAnimation)
            }
            STAGE_RESPONDENT_INFO -> {
                showtoolbar(
                    true,
                    getString(R.string.consent_respondent_info_title)
                )
                loadFragment(
                    ConsentRespondentInfoFragment.newInstance(), "ConsentRespondentInfo",
                        reverseAnimation = reverseAnimation)
            }
            STAGE_WRITTEN_CONSENT_1 -> {
                showtoolbar(
                    true,
                    interviewTitle
                )
                loadFragment(
                    ConsentWrittenFragment.newInstance(
                        projectId,
                        interviewId,
                        1,
                        if (hasConsentStep2) 4 else 2), "ConsentWritten",
                        reverseAnimation = reverseAnimation)
            }
            STAGE_CONSENT_QUESTION_1 -> {
                showtoolbar(
                    true,
                    interviewTitle
                )
                loadFragment(
                    ConsentCheckQuestionFragment.newInstance(
                        projectId,
                        interviewId,
                        1,
                        if (hasConsentStep2) 4 else 2,
                        consentFailureCount),
                    "ConsentQuestion1", reverseAnimation = reverseAnimation
                )
            }
            STAGE_WRITTEN_CONSENT_2 -> {
                showtoolbar(
                    true,
                    interviewTitle
                )
                loadFragment(
                    ConsentWrittenFragment.newInstance(
                        projectId,
                        interviewId,
                        2,
                        if (hasConsentStep2) 4 else 2), "ConsentWritten",
                        reverseAnimation = reverseAnimation)
            }
            STAGE_CONSENT_QUESTION_2 -> {
                showtoolbar(
                    true,
                    interviewTitle
                )
                loadFragment(
                    ConsentCheckQuestionFragment.newInstance(
                        projectId,
                        interviewId,
                        2,
                        if (hasConsentStep2) 4 else 2,
                        consentFailureCount),
                    "ConsentQuestion2", reverseAnimation = reverseAnimation
                )
            }
            STAGE_RESPONDENT_CONFIRM, STAGE_INTERVIEW_CONFIRM_CONSENT -> {
                showtoolbar(
                    true,
                    ""
                )
                loadFragment(
                    ConsentConfirmFragment.newInstance(), "ConsentConfirm",
                        reverseAnimation = reverseAnimation)
            }
            STAGE_RESPONDENT_EXTRA_RELATION -> {
                showtoolbar(
                    true,
                    interviewTitle
                )
                loadFragment(
                    ConsentExtraRelationFragment.newInstance(interviewLocale),
                    "ConsentExtraRelation",
                    reverseAnimation = reverseAnimation)
            }
            STAGE_RESPONDENT_EXTRA_CONFIRM -> {
                showtoolbar(
                    true,
                    ""
                )
                loadFragment(
                    ConsentConfirmFragment.newInstance(true), "ConsentExtraConfirm",
                        reverseAnimation = reverseAnimation)
            }
            STAGE_INTERVIEW_OVERVIEW -> {
                showtoolbar(
                    false
                )
                loadFragment(
                    InterviewOverviewFragment.newInstance(), "InterviewOverview",
                        reverseAnimation = reverseAnimation)
            }
            STAGE_INTERVIEW_COMPLETE -> {
                analyticsManager.onInterviewCompleted(InterviewCompletedEvent(interviewId))
                showtoolbar(false)
                loadFragment(
                    InterviewCompleteFragment.newInstance(), "InterviewComplete",
                        reverseAnimation = reverseAnimation)
            }
            STAGE_INTERVIEW_NEXT_QUESTION -> {
                // We're in the question stage
                val question = getNextQuestion()

                if (question == null) {
                    // We've run out of questions
                    navigateToStage(STAGE_INTERVIEW_OVERVIEW)
                    return
                }

                showtoolbar(true, interviewTitle)
                loadFragment(
                    InterviewQuestionFragment.newInstance(
                        question.id!!,
                        getNoQuestions(),
                        interviewLocale,
                        !question.is_probing_question
                    ), "InterviewQuestion$stage", reverseAnimation = reverseNextAnimation
                )
            }
            STAGE_INTERVIEW_NEW_QUESTION -> {
                showtoolbar(true, interviewTitle)
                loadFragment(
                    InterviewNewQuestionFragment.newInstance(getNextOrderValue(), interviewLocale),
                    "InterviewNewQuestion", reverseAnimation = reverseAnimation
                )
            }
            STAGE_INTERVIEW_ENUMERATOR_NOTES -> {
                showtoolbar(true, getString(R.string.interviewenumeratornotes_title))
                loadFragment(
                    InterviewEnumeratorNotesFragment.newInstance(enumeratorNotes, interviewLocale),
                    "InterviewEnumeratorNotes", reverseAnimation = reverseAnimation)
            }
            STAGE_INTERVIEW_RETAKE_QUESTION -> {
                showtoolbar(true, interviewTitle)
                loadFragment(
                    InterviewQuestionFragment.newInstance(
                        retakeQuestionId!!,
                        questions.size,
                        interviewLocale,
                        false
                    ), "InterviewQuestion$stage", reverseAnimation = true
                )
            }
            STAGE_INTERVIEW_BACK_QUESTION -> {
                getPreviousQuestion()?.let {
                    answers.remove(it.id)
                    showtoolbar(true, interviewTitle)
                    loadFragment(
                        InterviewQuestionFragment.newInstance(
                            it.id!!,
                            getNoQuestions(),
                            interviewLocale,
                            !it.is_probing_question
                        ), "InterviewQuestion$stage", reverseAnimation = true
                    )
                }
            }
            else -> {
                throw IllegalArgumentException("Invalid stage $stage")
            }
        }
    }

    private fun getPreviousQuestion() = questions.lastOrNull {
        answers.containsKey(it.id)
    }

    private fun getNextQuestion(): Question? {
        val questions = questions.sortedBy { it.order }

        return questionSelector.getNextQuestion(questions, answers)
    }

    private fun getNoQuestions(): Int {
        return questions.count { !it.is_probing_question }
    }

    private fun getNextOrderValue(): Int {
        var lastOrderValue = 0
        for (answer in answers.values.sortedBy { it.order }) {
            lastOrderValue = answer.order
        }
        lastOrderValue++
        return lastOrderValue
    }

    private fun loadFragment(
        fragment: Fragment,
        tag: String,
        animate: Boolean = true,
        reverseAnimation: Boolean = false
    ) {
        val transaction = supportFragmentManager
            .beginTransaction()

        if (animate) {
            if (reverseAnimation) {
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            } else {
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        reverseNextAnimation = false

        transaction.replace(R.id.contentHolder, fragment, tag).commit()
    }

    private fun showtoolbar(show: Boolean = true, title: String? = null) {
        binding.appBar.toolbar.visibility = if (show) View.VISIBLE else View.GONE
        binding.appBar.toolbar.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_consent, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menu_close -> {
                exitConfirmDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (currentStage == STAGE_INTERVIEW_COMPLETE) return
        exitConfirmDialog()
    }

    fun exitConfirmDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.LightDialog)
        builder.setTitle(R.string.confirmexitinterview_title)
            .setMessage(R.string.confirmexitinterview_blurb)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                trackInterviewTermination()
                dialog.dismiss()
                finish()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun onAddQuestion(currentAnswerHasProgress:Boolean) {
        if (currentAnswerHasProgress){
            // Show warning dialog that current question progress will be lost
            val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.LightDialog)
            builder.setTitle(R.string.addquestionwarningdialog_title)
                .setMessage(R.string.addquestionwarningdialog_blurb)
                .setPositiveButton(R.string.cont) { dialog, _ ->
                    onAddQuestion(false)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }.show()
            return
        }

        val d: AlertDialog.Builder = AlertDialog.Builder(this, R.style.LightDialog)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_add_question, null)
        d.setTitle(R.string.addquestiondialog_title)
        d.setMessage(R.string.addquestiondialog_blurb)
        d.setView(dialogView)

        val alertDialog: AlertDialog = d.create()

        dialogView.findViewById<View>(R.id.btnDialogAddExisting)
            .setOnClickListener { dialogTemplateQuestion(); alertDialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnDialogCreateNew).setOnClickListener {
            navigateToStage(STAGE_INTERVIEW_NEW_QUESTION)
            alertDialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }

    private fun dialogTemplateQuestion() {
        val mBottomSheetDialog = BottomSheetDialog(this)

        val bindingTemplates = DialogTemplatesBinding.inflate(layoutInflater)
        bindingTemplates.titleRow.setOnClickListener { mBottomSheetDialog.dismiss() }

        val layoutManager = LinearLayoutManager(this)

        bindingTemplates.templateList.layoutManager = layoutManager
        val templateAdapter = TemplateAdapter(object :
            TemplateAdapter.TemplateListener {
            override fun onTemplateClicked(template: Template) {

                val question = Question()

                val ord = getNextOrderValue()
                val subquestion = ord % 100
                val questionNo = (ord - subquestion) / 100

                question.order = ord
                question.questionNumberText = "$questionNo.$subquestion"
                question.title = template.question_title
                question.id = UUID.randomUUID().toString()
                question.is_probing_question = true
                question.type = Question.FREE_TEXT

                bindingTemplates.btnAddQuestion.isEnabled = true

                bindingTemplates.btnAddQuestion.setOnClickListener {
                    onQuestionAdded(question)
                    mBottomSheetDialog.dismiss()
                }
            }
        })

        val dividerItemDecoration = DividerItemDecoration(
            bindingTemplates.templateList.context,
            layoutManager.orientation
        )

        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider)!!)

        bindingTemplates.templateList.addItemDecoration(dividerItemDecoration)

        templateAdapter.setData(templates)
        bindingTemplates.templateList.adapter = templateAdapter

        mBottomSheetDialog.setContentView(bindingTemplates.root)
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        mBottomSheetDialog.show()
    }

    override fun onConsentConfirmed(confirmed: Boolean, extra: Boolean) {
        if (confirmed) {

            if (currentStage == STAGE_INTERVIEW_CONFIRM_CONSENT){
                // Upload data
                responseViewModel.uploadResponse(
                    startTime,
                    projectId,
                    interviewId,
                    respondentInfo,
                    questions,
                    answers,
                    enumeratorNotes)

                navigateToStage(STAGE_INTERVIEW_COMPLETE)
                return
            }

            if (extra) {
                showTemporaryDialog(getString(R.string.consent_additional_complete), {
                    analyticsManager.onInterviewStarted(InterviewStartedEvent(interviewId))
                    navigateToStage(STAGE_INTERVIEW_NEXT_QUESTION)
                })
            } else {
                showTemporaryDialog(getString(R.string.consent_complete), {
                    if (respondentInfo.extraConsent!!) {
                        navigateToStage(STAGE_RESPONDENT_EXTRA_RELATION)
                    } else {
                        analyticsManager.onInterviewStarted(InterviewStartedEvent(interviewId))
                        navigateToStage(STAGE_INTERVIEW_NEXT_QUESTION)
                    }
                })
            }
        } else {
            exitConfirmDialog()
        }
    }

    override fun onConsentExtraRelation(relation: String) {
        respondentInfo.consent_relationship = relation
        navigateToStage(STAGE_RESPONDENT_EXTRA_CONFIRM)
    }

    fun getAnswers(): HashMap<String, Answer> {
        return answers
    }

    fun getQuestions(): List<Question> {
        return questions
    }

    fun getQuestion(id: String): Question? {
        for (question in questions) {
            if (id == question.id) return question
        }
        return null
    }


    override fun onQuestionAnswered(answer: Answer) {
        answers[answer.questionId!!] = answer

        answer.questionId?.let {
            AnswerSubmitEvent(
                interviewId,
                it,
                answer.usedMicrophone,
                answer.answerOptions.ifEmpty { null }?.get(0) == answer.transcribedAnswer)
                .let { event ->
                    analyticsManager.onQuestionAnswerSubmitted(event)
                }
        }

        if (currentStage == STAGE_INTERVIEW_RETAKE_QUESTION) {
            navigateToStage(STAGE_INTERVIEW_OVERVIEW)
        } else {
            navigateToStage(STAGE_INTERVIEW_NEXT_QUESTION)
        }
    }

    override fun onQuestionAdded(question: Question?) {
        if (question == null) {
            reverseNextAnimation = true
        } else {
            questions.add(question)
        }

        navigateToStage(STAGE_INTERVIEW_NEXT_QUESTION)
    }

    override fun onQuestionRetake(questionId: String) {
        retakeQuestionId = questionId
        navigateToStage(STAGE_INTERVIEW_RETAKE_QUESTION)
    }

    fun onAddEnumeratorNotes() {
        navigateToStage(STAGE_INTERVIEW_ENUMERATOR_NOTES)
    }

    override fun onEnumeratorNotesChanged(notes: String?) {
        enumeratorNotes = notes
        navigateToStage(STAGE_INTERVIEW_OVERVIEW, true)
    }

    override fun onEnumeratorNotesCancelled() {
        navigateToStage(STAGE_INTERVIEW_OVERVIEW, true)
    }

    override fun onInterviewOverviewConfirmed() {
        navigateToStage(STAGE_INTERVIEW_CONFIRM_CONSENT)
    }

    override fun onInterviewCompleted() {
        finish()
    }

    private fun trackInterviewTermination() {
        when (currentStage) {
            STAGE_RESPONDENT_CONFIRM ->
                InterviewTerminationPoint.CONSENT_NOT_GRANTED_START_RESPONDENT
            STAGE_RESPONDENT_EXTRA_CONFIRM ->
                InterviewTerminationPoint.CONSENT_NOT_GRANTED_START_ADDITIONAL_CONSENT
            STAGE_INTERVIEW_CONFIRM_CONSENT ->
                InterviewTerminationPoint.CONSENT_NOT_GRANTED_END
            STAGE_INTERVIEW_OVERVIEW ->
                InterviewTerminationPoint.DURING_INTERVIEW
            else -> InterviewTerminationPoint.END_INTERVIEW
        }.let {
            analyticsManager.onInterviewTerminated(
                InterviewTerminatedEvent(interviewId, it)
            )
        }
    }
}