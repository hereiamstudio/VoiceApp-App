package com.voiceapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.voiceapp.Const
import com.voiceapp.activities.ConsentActivity
import com.voiceapp.databinding.FragmentInterviewEnumeratorNotesBinding
import com.voiceapp.util.LanguagePackHelper
import com.voiceapp.util.VoiceToTextHelper

class InterviewEnumeratorNotesFragment : BaseFragment() {

    companion object {

        private const val ARG_CURRENT_NOTES = "currentNotes"
        private const val ARG_INTERVIEW_LOCALE = "interviewLocale"

        fun newInstance(
            currentNotes: String?,
            interviewLocale: String) = InterviewEnumeratorNotesFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_CURRENT_NOTES, currentNotes)
                putString(ARG_INTERVIEW_LOCALE, interviewLocale)
            }
        }
    }

    private var voiceToTextHelper: VoiceToTextHelper = VoiceToTextHelper()

    private lateinit var callbacks: Callbacks

    private var _viewBinding: FragmentInterviewEnumeratorNotesBinding? = null
    private val viewBinding get() = _viewBinding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (ignored: ClassCastException) {
            throw IllegalStateException("${context::class.qualifiedName} does not implement " +
                    Callbacks::class.qualifiedName)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _viewBinding = FragmentInterviewEnumeratorNotesBinding.inflate(inflater, container, false)

        viewBinding.apply {
            fixLayoutParams(root)

            btnCancel.setOnClickListener {
                handleCancelClicked()
            }

            btnAddNote.setOnClickListener {
                handleAddNoteClicked()
            }

            textInputEnumeratorNotes.setEndIconOnClickListener {
                onVoiceInputRequested()
            }

            if (savedInstanceState == null) {
                editEnumeratorNotes.setText(arguments?.getString(ARG_CURRENT_NOTES))
            }

            voiceToTextHelper.addListener(object : VoiceToTextHelper.VoiceResponseListener {
                override fun onResponse(text: String) {
                    if (!editEnumeratorNotes.text.isNullOrEmpty()) {
                        editEnumeratorNotes.append("\n")
                    }

                    editEnumeratorNotes.append(text)
                }
            })
        }

        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        voiceToTextHelper.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleCancelClicked() {
        callbacks.onEnumeratorNotesCancelled()
    }

    private fun handleAddNoteClicked() {
        callbacks.onEnumeratorNotesChanged(viewBinding.editEnumeratorNotes.text.toString())
    }

    private fun onVoiceInputRequested() {
        (activity as ConsentActivity).pinHelper.nextTimeoutMillis =
            Const.APP_LOCK_TIMEOUT_VOICE_INPUT
        val language = arguments?.getString(ARG_INTERVIEW_LOCALE)
            ?: LanguagePackHelper.getDefaultLanguage()
        startActivityForResult(
            voiceToTextHelper.getRecogniserIntent(language, false),
            VoiceToTextHelper.REQUEST_RECOGNIZER)
    }

    interface Callbacks {

        fun onEnumeratorNotesChanged(notes: String?)

        fun onEnumeratorNotesCancelled()
    }
}