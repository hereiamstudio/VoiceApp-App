package com.voiceapp.ui.project

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.voiceapp.core.appproperties.AppPropertiesRepository
import com.voiceapp.core.settings.SettingsRepository
import com.voiceapp.core.sync.SyncRepository
import com.voiceapp.core.sync.SyncState
import com.voiceapp.coroutines.MainCoroutineRule
import com.voiceapp.livedata.Event
import com.voiceapp.testutils.LiveDataTestObserver
import com.voiceapp.ui.project.ProjectFetcher
import com.voiceapp.ui.project.ProjectListActivityViewModel
import com.voiceapp.ui.project.UiProject
import com.voiceapp.ui.project.UiSyncState
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ProjectListActivityViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var projectFetcher: ProjectFetcher
    @Mock
    private lateinit var syncRepository: SyncRepository
    @Mock
    private lateinit var appPropertiesRepository: AppPropertiesRepository
    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private val viewModel: ProjectListActivityViewModel by lazy {
        ProjectListActivityViewModel(
            projectFetcher,
            syncRepository,
            appPropertiesRepository,
            settingsRepository,
            coroutineRule.dispatcher,
            coroutineRule.scope)
    }

    private val projectsObserver = LiveDataTestObserver<List<UiProject>?>()
    private val projectCountObserver = LiveDataTestObserver<Int>()
    private val syncStatusObserver = LiveDataTestObserver<UiSyncState>()
    private val syncButtonEnabledObserver = LiveDataTestObserver<Boolean>()
    private val showProgressObserver = LiveDataTestObserver<Boolean>()
    private val showEmptyLayoutObserver = LiveDataTestObserver<Boolean>()
    private val showSettingsObserver = LiveDataTestObserver<Nothing?>()
    private val showInterviewListObserver = LiveDataTestObserver<UiProject>()
    private val showWhatsNewObserver = LiveDataTestObserver<Event<Unit>?>()

    @Before
    fun setUp() {
        whenever(syncRepository.syncStateFlow)
            .thenReturn(emptyFlow())
        whenever(projectFetcher.projectsFlow)
            .thenReturn(emptyFlow())
    }

    @Test
    fun projectsInitiallyEmitsNull() {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(emptyFlow())

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        projectsObserver.assertValues(null)
    }

    @Test
    fun projectsEmitsEmptyListWhenUpstreamIsNull() {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(flowOf(null))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        projectsObserver.assertValues(null, emptyList())
    }

    @Test
    fun projectEmitsEmptyListWhenUpstreamEmitsEmptyList() {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(flowOf(emptyList()))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        projectsObserver.assertValues(null, emptyList())
    }

    @Test
    fun projectEmitsPopulatedListWhenUpstreamEmitsPopulatedList() {
        val project1 = mock<UiProject>()
        val project2 = mock<UiProject>()
        val project3 = mock<UiProject>()
        val projects = listOf(project1, project2, project3)
        whenever(projectFetcher.projectsFlow)
            .thenReturn(flowOf(projects))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        projectsObserver.assertValues(null, projects)
    }

    @Test
    fun projectCountEmits0Initially() {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(emptyFlow())

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        projectCountObserver.assertValues(0)
    }

    @Test
    fun projectsCountEmits0WhenUpstreamIsNull() {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(flowOf(null))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        projectCountObserver.assertValues(0)
    }

    @Test
    fun projectsCountEmits0WhenUpstreamIsEmpty() {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(flowOf(emptyList()))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        projectCountObserver.assertValues(0)
    }

    @Test
    fun projectCountEmitsCorrectValueWhenUpstreamEmitsPopulatedList() {
        val project1 = mock<UiProject>()
        val project2 = mock<UiProject>()
        val project3 = mock<UiProject>()
        val projects = listOf(project1, project2, project3)
        whenever(projectFetcher.projectsFlow)
            .thenReturn(flowOf(projects))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        projectCountObserver.assertValues(0, 3)
    }

    @Test
    fun syncStatusEmitsWaitingWhenDeviceIsOffline() {
        whenever(syncRepository.syncStateFlow)
            .thenReturn(flowOf(SyncState.OFFLINE))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        syncStatusObserver.assertValues(UiSyncState.WAITING)
    }

    @Test
    fun syncStatusEmitsDoneWhenIdle() {
        whenever(syncRepository.syncStateFlow)
            .thenReturn(flowOf(SyncState.IDLE))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        syncStatusObserver.assertValues(UiSyncState.DONE)
    }

    @Test
    fun syncStatusEmitsSyncingWhenSyncing() {
        whenever(syncRepository.syncStateFlow)
            .thenReturn(flowOf(SyncState.SYNCING))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        syncStatusObserver.assertValues(UiSyncState.SYNCING)
    }

    @Test
    fun syncButtonEnabledIsFalseWhenSyncStateIsOffline() {
        whenever(syncRepository.syncStateFlow)
            .thenReturn(flowOf(SyncState.OFFLINE))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        syncButtonEnabledObserver.assertValues(false)
    }

    @Test
    fun syncButtonEnabledIsFalseWhenSyncing() {
        whenever(syncRepository.syncStateFlow)
            .thenReturn(flowOf(SyncState.SYNCING))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        syncButtonEnabledObserver.assertValues(false)
    }

    @Test
    fun syncButtonEnabledIsTrueWhenIdle() {
        whenever(syncRepository.syncStateFlow)
            .thenReturn(flowOf(SyncState.IDLE))

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        syncButtonEnabledObserver.assertValues(true)
    }

    @Test
    fun showProgressInitiallyShowsProgress() {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(emptyFlow())

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showProgressObserver.assertValues(true)
    }

    @Test
    fun showProgressShowsProgressOnInitialLoad() = runTest {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(flow {
                delay(1000L)
                emit(listOf(mock()))
            })

        registerLiveDataObservers()
        advanceUntilIdle()

        showProgressObserver.assertValues(true, false)
    }

    @Test
    fun showEmptyInitiallyShowsEmpty() {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(emptyFlow())

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showEmptyLayoutObserver.assertValues(true)
    }

    @Test
    fun showEmptyShowsEmptyWhenProjectsListIsEmpty() = runTest {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(flow {
                delay(1000L)
                emit(emptyList())
            })

        registerLiveDataObservers()
        advanceUntilIdle()

        showEmptyLayoutObserver.assertValues(true, true)
    }

    @Test
    fun showEmptyShowsNonEmptyWhenProjectsListIsPopulated() = runTest {
        whenever(projectFetcher.projectsFlow)
            .thenReturn(flow {
                delay(1000L)
                emit(listOf(mock()))
            })

        registerLiveDataObservers()
        advanceUntilIdle()

        showEmptyLayoutObserver.assertValues(true, false)
    }

    @Test
    fun showSettingsDoesNotEmitByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showSettingsObserver.assertEmpty()
    }

    @Test
    fun showSettingsShowsSettingsWhenClicked() {
        registerLiveDataObservers()
        viewModel.onSettingsButtonClicked()
        coroutineRule.scope.advanceUntilIdle()

        showSettingsObserver.assertValues(null)
    }

    @Test
    fun showInterviewListDoesNotEmitByDefault() {
        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showInterviewListObserver.assertEmpty()
    }

    @Test
    fun showInterviewListShowsInterviewListOnUserClick() {
        val project = mock<UiProject>()

        registerLiveDataObservers()
        viewModel.onProjectClicked(project)
        coroutineRule.scope.advanceUntilIdle()

        showInterviewListObserver.assertValues(project)
    }

    @Test
    fun showWhatsNewDoesNotShowWhatsNewWhenVersionCodesMatch() {
        whenever(settingsRepository.lastSeenVersionCode)
            .thenReturn(10)
        whenever(appPropertiesRepository.versionCode)
            .thenReturn(10)

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        showWhatsNewObserver.assertEmpty()
    }

    @Test
    fun showWhatsNewShowsWhatsNewWhenVersionCodeIsGreaterThanStoredVersionCode() {
        whenever(appPropertiesRepository.versionCode)
            .thenReturn(11)
        whenever(settingsRepository.lastSeenVersionCode)
            .thenReturn(10)

        registerLiveDataObservers()
        coroutineRule.scope.advanceUntilIdle()

        assertEquals(1, showWhatsNewObserver.observedValues.size)
    }

    @Test
    fun onSyncButtonClickedCausesSync() = runTest {
        viewModel.onSyncButtonClicked()
        coroutineRule.scope.advanceUntilIdle()

        verify(syncRepository)
            .performSync()
    }

    private fun registerLiveDataObservers() {
        viewModel.projectsLiveData.observeForever(projectsObserver)
        viewModel.projectCountLiveData.observeForever(projectCountObserver)
        viewModel.syncStatusLiveData.observeForever(syncStatusObserver)
        viewModel.syncButtonEnabledLiveData.observeForever(syncButtonEnabledObserver)
        viewModel.showProgressLiveData.observeForever(showProgressObserver)
        viewModel.showEmptyLayoutLiveData.observeForever(showEmptyLayoutObserver)
        viewModel.showSettingsViewModel.observeForever(showSettingsObserver)
        viewModel.showInterviewListLiveData.observeForever(showInterviewListObserver)
        viewModel.showWhatsNewLiveData.observeForever(showWhatsNewObserver)
    }
}