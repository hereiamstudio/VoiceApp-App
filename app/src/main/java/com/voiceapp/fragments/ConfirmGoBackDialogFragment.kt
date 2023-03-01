package com.voiceapp.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.voiceapp.R

class ConfirmGoBackDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_confirm_back, null)

        view.findViewById<Button>(R.id.btnGoBack).setOnClickListener {
            (targetFragment as? Callbacks)?.onConfirmGoBack()
            dismiss()
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(context, R.style.LightDialog)
            .setTitle(R.string.confirmgobackdialog_title)
            .setMessage(R.string.confirmgobackdialog_message)
            .setView(view)
            .create()
    }

    interface Callbacks {

        fun onConfirmGoBack()
    }
}