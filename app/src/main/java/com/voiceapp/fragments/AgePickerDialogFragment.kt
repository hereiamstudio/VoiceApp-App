package com.voiceapp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.voiceapp.R

class AgePickerDialogFragment : DialogFragment() {

    companion object {

        private const val ARG_AGE = "age"

        private const val DEFAULT_AGE = 18
        private const val MINIMUM_AGE = 0
        private const val MAXIMUM_AGE = 130

        fun newInstance() = AgePickerDialogFragment()

        fun newInstance(age: Int) = AgePickerDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_AGE, age)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_number_picker, null, false)
        val numberPicker: NumberPicker = dialogView.findViewById(R.id.numberPicker)
        numberPicker.apply {
            minValue = MINIMUM_AGE
            maxValue = MAXIMUM_AGE
            value = getAgeArgument()
            wrapSelectorWheel = false
        }

        return AlertDialog.Builder(context, R.style.LightDialog)
            .setTitle(R.string.consent_respondent_info_age_question)
            .setView(dialogView)
            .setPositiveButton(R.string.done) { _, _ ->
                (targetFragment as? Callbacks)?.onAgeChosen(numberPicker.value)
            }
            .create()
    }

    private fun getAgeArgument() = arguments?.let {
        val age = it.getInt(ARG_AGE, DEFAULT_AGE)

        return if (age >= MINIMUM_AGE) {
            age
        } else {
            DEFAULT_AGE
        }
    } ?: DEFAULT_AGE

    interface Callbacks {

        fun onAgeChosen(age: Int)
    }
}