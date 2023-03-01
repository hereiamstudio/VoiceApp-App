package com.voiceapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.voiceapp.R
import com.voiceapp.activities.ConsentActivity
import com.voiceapp.databinding.FragmentConsentConfirmBinding

class ConsentConfirmFragment : BaseFragment() {

    private var extraConsent: Boolean = false

    companion object{

        const val ARG_EXTRA = "extra_consent"

        fun newInstance(extraConsent:Boolean = false): ConsentConfirmFragment {
            val fragment = ConsentConfirmFragment()
            val args = Bundle()
            args.putBoolean(ARG_EXTRA, extraConsent)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentConsentConfirmBinding.inflate(layoutInflater)
        val view = binding.root

        extraConsent = arguments?.getBoolean(ARG_EXTRA, false) ?: false

        binding.btnConsentNo.setOnClickListener { onNoClicked() }
        binding.btnConsentYes.setOnClickListener { onYesClicked() }

        binding.descText.text = if (extraConsent) {
            getString(R.string.consentconfirm_blurb_extra_consent)
        } else {
            getString(R.string.consentconfirm_blurb)
        }

        return view
    }

    private fun onNoClicked() {
        (activity as ConsentActivity).onConsentConfirmed(false, extraConsent)
    }

    private fun onYesClicked() {
        (activity as ConsentActivity).onConsentConfirmed(true, extraConsent)
    }
}