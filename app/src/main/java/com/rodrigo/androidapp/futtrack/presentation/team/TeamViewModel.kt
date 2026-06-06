package com.rodrigo.androidapp.futtrack.presentation.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.Player
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.repository.PlayerRepository
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamUiState(
    val teams: List<Team> = emptyList(),
    val isLoading: Boolean = true,
    val isPlayersLoading: Boolean = false,
    val selectedTeamPlayers: List<Player> = emptyList()
)

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    private var playersJob: Job? = null

    init {
        loadTeams()
    }

    private fun loadTeams() {
        viewModelScope.launch {
            teamRepository.getTeams().collect { teamList ->
                _uiState.update { it.copy(teams = teamList, isLoading = false) }
            }
        }
    }

    fun loadPlayersForTeam(teamId: String) {
        playersJob?.cancel()

        _uiState.update { it.copy(isPlayersLoading = true, selectedTeamPlayers = emptyList()) }

        playersJob = viewModelScope.launch {
            playerRepository.getPlayersByTeam(teamId).collect { players ->
                _uiState.update { it.copy(selectedTeamPlayers = players, isPlayersLoading = false) }
            }
        }
    }

    fun clearSelectedPlayers() {
        _uiState.update { it.copy(selectedTeamPlayers = emptyList(), isPlayersLoading = false) }
    }
}