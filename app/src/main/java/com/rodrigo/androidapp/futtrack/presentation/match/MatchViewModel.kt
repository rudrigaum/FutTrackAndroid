package com.rodrigo.androidapp.futtrack.presentation.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class MatchUiState(
    val isLoading: Boolean = true,
    val availableTeams: List<Team> = emptyList(),
    val scheduledMatches: List<Match> = emptyList()
)

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    val uiState: StateFlow<MatchUiState> = combine(
        teamRepository.getTeams(),
        matchRepository.getMatches()
    ) { teams, matches ->
        MatchUiState(
            isLoading = false,
            availableTeams = teams,
            scheduledMatches = matches
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MatchUiState(isLoading = true)
    )

    fun scheduleNewMatch(homeTeamId: String, awayTeamId: String, date: LocalDateTime) {
        viewModelScope.launch {
            val newMatch = Match(
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId,
                date = date
            )
            matchRepository.scheduleMatch(newMatch)
        }
    }

    fun deleteMatch(matchId: String) {
        viewModelScope.launch {
            matchRepository.deleteMatch(matchId)
        }
    }

    fun finishMatch(matchId: String, homeScore: Int, awayScore: Int) {
        viewModelScope.launch {
            val match = uiState.value.scheduledMatches.find { it.id == matchId }
            if (match != null) {
                val updatedMatch = match.copy(
                    homeScore = homeScore,
                    awayScore = awayScore,
                    status = MatchStatus.FINISHED
                )
                matchRepository.updateMatch(updatedMatch)
            }
        }
    }
}