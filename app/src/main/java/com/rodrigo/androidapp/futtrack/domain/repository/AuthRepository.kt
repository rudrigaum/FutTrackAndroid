package com.rodrigo.androidapp.futtrack.domain.repository
import com.rodrigo.androidapp.futtrack.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUserStream(): Flow<UserProfile?>
    suspend fun syncUserAuth()
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signOut()
}