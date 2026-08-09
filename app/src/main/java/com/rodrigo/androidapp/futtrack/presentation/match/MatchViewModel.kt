package com.rodrigo.androidapp.futtrack.presentation.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchSlot
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
    val groupedMatches: Map<LocalDate, List<Match>> = emptyMap()
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
        val groupedMatches = matches
            .filter(::isVisibleMatch)
            .groupBy { match ->
                match.date.toLocalDate()
            }
            .mapValues { (_, matchesForDate) ->
                matchesForDate.sortedWith(matchComparator)
            }

        MatchUiState(
            isLoading = false,
            availableTeams = teams,
            groupedMatches = groupedMatches
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MatchUiState()
    )

    fun getAvailableMatchSlots(
        date: LocalDate
    ): List<MatchSlot> {
        val usedMatchNumbers = uiState.value
            .groupedMatches[date]
            .orEmpty()
            .mapNotNull(Match::matchNumber)
            .toSet()

        return MatchSlot.entries.filterNot { slot ->
            slot.matchNumber in usedMatchNumbers
        }
    }

    fun scheduleNewMatch(
        homeTeamId: String,
        awayTeamId: String,
        date: LocalDate,
        slot: MatchSlot
    ) {
        if (homeTeamId == awayTeamId) {
            return
        }

        if (slot !in getAvailableMatchSlots(date)) {
            return
        }

        val match = Match(
            matchNumber = slot.matchNumber,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            date = date.atTime(slot.startTime)
        )

        viewModelScope.launch {
            matchRepository.scheduleMatch(match)
        }
    }

    fun deleteMatch(matchId: String) {
        viewModelScope.launch {
            matchRepository.deleteMatch(matchId)
        }
    }

    fun finishMatch(
        matchId: String,
        homeScore: Int,
        awayScore: Int
    ) {
        viewModelScope.launch {
            val match = findMatch(matchId)
                ?: return@launch

            val updatedMatch = match.copy(
                homeScore = homeScore,
                awayScore = awayScore,
                status = MatchStatus.FINISHED
            )

            matchRepository.updateMatch(updatedMatch)
        }
    }

    private fun findMatch(matchId: String): Match? {
        return uiState.value
            .groupedMatches
            .values
            .asSequence()
            .flatten()
            .find { match ->
                match.id == matchId
            }
    }

    private fun isVisibleMatch(match: Match): Boolean {
        return match.date.isAfter(HISTORICAL_MATCHES_CUTOFF)
    }

    private companion object {
        val HISTORICAL_MATCHES_CUTOFF =
            LocalDateTime.of(2026, 5, 31, 23, 59)

        val matchComparator =
            compareBy<Match> { match ->
                match.matchNumber ?: Int.MAX_VALUE
            }.thenBy { match ->
                match.date
            }
    }
}