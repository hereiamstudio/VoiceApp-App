package com.voiceapp.core.user

import com.voiceapp.core.sync.SyncRepository
import com.voiceapp.core.user.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseUserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val syncRepository: SyncRepository
) : UserRepository {

    override val isUserLoggedIn
        get() = firebaseAuth.currentUser != null

    override val userStatusFlow: Flow<UserStatus>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener {
                launch {
                    getAndSendCurrentUserStatus(channel)
                }
            }

            // As documented, adding the listener immediately fires an event
            firebaseAuth.addAuthStateListener(listener)

            awaitClose {
                firebaseAuth.removeAuthStateListener(listener)
            }
        }.distinctUntilChanged()

    override val userProfileFlow get() = userStatusFlow.map { createUserProfile() }

    override fun loginFlow(credentials: Credentials) = callbackFlow {
        val completeListener = OnCompleteListener<AuthResult> {
            launch {
                handleLoginTaskComplete(channel, it)
            }
        }

        channel.send(SignInState.PROGRESS)
        firebaseAuth.signInWithEmailAndPassword(credentials.email, credentials.password)
            .addOnCompleteListener(completeListener)

        awaitClose()
    }.transform {
        if (it == SignInState.SUCCESS) {
            // Perform a sync of user data before declaring the login a success.
            syncRepository.performSync(true)
        }

        emit(it)
    }

    override fun requestPasswordResetFlow(email: String) = callbackFlow {
        val completeListener = OnCompleteListener<Void> {
            launch {
                handleRequestPasswordTaskComplete(channel, it)
            }
        }

        channel.send(PasswordResetState.PROGRESS)
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(completeListener)

        awaitClose()
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private fun createUserProfile() = firebaseAuth.currentUser?.let {
        UserProfile(it.uid)
    }

    private suspend fun getAndSendCurrentUserStatus(
        channel: SendChannel<UserStatus>) {
        if (isUserLoggedIn) {
            channel.send(UserStatus.SIGNED_IN)
        } else {
            channel.send(UserStatus.SIGNED_OUT)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun handleLoginTaskComplete(
        channel: SendChannel<SignInState>,
        task: Task<AuthResult>) {

        if (!channel.isClosedForSend) {
            channel.send(if (task.isSuccessful) SignInState.SUCCESS else SignInState.ERROR)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun handleRequestPasswordTaskComplete(
        channel: SendChannel<PasswordResetState>,
        task: Task<Void>) {
        if (!channel.isClosedForSend) {
            val state = if (task.isSuccessful) {
                PasswordResetState.SUCCESS
            } else {
                PasswordResetState.ERROR
            }

            channel.send(state)
        }
    }
}