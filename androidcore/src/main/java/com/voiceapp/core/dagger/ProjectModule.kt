package com.voiceapp.core.dagger

import com.voiceapp.core.project.FirestoreProjectRepository
import com.voiceapp.core.project.ProjectRepository
import dagger.Binds
import dagger.Module

@Module
internal interface ProjectModule {

    @Suppress("unused")
    @Binds
    fun bindProjectRepository(firestoreProjectRepository: FirestoreProjectRepository):
            ProjectRepository
}