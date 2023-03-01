package com.voiceapp.core.project

import kotlinx.coroutines.flow.Flow

interface ProjectRepository {

    val allUserProjectsFlow: Flow<List<Project>?>
}