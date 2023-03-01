package com.voiceapp.ui.project

import com.voiceapp.core.interview.InterviewRepository
import com.voiceapp.core.project.Project
import com.voiceapp.core.project.ProjectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProjectFetcher @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val interviewRepository: InterviewRepository
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val projectsFlow get() = projectRepository.allUserProjectsFlow
        .flatMapLatest(this::loadProjectInterviewCounts)

    private fun loadProjectInterviewCounts(projects: List<Project>?) =
        projects?.let { p ->
            val projectIds = p.map(Project::id).toSet()

            interviewRepository.getInterviewCountForProjectsFlow(projectIds)
                .map { combineProjectsWithInterviewCounts(p, it) }
        } ?: flowOf(null)

    private fun combineProjectsWithInterviewCounts(
        projects: List<Project>,
        interviewCounts: Map<String, Int>) = projects.map {
        createUiProject(it, interviewCounts[it.id] ?: 0)
    }

    private fun createUiProject(project: Project, interviewCount: Int) =
        UiProject(
            project.id,
            project.title,
            project.description,
            interviewCount)
}