package com.rodrigo.androidapp.futtrack.presentation.standings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.SemifinalStatus
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import com.rodrigo.androidapp.futtrack.domain.repository.StandingBaselineRepository
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import com.rodrigo.androidapp.futtrack.domain.usecase.CalculateSemifinalStatusUseCase
import com.rodrigo.androidapp.futtrack.domain.usecase.CalculateStandingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import javax.inject.Inject

data class StandingsUiState(
    val isLoading: Boolean = true,
    val standings: List<TeamStanding> = emptyList(),
    val semifinalStatus: SemifinalStatus? = null
)

@HiltViewModel
class StandingsViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository,
    private val standingBaselineRepository: StandingBaselineRepository,
    private val calculateStandingsUseCase: CalculateStandingsUseCase,
    private val calculateSemifinalStatusUseCase: CalculateSemifinalStatusUseCase
) : ViewModel() {

    val uiState: StateFlow<StandingsUiState> = combine(
        teamRepository.getTeams(),
        matchRepository.getMatches(),
        standingBaselineRepository.getBaselines()
    ) { teams, matches, baselines ->

        val realMatches = matches.filter { match ->
            !match.date.isBefore(REAL_MATCHES_START_DATE)
        }

        val calculatedStandings = calculateStandingsUseCase(
            teams = teams,
            matches = realMatches,
            baselines = baselines
        )

        StandingsUiState(
            isLoading = false,
            standings = calculatedStandings,
            semifinalStatus = calculateSemifinalStatus(
                standings = calculatedStandings
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StandingsUiState()
    )

    private fun calculateSemifinalStatus(
        standings: List<TeamStanding>
    ): SemifinalStatus? {
        val secondPlace = standings.getOrNull(SECOND_PLACE_INDEX)
            ?: return null

        val thirdPlace = standings.getOrNull(THIRD_PLACE_INDEX)
            ?: return null

        return calculateSemifinalStatusUseCase(
            secondPlace = secondPlace,
            thirdPlace = thirdPlace
        )
    }

    private companion object {
        const val SECOND_PLACE_INDEX = 1
        const val THIRD_PLACE_INDEX = 2

        val REAL_MATCHES_START_DATE: LocalDateTime =
            LocalDateTime.of(2026, 8, 8, 0, 0)
    }
}