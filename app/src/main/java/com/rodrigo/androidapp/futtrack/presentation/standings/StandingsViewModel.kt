package com.rodrigo.androidapp.futtrack.presentation.standings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import com.rodrigo.androidapp.futtrack.domain.usecase.CalculateStandingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StandingsUiState(
    val isLoading: Boolean = true,
    val standings: List<TeamStanding> = emptyList()
)

@HiltViewModel
class StandingsViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository,
    private val calculateStandingsUseCase: CalculateStandingsUseCase
) : ViewModel() {


    val uiState: StateFlow<StandingsUiState> = combine(
        teamRepository.getTeams(),
        matchRepository.getMatches()
    ) { teams, matches ->
        val calculatedStandings = calculateStandingsUseCase(teams, matches)

        StandingsUiState(
            isLoading = false,
            standings = calculatedStandings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StandingsUiState(isLoading = true)
    )
}