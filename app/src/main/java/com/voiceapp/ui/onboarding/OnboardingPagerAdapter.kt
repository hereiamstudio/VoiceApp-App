package com.voiceapp.ui.onboarding

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.voiceapp.R

class OnboardingPagerAdapter(
    private val activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = activity.resources.getInteger(R.integer.onboarding_step_count)

    override fun createFragment(position: Int) = OnboardingItemFragment.newInstance(position)
}