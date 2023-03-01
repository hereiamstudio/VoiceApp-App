package com.voiceapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.voiceapp.R
import com.voiceapp.activities.ConsentActivity
import com.voiceapp.data.model.Answer
import com.voiceapp.data.model.Question
import com.voiceapp.databinding.FragmentInterviewOverviewBinding
import com.voiceapp.databinding.ViewQuestionOverviewListItemBinding
import com.voiceapp.util.TextHelper
import java.util.*

class InterviewOverviewFragment : BaseFragment() {

    private lateinit var answers: HashMap<String, Answer>
    private var questions: List<Question>? = null
    private var hasSkipLogic = false

    private val viewBinding get() = _viewBinding!!
    private var _viewBinding: FragmentInterviewOverviewBinding? = null

    companion object {

        fun newInstance() = InterviewOverviewFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        answers = (activity as ConsentActivity).getAnswers()
        questions = (activity as ConsentActivity).getQuestions()
        hasSkipLogic = (activity as ConsentActivity).hasSkipLogic
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _viewBinding = FragmentInterviewOverviewBinding.inflate(layoutInflater)
        val view = viewBinding.root

        fixLayoutParams(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.apply {
            btnConfirmAnswers.setOnClickListener { addInformantClicked() }
            btnAdd.setOnClickListener { addClicked() }
            cancelButton.setOnClickListener { cancelClicked() }
            btnComments.setOnClickListener { onCommentsClicked() }

            cancelButton.text =
                TextHelper.underlineText(getString(R.string.interviewoverview_btn_cancel))
        }

        setCountText()

        for (answer in answers.values.sortedBy { it.order }) {
            val question = getQuestionById(answer.questionId!!)!!
            addQuestionOverview(question, answer)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    private fun setCountText() {
        var skipCount = 0

        for (answer in answers.values) {
            if (answer.skipped) {
                skipCount++
            }
        }

        viewBinding.descText.text = resources.getQuantityString(
            R.plurals.interviewoverview_description,
            skipCount,
            answers.size,
            skipCount)
    }

    private fun getQuestionById(questionId: String): Question? {
        for (question in questions!!) {
            if (question.id == questionId) {
                return question
            }
        }

        return null
    }

    private fun addQuestionOverview(question: Question, answer: Answer) {
        val itemBinding = ViewQuestionOverviewListItemBinding
            .inflate(LayoutInflater.from(context)).apply {
                questionText.text = question.title

                flaggedText.visibility = if (answer.flagged) View.VISIBLE else View.GONE
                starredText.visibility = if (answer.starred) View.VISIBLE else View.GONE

                btnRemove.text =
                    TextHelper.underlineText(getString(R.string.interviewoverview_item_btn_remove))
                btnRetake.text =
                    TextHelper.underlineText(getString(R.string.interviewoverview_item_btn_retake))

                if (answer.skipped) {
                    skippedText.visibility = View.VISIBLE
                    btnFlag.visibility = View.GONE
                    btnStar.visibility = View.GONE
                    btnRemove.visibility = View.GONE
                } else {
                    btnRemove.visibility = if (!hasSkipLogic) View.VISIBLE else View.GONE

                    btnRemove.setOnClickListener {
                        removeAnswer(this, answer)
                    }
                }

                if (question.is_probing_question) {
                    probingText.visibility = View.VISIBLE
                }

                btnFlag.setOnClickListener {
                    answer.flagged = !answer.flagged
                    flaggedText.visibility = if (answer.flagged) View.VISIBLE else View.GONE
                }

                btnStar.setOnClickListener {
                    answer.starred = !answer.starred
                    starredText.visibility =
                        if (answer.starred) View.VISIBLE else View.GONE
                }

                if (hasSkipLogic) {
                    btnRetake.visibility = View.GONE
                } else {
                    btnRetake.visibility = View.VISIBLE
                    btnRetake.setOnClickListener { retake(question.id!!) }
                }

                if (question.type == Question.FREE_TEXT) {
                    btnFlag.visibility = View.VISIBLE
                    btnStar.visibility = View.VISIBLE
                } else {
                    btnFlag.visibility = View.GONE
                    btnStar.visibility = View.GONE
                }
            }

        viewBinding.questionOverviewHolder.addView(itemBinding.root)
    }

    private fun removeAnswer(
        itemBinding: ViewQuestionOverviewListItemBinding,
        answer: Answer
    ) {
        AlertDialog.Builder(requireContext(), R.style.LightDialog)
            .setTitle(R.string.confirmremoveanswer_title)
            .setMessage(R.string.confirmremoveanswer_blurb)
            .setPositiveButton(R.string.yes) { _, _ ->
                answer.removeAllAnswers()
                answer.flagged = false
                answer.starred = false
                answer.skipped = true
                itemBinding.skippedText.visibility = View.VISIBLE
                itemBinding.btnFlag.visibility = View.GONE
                itemBinding.btnStar.visibility = View.GONE
                itemBinding.btnRemove.visibility = View.GONE
                itemBinding.flaggedText.visibility = View.GONE
                itemBinding.starredText.visibility = View.GONE
                setCountText()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun cancelClicked() {
        (activity as ConsentActivity).exitConfirmDialog()
    }

    private fun addClicked() {
        (activity as ConsentActivity).onAddQuestion(false)
    }

    private fun onCommentsClicked() {
        (activity as ConsentActivity).onAddEnumeratorNotes()
    }

    private fun retake(questionId:String) {
        (activity as ConsentActivity).onQuestionRetake(questionId)
    }

    private fun addInformantClicked() {
        (activity as ConsentActivity).onInterviewOverviewConfirmed()
    }
}