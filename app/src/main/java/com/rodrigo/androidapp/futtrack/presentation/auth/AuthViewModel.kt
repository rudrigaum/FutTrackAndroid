package com.rodrigo.androidapp.futtrack.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.rodrigo.androidapp.futtrack.domain.model.UserProfile
import com.rodrigo.androidapp.futtrack.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class AuthUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isAdminMode: Boolean
        get() = userProfile?.isAdmin == true
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())

    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getCurrentUserStream().collect { profile ->
                Log.d("AuthDebug", "VAR: Perfil recebido do Firestore -> $profile")
                _uiState.update { currentState ->
                    currentState.copy(
                        userProfile = profile,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                authRepository.signInWithEmail(email, password)
                authRepository.syncUserAuth()
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()

                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "E-mail não encontrado. Tem certeza que é o do admin?"
                    is FirebaseAuthInvalidCredentialsException -> "Senha incorreta, meu chapa. Tenta de novo."
                    else -> "Ocorreu um erro ao tentar logar. Verifique sua internet."
                }

                onError(errorMessage)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}