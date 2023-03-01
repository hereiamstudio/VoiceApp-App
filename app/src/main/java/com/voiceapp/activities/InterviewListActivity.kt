package com.voiceapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.voiceapp.adapters.InterviewAdapter
import com.voiceapp.data.model.Interview
import com.voiceapp.data.model.Project
import com.voiceapp.databinding.ActivityInterviewsBinding
import com.voiceapp.viewmodels.InterviewViewModel
import com.voiceapp.viewmodels.ProjectViewModel
import dagger.android.AndroidInjection
import javax.inject.Inject

class InterviewListActivity : BaseActivity(true) {

    companion object{
        const val ARG_PROJECT_ID: String = "PROJECT_ID"
        const val ARG_PROJECT_TITLE: String = "PROJECT_NAME"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val projectViewModel: ProjectViewModel by viewModels { viewModelFactory }
    private val interviewViewModel: InterviewViewModel by viewModels { viewModelFactory }

    private lateinit var projectId: String
    private lateinit var interviewAdapter: InterviewAdapter
    private lateinit var binding: ActivityInterviewsBinding

    override fun getRootView(): ViewGroup {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        binding = ActivityInterviewsBinding.inflate(layoutInflater)

        projectId = intent.getStringExtra(ARG_PROJECT_ID)!!
        val projectName = intent.getStringExtra(ARG_PROJECT_TITLE)!!

        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.appBar.toolbar)

        supportActionBar?.apply {
            title = projectName
            // Display the app icon in action bar/toolbar
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onResume() {
        super.onResume()
        projectViewModel.getProject(projectId).observe(this) { project ->
            onProjectLoaded(project)
        }
    }

    private fun onProjectLoaded(project: Project) {
        interviewViewModel.getInterviewsWithQuestions(project.id!!).observe(this) { interviews ->
            onDataLoaded(project, interviews)
        }
    }

    private fun onDataLoaded(project: Project, interviews: List<Interview>) {
        interviewAdapter =  InterviewAdapter(project, object : InterviewAdapter.InteviewListener {
            override fun onInterviewClicked(interview: Interview) {
                onInterviewClicked(project, interview)
            }
        })

        binding.interviewList.layoutManager = LinearLayoutManager(this)
        binding.interviewList.adapter = interviewAdapter

        binding.interviewList.visibility = View.VISIBLE
        interviewAdapter.setData(interviews)
    }

    private fun onInterviewClicked(project: Project, interview: Interview) {
        Intent(this, ConsentActivity::class.java)
            .putExtra(ConsentActivity.ARG_PROJECT_ID, project.id)
            .putExtra(ConsentActivity.ARG_INTERVIEW_TITLE, interview.title)
            .putExtra(ConsentActivity.ARG_INTERVIEW_ID, interview.id)
            .putExtra(ConsentActivity.ARG_INTERVIEW_LOCALE, interview.locale)
            .putExtra(ConsentActivity.ARG_INTERVIEW_PRIMARY_LANGUAGE, interview.primary_language)
            .putExtra(ConsentActivity.ARG_HAS_CONSENT_STEP_2, interview.consent_step_2 != null)
            .putExtra(ConsentActivity.ARG_HAS_SKIP_LOGIC, interview.has_skip_logic)
            .let(this::startActivity)
    }
}