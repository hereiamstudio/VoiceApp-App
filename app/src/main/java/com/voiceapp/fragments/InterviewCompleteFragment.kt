package com.voiceapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.voiceapp.Const
import com.voiceapp.activities.ConsentActivity
import com.voiceapp.databinding.FragmentCompleteBinding

class InterviewCompleteFragment : BaseFragment() {

    companion object{
        fun newInstance(): InterviewCompleteFragment {
            return InterviewCompleteFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding = FragmentCompleteBinding.inflate(layoutInflater)
        val view = binding.root

        getBaseActivity()!!.handler.postDelayed({
            completed()
        }, Const.DELAY_INTERVIEW_SUCCESS)

        return view
    }

    private fun completed() {
        if (getBaseActivity() != null) {
            (getBaseActivity() as ConsentActivity).onInterviewCompleted()
        }
    }
}