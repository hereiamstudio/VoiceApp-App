package com.voiceapp.fragments

import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.voiceapp.activities.BaseActivity

abstract class BaseFragment : Fragment() {

    fun getBaseActivity(): BaseActivity? {
        return (activity as BaseActivity)
    }
    
    fun toastMessage(message: String? = null) {
        getBaseActivity()!!.toastMessage(message)
    }

    // Fixes layout params when fragment doens't match_parent
    fun fixLayoutParams(view: View){
        // Note this is hardcoded for linearlayout params atm, change if needed for other viewgroups
        val contentViewLayout = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        view.layoutParams = contentViewLayout
    }

}