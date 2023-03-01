package com.voiceapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.voiceapp.R
import com.voiceapp.databinding.FragmentOnboardingItemBinding

class OnboardingItemFragment : Fragment() {

    companion object {

        private const val ARG_POSITION = "position"

        fun newInstance(position: Int) = OnboardingItemFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POSITION, position)
            }
        }
    }

    private val viewBinding get() = _viewBinding!!
    private var _viewBinding: FragmentOnboardingItemBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _viewBinding = FragmentOnboardingItemBinding.inflate(inflater, container, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.progressIndicator.selectedIndex = arguments?.getInt(ARG_POSITION, 0) ?: 0

        populateViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    private fun populateViews() {
        val featureImageDrawableRes: Int
        val titleStringRes: Int
        val blurbStringRes: Int

        when (arguments?.getInt(ARG_POSITION, -1)) {
            0 -> {
                featureImageDrawableRes = R.drawable.onboarding_item_1
                titleStringRes = R.string.onboarding_fragment_item_1_title
                blurbStringRes = R.string.onboarding_fragment_item_1_blurb
            }
            1 -> {
                featureImageDrawableRes = R.drawable.onboarding_item_2_automirror
                titleStringRes = R.string.onboarding_fragment_item_2_title
                blurbStringRes = R.string.onboarding_fragment_item_2_blurb
            }
            2 -> {
                featureImageDrawableRes = R.drawable.onboarding_item_3
                titleStringRes = R.string.onboarding_fragment_item_3_title
                blurbStringRes = R.string.onboarding_fragment_item_3_blurb
            }
            3 -> {
                featureImageDrawableRes = R.drawable.onboarding_item_4
                titleStringRes = R.string.onboarding_fragment_item_4_title
                blurbStringRes = R.string.onboarding_fragment_item_4_blurb
            }
            4 -> {
                featureImageDrawableRes = R.drawable.onboarding_item_5
                titleStringRes = R.string.onboarding_fragment_item_5_title
                blurbStringRes = R.string.onboarding_fragment_item_5_blurb
            }
            else -> return
        }

        viewBinding.apply {
            imgFeature.setImageResource(featureImageDrawableRes)
            txtTitle.setText(titleStringRes)
            txtBlurb.setText(blurbStringRes)
        }
    }
}