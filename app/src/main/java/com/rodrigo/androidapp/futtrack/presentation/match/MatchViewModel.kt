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
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class MatchUiState(
    val isLoading: Boolean = true,
    val availableTeams: List<Team> = emptyList(),
    // Agora enviamos para a tela um mapa agrupado pela data
    val groupedMatches: Map<LocalDate, List<Match>> = emptyMap()
)

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val cutoffDate = LocalDateTime.of(2026, 5, 31, 23, 59)

    val uiState: StateFlow<MatchUiState> = combine(
        teamRepository.getTeams(),
        matchRepository.getMatches()
    ) { teams, matches ->

        val visibleMatches = matches.filter { it.date.isAfter(cutoffDate) }
        val grouped = visibleMatches.groupBy { it.date.toLocalDate() }

        MatchUiState(
            isLoading = false,
            availableTeams = teams,
            groupedMatches = grouped
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
            val allMatches = uiState.value.groupedMatches.values.flatten()
            val match = allMatches.find { it.id == matchId }
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