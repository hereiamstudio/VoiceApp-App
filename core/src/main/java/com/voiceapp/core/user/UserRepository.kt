package com.voiceapp.core.user

import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val isUserLoggedIn: Boolean

    val userStatusFlow: Flow<UserStatus>

    val userProfileFlow: Flow<UserProfile?>

    fun loginFlow(credentials: Credentials): Flow<SignInState>

    fun requestPasswordResetFlow(email: String): Flow<PasswordResetState>

    fun signOut()
}