package com.rodrigo.androidapp.futtrack.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.UserProfile
import com.rodrigo.androidapp.futtrack.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                _uiState.update { currentState ->
                    currentState.copy(
                        userProfile = profile,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.signInWithGoogleToken(idToken)

            result.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro desconhecido ao logar"
                    )
                }
            }
        }
    }
}