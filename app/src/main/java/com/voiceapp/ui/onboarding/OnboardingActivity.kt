package com.voiceapp.ui.onboarding

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import com.voiceapp.R
import com.voiceapp.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var pagerAdapter: OnboardingPagerAdapter

    private lateinit var viewBinding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        pagerAdapter = OnboardingPagerAdapter(this)

        viewBinding.apply {
            viewPager.adapter = pagerAdapter
            viewPager.registerOnPageChangeCallback(pageSelectedListener)

            btnPrevious.setOnClickListener {
                handlePreviousButtonClicked()
            }

            btnNext.setOnClickListener {
                handleNextButtonClicked()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.onboarding_activity_options_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.onboarding_activity_option_item_close -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun handlePreviousButtonClicked() {
        viewBinding.viewPager.apply {
            if (currentItem <= 0) {
                return
            }

            setCurrentItem(currentItem - 1, true)
        }
    }

    private fun handleNextButtonClicked() {
        viewBinding.viewPager.apply {
            val nextIndex = currentItem + 1

            if (nextIndex >= pagerAdapter.itemCount) {
                finish()
            } else {
                setCurrentItem(nextIndex, true)
            }
        }
    }

    private val pageSelectedListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val itemCount = pagerAdapter.itemCount

            viewBinding.apply {
                // If we don't put this in a Handler.post(), the buttons don't lay out correctly on
                // initial layout of the screen. This is probably because the ViewPager gives us the
                // page selected event while Android is still resolving the layout, and we hide the
                // previous button during this time. So we defer the actual changing of the layout
                // until the next time the Handler queue is processed, which in reality is pretty
                // much immediately.
                root.handler.post {
                    btnPrevious.visibility = if (position <= 0) View.GONE else View.VISIBLE

                    val nextIconRes = if (position >= (itemCount - 1)) {
                        R.drawable.ic_check
                    } else {
                        R.drawable.ic_arrow_end
                    }

                    btnNext.icon = ResourcesCompat.getDrawable(resources, nextIconRes, null)
                }
            }
        }
    }
}