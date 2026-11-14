package com.rodrigo.androidapp.futtrack.presentation.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchLeg
import com.rodrigo.androidapp.futtrack.domain.model.MatchSlot
import com.rodrigo.androidapp.futtrack.domain.model.MatchStage
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import com.rodrigo.androidapp.futtrack.domain.usecase.KnockoutSchedulingResult
import com.rodrigo.androidapp.futtrack.domain.usecase.ScheduleKnockoutMatchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

sealed interface RegularMatchSchedulingResult {
    data object Success : RegularMatchSchedulingResult
    data object Failure : RegularMatchSchedulingResult
}

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository,
    private val scheduleKnockoutMatchUseCase: ScheduleKnockoutMatchUseCase
) : ViewModel() {

    private val selectedMonth = MutableStateFlow<YearMonth?>(null)

    private val _knockoutSchedulingResult =
        MutableStateFlow<KnockoutSchedulingResult?>(null)

    val knockoutSchedulingResult: StateFlow<KnockoutSchedulingResult?> =
        _knockoutSchedulingResult.asStateFlow()

    private val _knockoutPersistenceFailure = MutableStateFlow(false)

    val knockoutPersistenceFailure: StateFlow<Boolean> =
        _knockoutPersistenceFailure.asStateFlow()

    private val _regularSchedulingResult =
        MutableStateFlow<RegularMatchSchedulingResult?>(null)

    val regularSchedulingResult: StateFlow<RegularMatchSchedulingResult?> =
        _regularSchedulingResult.asStateFlow()

    fun consumeKnockoutSchedulingResult() {
        _knockoutSchedulingResult.value = null
    }

    fun consumeKnockoutPersistenceFailure() {
        _knockoutPersistenceFailure.value = false
    }

    fun consumeRegularSchedulingResult() {
        _regularSchedulingResult.value = null
    }

    val uiState: StateFlow<MatchUiState> = combine(
        teamRepository.getTeams(),
        matchRepository.getMatches(),
        selectedMonth
    ) { teams, matches, requestedMonth ->
        val groupedMatches = groupAndSortMatches(matches)
        val availableMonths = getAvailableMonths(groupedMatches)
        val effectiveMonth = resolveSelectedMonth(
            requestedMonth = requestedMonth,
            availableMonths = availableMonths
        )
        val selectedMonthIndex = availableMonths.indexOf(effectiveMonth)

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
            canSelectNewerMonth = selectedMonthIndex > 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MatchUiState()
    )

    fun selectOlderMonth() {
        selectAdjacentMonth(offset = OLDER_MONTH_OFFSET)
    }

    fun selectNewerMonth() {
        selectAdjacentMonth(offset = NEWER_MONTH_OFFSET)
    }

    fun getAvailableMatchSlots(date: LocalDate): List<MatchSlot> {
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
        slot: MatchSlot,
        stage: MatchStage = MatchStage.REGULAR,
        leg: MatchLeg? = null
    ) {
        if (homeTeamId == awayTeamId) {
            return
        }

        if (slot !in getAvailableMatchSlots(date)) {
            return
        }

        when (stage) {
            MatchStage.REGULAR -> {
                if (leg != null) {
                    return
                }
            }
            MatchStage.SEMIFINAL, MatchStage.FINAL -> {
                if (leg == null) {
                    return
                }
            }
        }

        val match = Match(
            matchNumber = slot.matchNumber,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            date = date.atTime(slot.startTime),
            stage = stage,
            leg = leg
        )

        _knockoutSchedulingResult.value = null

        if (stage == MatchStage.REGULAR) {
            _regularSchedulingResult.value = null
        } else {
            _knockoutPersistenceFailure.value = false
        }

        viewModelScope.launch {
            when (stage) {
                MatchStage.REGULAR -> {
                    try {
                        matchRepository.scheduleMatch(match)
                        _regularSchedulingResult.value =
                            RegularMatchSchedulingResult.Success
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        _regularSchedulingResult.value =
                            RegularMatchSchedulingResult.Failure
                    }
                }
                MatchStage.SEMIFINAL, MatchStage.FINAL -> {
                    try {
                        _knockoutSchedulingResult.value =
                            scheduleKnockoutMatchUseCase(match)
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        _knockoutPersistenceFailure.value = true
                    }
                }
            }
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
            val match = findMatch(matchId) ?: return@launch

            val updatedMatch = match.copy(
                homeScore = homeScore,
                awayScore = awayScore,
                status = MatchStatus.FINISHED
            )

            matchRepository.updateMatch(updatedMatch)
        }
    }

    private fun selectAdjacentMonth(offset: Int) {
        val state = uiState.value
        val currentMonth = state.selectedMonth ?: return
        val availableMonths = getAvailableMonths(state.groupedMatches)
        val currentIndex = availableMonths.indexOf(currentMonth)

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
            .groupBy { match -> match.date.toLocalDate() }
            .toSortedMap(compareByDescending { date -> date })
            .mapValues { (_, matchesForDate) ->
                matchesForDate.sortedWith(matchComparator)
            }
    }

    private fun getAvailableMonths(
        groupedMatches: Map<LocalDate, List<Match>>
    ): List<YearMonth> {
        return groupedMatches.keys
            .map(YearMonth::from)
            .distinct()
            .sortedDescending()
    }

    private fun resolveSelectedMonth(
        requestedMonth: YearMonth?,
        availableMonths: List<YearMonth>
    ): YearMonth? {
        return requestedMonth
            ?.takeIf { month -> month in availableMonths }
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
        return uiState.value.groupedMatches
            .values
            .asSequence()
            .flatten()
            .find { match -> match.id == matchId }
    }

    private fun isVisibleMatch(match: Match): Boolean {
        return match.date.isAfter(HISTORICAL_MATCHES_CUTOFF)
    }

    private companion object {
        const val OLDER_MONTH_OFFSET = 1
        const val NEWER_MONTH_OFFSET = -1

        val HISTORICAL_MATCHES_CUTOFF = LocalDateTime.of(
            2026, 5, 31, 23, 59
        )

        val matchComparator = compareBy<Match> { match ->
            match.matchNumber ?: Int.MAX_VALUE
        }.thenBy { match -> match.date }
    }
}
