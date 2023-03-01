package com.voiceapp.core.project

import com.voiceapp.core.user.UserProfile
import com.voiceapp.core.user.UserRepository
import com.voiceapp.core.project.Project
import com.voiceapp.core.project.ProjectRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirestoreProjectRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) : ProjectRepository {

    companion object {

        private const val GROUP_PROJECTS = "projects_public"

        private const val FIELD_ASSIGNED_USER_IDS = "assigned_users_ids"
        private const val FIELD_ARCHIVED = "is_archived"
        private const val FIELD_ACTIVE = "is_active"
        private const val FIELD_CREATED = "created_at"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val allUserProjectsFlow get() =
        userRepository.userProfileFlow
            .distinctUntilChanged()
            .flatMapLatest(this::createAllUserProjectsFlow)

    private fun createAllUserProjectsFlow(userProfile: UserProfile?) = userProfile?.let {
        callbackFlow<List<Project>?> {
            val listener = firestore.collectionGroup(GROUP_PROJECTS)
                .whereEqualTo(FIELD_ARCHIVED, false)
                .whereEqualTo(FIELD_ACTIVE, true)
                .whereArrayContains(FIELD_ASSIGNED_USER_IDS, it.userId)
                .orderBy(FIELD_CREATED, Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    launch {
                        handleNewProjectSnapshot(channel, value, error)
                    }
                }

            awaitClose {
                listener.remove()
            }
        }
    } ?: flowOf(null)

    private suspend fun handleNewProjectSnapshot(
        channel: SendChannel<List<Project>?>,
        snapshot: QuerySnapshot?,
        exception: FirebaseFirestoreException?) {
        if (exception != null) {
            channel.send(null)
        } else {
            val projects = snapshot?.toObjects(FirebaseProject::class.java)
                ?.map(this::mapFirebaseProjectToProject)

            channel.send(projects)
        }
    }

    private fun mapFirebaseProjectToProject(firebaseProject: FirebaseProject) =
        Project(
            firebaseProject.id,
            firebaseProject.title,
            firebaseProject.description)
}