package com.voiceapp.ui.project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.voiceapp.Const
import com.voiceapp.activities.InterviewListActivity
import com.voiceapp.R
import com.voiceapp.databinding.ActivityProjectsBinding
import com.voiceapp.livedata.Event
import com.voiceapp.ui.settings.SettingsActivity
import com.voiceapp.ui.whatsnew.WhatsNewActivity
import dagger.android.AndroidInjection
import javax.inject.Inject

class ProjectListActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ProjectListActivityViewModel by viewModels { viewModelFactory }

    private lateinit var viewBinding: ActivityProjectsBinding
    private lateinit var adapter: ProjectListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        viewBinding = ActivityProjectsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.projectAppBarLayout.buttonSettings.setOnClickListener {
            viewModel.onSettingsButtonClicked()
        }
        viewBinding.projectAppBarLayout.buttonSync.setOnClickListener {
            viewModel.onSyncButtonClicked()
        }

        adapter = ProjectListAdapter(this, viewModel::onProjectClicked)
        viewBinding.projectList.setHasFixedSize(true)
        viewBinding.projectList.adapter = adapter

        viewModel.projectsLiveData.observe(this, adapter::submitList)
        viewModel.projectCountLiveData.observe(this, this::handleProjectCountChanged)
        viewModel.syncStatusLiveData.observe(this, this::handleSyncStateChanged)
        viewModel.syncButtonEnabledLiveData.observe(this, this::handleSyncButtonEnabledChanged)
        viewModel.showProgressLiveData.observe(this, this::handleShowProgress)
        viewModel.showEmptyLayoutLiveData.observe(this, this::handleShowEmptyLayout)
        viewModel.showSettingsViewModel.observe(this) {
            handleShowSettings()
        }
        viewModel.showInterviewListLiveData.observe(this, this::handleShowInterviewList)
        viewModel.showWhatsNewLiveData.observe(this, this::handleShowWhatsNew)
    }

    private fun handleProjectCountChanged(count: Int) {
        viewBinding.projectAppBarLayout.projectCountText.text =
            resources.getQuantityString(R.plurals.projectlist_project_count, count, count)
    }

    private fun handleSyncStateChanged(state: UiSyncState) {
        when (state) {
            UiSyncState.WAITING -> {
                viewBinding.projectAppBarLayout.buttonSync.animation = null
                viewBinding.projectAppBarLayout.buttonSync
                    .setImageResource(R.drawable.ic_sync_disabled)
            }
            UiSyncState.SYNCING -> {
                viewBinding.projectAppBarLayout.buttonSync.setImageResource(R.drawable.ic_sync)
                animateRotatingSyncButton()
            }
            UiSyncState.DONE -> {
                viewBinding.projectAppBarLayout.buttonSync.animation = null
                viewBinding.projectAppBarLayout.buttonSync.setImageResource(R.drawable.ic_done)
            }
        }
    }

    private fun handleSyncButtonEnabledChanged(enabled: Boolean) {
        viewBinding.projectAppBarLayout.buttonSync.isEnabled = enabled
    }

    private fun handleShowProgress(showProgress: Boolean) {
        viewBinding.loadingIndicator.visibility = if (showProgress) View.VISIBLE else View.GONE
    }

    private fun handleShowEmptyLayout(showEmpty: Boolean) {
        if (showEmpty) {
            viewBinding.emptyListTextView.visibility = View.VISIBLE
            viewBinding.projectList.visibility = View.GONE
        } else {
            viewBinding.emptyListTextView.visibility = View.GONE
            viewBinding.projectList.visibility = View.VISIBLE
        }
    }

    private fun handleShowSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun handleShowInterviewList(project: UiProject) {
        Intent(this, InterviewListActivity::class.java)
            .putExtra(InterviewListActivity.ARG_PROJECT_ID, project.id)
            .putExtra(InterviewListActivity.ARG_PROJECT_TITLE, project.title)
            .let(this::startActivity)
    }

    private fun handleShowWhatsNew(event: Event<Unit>?) {
        if (event?.getContentIfNotHandled() != null) {
            startActivity(Intent(this, WhatsNewActivity::class.java))
        }
    }

    private fun animateRotatingSyncButton() {
        val rotate = RotateAnimation(
            0.0f,
            -180.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = Const.ROTATION_DURATION_MILLIS
        rotate.interpolator = LinearInterpolator()
        rotate.repeatMode = Animation.RESTART
        rotate.repeatCount = Animation.INFINITE
        viewBinding.projectAppBarLayout.buttonSync.startAnimation(rotate)
    }
}