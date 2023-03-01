package com.voiceapp.core.interview

import kotlinx.coroutines.flow.Flow

interface InterviewRepository {

    fun getInterviewCountForProjectsFlow(projects: Set<String>): Flow<Map<String, Int>>
}