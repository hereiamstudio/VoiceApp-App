package com.voiceapp.ui.project

import androidx.lifecycle.*
import com.voiceapp.core.appproperties.AppPropertiesRepository
import com.voiceapp.core.di.ForDefaultDispatcher
import com.voiceapp.core.di.ForGlobalCoroutineScope
import com.voiceapp.core.settings.SettingsRepository
import com.voiceapp.core.sync.SyncRepository
import com.voiceapp.core.sync.SyncState
import com.voiceapp.livedata.Event
import com.voiceapp.livedata.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProjectListActivityViewModel @Inject constructor(
    projectFetcher: ProjectFetcher,
    private val syncRepository: SyncRepository,
    private val appPropertiesRepository: AppPropertiesRepository,
    private val settingsRepository: SettingsRepository,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @ForGlobalCoroutineScope private val globalCoroutineScope: CoroutineScope) : ViewModel() {

    private val projectsFlow = projectFetcher.projectsFlow
        .flowOn(defaultDispatcher)
        .map { it ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val projectsLiveData = projectsFlow
        .asLiveData(viewModelScope.coroutineContext)

    val projectCountLiveData = projectsFlow.map {
        it?.size ?: 0
    }.distinctUntilChanged().asLiveData(viewModelScope.coroutineContext)

    val syncStatusLiveData = syncRepository.syncStateFlow
        .map(this::mapSyncState)
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext + defaultDispatcher)

    val syncButtonEnabledLiveData = syncStatusLiveData.map { it == UiSyncState.DONE }

    val showProgressLiveData = projectsFlow.map { it == null }
        .asLiveData(viewModelScope.coroutineContext)

    val showEmptyLayoutLiveData = projectsFlow.map { it?.isEmpty() ?: true }
        .asLiveData(viewModelScope.coroutineContext)

    val showSettingsViewModel: LiveData<Nothing> get() = showSettings
    private val showSettings = SingleLiveEvent<Nothing>()

    val showInterviewListLiveData: LiveData<UiProject> get() = showInterviewList
    private val showInterviewList = SingleLiveEvent<UiProject>()

    val showWhatsNewLiveData = liveData(viewModelScope.coroutineContext + defaultDispatcher) {
        val versionCode = appPropertiesRepository.versionCode

        if (versionCode > settingsRepository.lastSeenVersionCode) {
            emit(Event(Unit))
        }

        settingsRepository.lastSeenVersionCode = versionCode
    }

    fun onSyncButtonClicked() {
        globalCoroutineScope.launch {
            syncRepository.performSync()
        }
    }

    fun onSettingsButtonClicked() {
        showSettings.call()
    }

    fun onProjectClicked(project: UiProject) {
        showInterviewList.value = project
    }

    private fun mapSyncState(syncState: SyncState) = when (syncState) {
        SyncState.OFFLINE -> UiSyncState.WAITING
        SyncState.IDLE -> UiSyncState.DONE
        SyncState.SYNCING -> UiSyncState.SYNCING
    }
}