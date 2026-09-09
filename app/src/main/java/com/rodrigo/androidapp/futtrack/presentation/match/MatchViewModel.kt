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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

data class MatchUiState(
    val isLoading: Boolean = true,
    val availableTeams: List<Team> = emptyList(),
    val groupedMatches: Map<LocalDate, List<Match>> = emptyMap(),
    val filteredGroupedMatches: Map<LocalDate, List<Match>> = emptyMap(),
    val selectedMonth: YearMonth? = null,
    val canSelectOlderMonth: Boolean = false,
    val canSelectNewerMonth: Boolean = false
)

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val selectedMonth =
        MutableStateFlow<YearMonth?>(null)

    val uiState: StateFlow<MatchUiState> = combine(
        teamRepository.getTeams(),
        matchRepository.getMatches(),
        selectedMonth
    ) { teams, matches, requestedMonth ->
        val groupedMatches =
            groupAndSortMatches(matches)

        val availableMonths =
            getAvailableMonths(groupedMatches)

        val effectiveMonth = resolveSelectedMonth(
            requestedMonth = requestedMonth,
            availableMonths = availableMonths
        )

        val selectedMonthIndex =
            availableMonths.indexOf(effectiveMonth)

        MatchUiState(
            isLoading = false,
            availableTeams = teams,
            groupedMatches = groupedMatches,
            filteredGroupedMatches = filterMatchesByMonth(
                groupedMatches = groupedMatches,
                selectedMonth = effectiveMonth
            ),
            selectedMonth = effectiveMonth,
            canSelectOlderMonth =
                selectedMonthIndex >= 0 &&
                        selectedMonthIndex < availableMonths.lastIndex,
            canSelectNewerMonth =
                selectedMonthIndex > 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MatchUiState()
    )

    fun selectOlderMonth() {
        selectAdjacentMonth(
            offset = OLDER_MONTH_OFFSET
        )
    }

    fun selectNewerMonth() {
        selectAdjacentMonth(
            offset = NEWER_MONTH_OFFSET
        )
    }

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

    private fun selectAdjacentMonth(
        offset: Int
    ) {
        val state = uiState.value
        val currentMonth = state.selectedMonth
            ?: return

        val availableMonths =
            getAvailableMonths(state.groupedMatches)

        val currentIndex =
            availableMonths.indexOf(currentMonth)

        if (currentIndex < 0) {
            return
        }

        val targetMonth = availableMonths.getOrNull(
            currentIndex + offset
        ) ?: return

        selectedMonth.value = targetMonth
    }

    private fun groupAndSortMatches(
        matches: List<Match>
    ): Map<LocalDate, List<Match>> {
        return matches
            .filter(::isVisibleMatch)
            .groupBy { match ->
                match.date.toLocalDate()
            }
            .toSortedMap(
                compareByDescending { date ->
                    date
                }
            )
            .mapValues { (_, matchesForDate) ->
                matchesForDate.sortedWith(matchComparator)
            }
    }

    private fun getAvailableMonths(
        groupedMatches: Map<LocalDate, List<Match>>
    ): List<YearMonth> {
        return groupedMatches
            .keys
            .map(YearMonth::from)
            .distinct()
            .sortedDescending()
    }

    private fun resolveSelectedMonth(
        requestedMonth: YearMonth?,
        availableMonths: List<YearMonth>
    ): YearMonth? {
        return requestedMonth
            ?.takeIf { month ->
                month in availableMonths
            }
            ?: availableMonths.firstOrNull()
    }

    private fun filterMatchesByMonth(
        groupedMatches: Map<LocalDate, List<Match>>,
        selectedMonth: YearMonth?
    ): Map<LocalDate, List<Match>> {
        if (selectedMonth == null) {
            return emptyMap()
        }

        return groupedMatches.filterKeys { date ->
            YearMonth.from(date) == selectedMonth
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
        return match.date.isAfter(
            HISTORICAL_MATCHES_CUTOFF
        )
    }

    private companion object {
        const val OLDER_MONTH_OFFSET = 1
        const val NEWER_MONTH_OFFSET = -1

        val HISTORICAL_MATCHES_CUTOFF =
            LocalDateTime.of(
                2026,
                5,
                31,
                23,
                59
            )

        val matchComparator =
            compareBy<Match> { match ->
                match.matchNumber ?: Int.MAX_VALUE
            }.thenBy { match ->
                match.date
            }
    }
}