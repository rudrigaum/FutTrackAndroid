package com.rodrigo.androidapp.futtrack.presentation.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TeamUiState {
    data object Loading : TeamUiState
    data class Success(val teams: List<Team>) : TeamUiState
    data class Error(val message: String) : TeamUiState
}

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeamUiState>(TeamUiState.Loading)
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        fetchTeams()
    }

    private fun fetchTeams() {
        viewModelScope.launch {
            try {
                teamRepository.getTeams().collect { teams ->
                    _uiState.value = TeamUiState.Success(teams)
                }
            } catch (e: Exception) {
                _uiState.value = TeamUiState.Error(message = "Erro ao carregar times: ${e.message}")
            }
        }
    }
}