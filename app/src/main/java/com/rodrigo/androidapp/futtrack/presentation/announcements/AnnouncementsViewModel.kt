package com.rodrigo.androidapp.futtrack.presentation.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.Announcement
import com.rodrigo.androidapp.futtrack.domain.usecase.ObserveAnnouncementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface AnnouncementsUiState {

    data object Loading : AnnouncementsUiState

    data class Success(
        val announcements: List<Announcement>
    ) : AnnouncementsUiState

    data object Error : AnnouncementsUiState
}

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    observeAnnouncementsUseCase: ObserveAnnouncementsUseCase
) : ViewModel() {

    val uiState: StateFlow<AnnouncementsUiState> =
        observeAnnouncementsUseCase()
            .map<List<Announcement>, AnnouncementsUiState> { announcements ->
                AnnouncementsUiState.Success(announcements)
            }
            .catch { error ->
                if (error is CancellationException) {
                    throw error
                }

                emit(AnnouncementsUiState.Error)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AnnouncementsUiState.Loading
            )
}