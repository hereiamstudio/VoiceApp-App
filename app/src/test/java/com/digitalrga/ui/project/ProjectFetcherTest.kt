package com.voiceapp.ui.project

import com.voiceapp.core.interview.InterviewRepository
import com.voiceapp.core.project.Project
import com.voiceapp.core.project.ProjectRepository
import com.voiceapp.coroutines.MainCoroutineRule
import com.voiceapp.coroutines.test
import com.voiceapp.ui.project.ProjectFetcher
import com.voiceapp.ui.project.UiProject
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ProjectFetcherTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var projectRepository: ProjectRepository
    @Mock
    private lateinit var interviewRepository: InterviewRepository

    private lateinit var fetcher: ProjectFetcher

    @Before
    fun setUp() {
        fetcher = ProjectFetcher(projectRepository, interviewRepository)
    }

    @Test
    fun projectsFlowWithNullProjectsEmitsNull() = runTest {
        whenever(projectRepository.allUserProjectsFlow)
            .thenReturn(flowOf(null))

        val observer = fetcher.projectsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null)
        verify(interviewRepository, never())
            .getInterviewCountForProjectsFlow(any())
    }

    @Test
    fun projectsFlowWithEmptyListEmitsEmptyList() = runTest {
        whenever(projectRepository.allUserProjectsFlow)
            .thenReturn(flowOf(emptyList()))
        whenever(interviewRepository.getInterviewCountForProjectsFlow(emptySet()))
            .thenReturn(flowOf(emptyMap()))

        val observer = fetcher.projectsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(emptyList())
    }

    @Test
    fun projectsFlowWithSingleItemListAndInterviewsNotFoundEmitsCountOfZero() = runTest {
        val projects = listOf(
            Project("id", "Title", "Description")
        )
        whenever(projectRepository.allUserProjectsFlow)
            .thenReturn(flowOf(projects))
        whenever(interviewRepository.getInterviewCountForProjectsFlow(setOf("id")))
            .thenReturn(flowOf(emptyMap()))

        val observer = fetcher.projectsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(listOf(
            UiProject("id", "Title", "Description", 0)
        ))
    }

    @Test
    fun projectsFlowWithSingleItemListAndHasInterviewsEmitsCount() = runTest {
        val projects = listOf(
            Project("id", "Title", "Description")
        )
        whenever(projectRepository.allUserProjectsFlow)
            .thenReturn(flowOf(projects))
        whenever(interviewRepository.getInterviewCountForProjectsFlow(setOf("id")))
            .thenReturn(flowOf(mapOf("id" to 5)))

        val observer = fetcher.projectsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(listOf(
            UiProject("id", "Title", "Description", 5)
        ))
    }

    @Test
    fun projectsFlowWithSingleItemEmitsProjectUpdates() = runTest {
        val projects1 = listOf(
            Project("id", "Title", "Description")
        )
        val projects2 = listOf(
            Project("id", "Updated title", "Description")
        )
        whenever(projectRepository.allUserProjectsFlow)
            .thenReturn(flow {
                emit(projects1)
                delay(100L)
                emit(projects2)
            })
        whenever(interviewRepository.getInterviewCountForProjectsFlow(setOf("id")))
            .thenReturn(flowOf(mapOf("id" to 5)))

        val observer = fetcher.projectsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            listOf(
                UiProject("id", "Title", "Description", 5)
            ),
            listOf(
                UiProject("id", "Updated title", "Description", 5)
            ))
    }

    @Test
    fun projectsFlowWithSingleItemListAndHasInterviewsEmitsCountUpdated() = runTest {
        val projects = listOf(
            Project("id", "Title", "Description")
        )
        whenever(projectRepository.allUserProjectsFlow)
            .thenReturn(flowOf(projects))
        whenever(interviewRepository.getInterviewCountForProjectsFlow(setOf("id")))
            .thenReturn(flow {
                emit(mapOf("id" to 5))
                delay(100L)
                emit(mapOf("id" to 4))
            })

        val observer = fetcher.projectsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            listOf(
                UiProject("id", "Title", "Description", 5)
            ),
            listOf(
                UiProject("id", "Title", "Description", 4)
            ))
    }
}