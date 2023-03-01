package com.voiceapp.core.interview

import com.voiceapp.core.user.UserProfile
import com.voiceapp.core.user.UserRepository
import com.voiceapp.core.interview.InterviewRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseInterviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) : InterviewRepository {

    companion object {

        private const val GROUP_INTERVIEWS: String = "interviews_public"

        private const val FIELD_ASSIGNED_USER_IDS = "assigned_users_ids"
        private const val FIELD_PROJECT_ID = "project.id"
        private const val FIELD_STATUS = "status"
        private const val FIELD_ARCHIVED = "is_archived"

        private const val STATUS_ACTIVE = "active"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getInterviewCountForProjectsFlow(projects: Set<String>) =
        userRepository.userProfileFlow
            .distinctUntilChanged()
            .flatMapLatest { createInterviewCountForProjectsFlow(it, projects) }

    private fun createInterviewCountForProjectsFlow(
        userProfile: UserProfile?,
        projects: Set<String>) = userProfile?.let {
        if (projects.isNotEmpty()) {
            callbackFlow {
                val listener = firestore.collectionGroup(GROUP_INTERVIEWS)
                    .whereArrayContains(FIELD_ASSIGNED_USER_IDS, it.userId)
                    .whereIn(FIELD_PROJECT_ID, projects.toList())
                    .whereEqualTo(FIELD_STATUS, STATUS_ACTIVE)
                    .whereEqualTo(FIELD_ARCHIVED, false)
                    .addSnapshotListener { value, error ->
                        launch {
                            handleNewInterviewCountSnapshot(channel, value, error)
                        }
                    }

                awaitClose {
                    listener.remove()
                }
            }
        } else {
            flowOf(emptyMap())
        }
    } ?: flowOf(emptyMap())

    private suspend fun handleNewInterviewCountSnapshot(
        channel: SendChannel<Map<String, Int>>,
        snapshot: QuerySnapshot?,
        exception: FirebaseFirestoreException?) {
        if (exception != null) {
            channel.send(emptyMap())
        } else {
            val counts = mutableMapOf<String, Int>()

            snapshot?.documents?.forEach {
                it.getString(FIELD_PROJECT_ID)?.let { projectId ->
                    counts[projectId] = counts[projectId]?.plus(1) ?: 1
                }
            }

            channel.send(counts)
        }
    }
}