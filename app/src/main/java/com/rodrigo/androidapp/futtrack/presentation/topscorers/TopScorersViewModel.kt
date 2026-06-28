package com.rodrigo.androidapp.futtrack.presentation.topscorers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.Player
import com.rodrigo.androidapp.futtrack.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopScorersUiState(
    val topScorers: List<Player> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TopScorersViewModel @Inject constructor(
    private val repository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopScorersUiState())
    val uiState: StateFlow<TopScorersUiState> = _uiState.asStateFlow()

    init {
        observeTopScorers()
    }

    private fun observeTopScorers() {
        viewModelScope.launch {
            repository.getTopScorers().collect { players ->
                _uiState.update { it.copy(topScorers = players, isLoading = false) }
            }
        }
    }

    fun updatePlayerGoals(playerId: String, currentGoals: Int, isIncrement: Boolean) {
        val newGoals = if (isIncrement) currentGoals + 1 else currentGoals - 1
        if (newGoals < 0) return

        viewModelScope.launch {
            repository.updatePlayerGoals(playerId, newGoals)
        }
    }
}