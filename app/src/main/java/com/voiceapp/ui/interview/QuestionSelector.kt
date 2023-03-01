package com.voiceapp.ui.interview

import com.voiceapp.data.model.Answer
import com.voiceapp.data.model.Question
import com.voiceapp.data.model.SkipCriteria
import javax.inject.Inject

class QuestionSelector @Inject constructor() {

    fun getNextQuestion(
        questions: List<Question>,
        answers: Map<String, Answer>): Question? {
        var skipTo: String? = null

        for (question in questions) {
            if (skipTo == null || question.id == skipTo) {
                val answer = answers[question.id]

                if (answer != null) {
                    skipTo = getSkippedQuestionId(question, answer)
                } else {
                    return question
                }
            }
        }

        return null
    }

    private fun getSkippedQuestionId(question: Question, answer: Answer): String? {
        return question.skip_logic?.firstOrNull {
            doesAnswerSatisfyCriteria(answer, it)
        }?.questionId
    }

    private fun doesAnswerSatisfyCriteria(answer: Answer, criteria: SkipCriteria): Boolean {
        if (criteria.action != "skip_question") {
            return false
        }

        return when (criteria.type) {
            "exactly" -> doesAnswerExactlyMatchCriteriaValues(answer, criteria)
            "all" -> doesAnswerContainAllCriteriaValues(answer, criteria)
            "any" -> doesAnswerContainAnyCriteriaValues(answer, criteria)
            "none" -> !doesAnswerContainAnyCriteriaValues(answer, criteria)
            else -> false
        }
    }

    private fun doesAnswerExactlyMatchCriteriaValues(
        answer: Answer,
        criteria: SkipCriteria
    ) =
        answer.answerOptions == criteria.values

    private fun doesAnswerContainAllCriteriaValues(
        answer: Answer,
        criteria: SkipCriteria
    ) =
        answer.answerOptions.containsAll(criteria.values ?: emptyList())

    private fun doesAnswerContainAnyCriteriaValues(
        answer: Answer,
        criteria: SkipCriteria
    ): Boolean {
        val answerOptions = answer.answerOptions.toSet()

        return criteria.values?.firstOrNull {
            answerOptions.contains(it)
        } != null
    }
}