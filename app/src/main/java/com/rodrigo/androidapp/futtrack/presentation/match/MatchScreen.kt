package com.rodrigo.androidapp.futtrack.presentation.match

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.R
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchSlot
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.presentation.auth.AuthViewModel
import com.rodrigo.androidapp.futtrack.presentation.match.components.MatchListSection
import com.rodrigo.androidapp.futtrack.presentation.match.components.ScheduleMatchCard
import com.rodrigo.androidapp.futtrack.presentation.match.components.ScoreDialog
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MatchRoute(
    viewModel: MatchViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    MatchScreen(
        uiState = uiState,
        isAdminMode = authUiState.isAdminMode,
        getAvailableMatchSlots = viewModel::getAvailableMatchSlots,
        onScheduleMatch = { home, away, date, slot ->
            viewModel.scheduleNewMatch(
                homeTeamId = home.id,
                awayTeamId = away.id,
                date = date,
                slot = slot
            )
        },
        onDeleteMatch = viewModel::deleteMatch,
        onFinishMatch = viewModel::finishMatch,
        onOlderMonthClick = viewModel::selectOlderMonth,
        onNewerMonthClick = viewModel::selectNewerMonth
    )
}

@Composable
fun MatchScreen(
    uiState: MatchUiState,
    isAdminMode: Boolean,
    getAvailableMatchSlots: (LocalDate) -> List<MatchSlot>,
    onScheduleMatch: (Team, Team, LocalDate, MatchSlot) -> Unit,
    onDeleteMatch: (String) -> Unit,
    onFinishMatch: (String, Int, Int) -> Unit,
    onOlderMonthClick: () -> Unit,
    onNewerMonthClick: () -> Unit
) {
    var matchToScore by remember {
        mutableStateOf<Match?>(null)
    }

    Scaffold(
        topBar = {
            MatchTopBar()
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            MatchLoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            MatchContent(
                uiState = uiState,
                isAdminMode = isAdminMode,
                getAvailableMatchSlots = getAvailableMatchSlots,
                onScheduleMatch = onScheduleMatch,
                onDeleteMatch = onDeleteMatch,
                onScoreClick = { match ->
                    matchToScore = match
                },
                onOlderMonthClick = onOlderMonthClick,
                onNewerMonthClick = onNewerMonthClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )
        }
    }

    matchToScore?.let { match ->
        MatchScoreDialog(
            match = match,
            availableTeams = uiState.availableTeams,
            onDismiss = {
                matchToScore = null
            },
            onConfirm = { homeScore, awayScore ->
                onFinishMatch(
                    match.id,
                    homeScore,
                    awayScore
                )

                matchToScore = null
            }
        )
    }
}

@Composable
private fun MatchContent(
    uiState: MatchUiState,
    isAdminMode: Boolean,
    getAvailableMatchSlots: (LocalDate) -> List<MatchSlot>,
    onScheduleMatch: (Team, Team, LocalDate, MatchSlot) -> Unit,
    onDeleteMatch: (String) -> Unit,
    onScoreClick: (Match) -> Unit,
    onOlderMonthClick: () -> Unit,
    onNewerMonthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MatchListSection(
        selectedMonth = uiState.selectedMonth,
        groupedMatches = uiState.filteredGroupedMatches,
        availableTeams = uiState.availableTeams,
        isAdminMode = isAdminMode,
        canSelectOlderMonth = uiState.canSelectOlderMonth,
        canSelectNewerMonth = uiState.canSelectNewerMonth,
        onOlderMonthClick = onOlderMonthClick,
        onNewerMonthClick = onNewerMonthClick,
        onDeleteMatch = onDeleteMatch,
        onScoreClick = onScoreClick,
        modifier = modifier,
        headerContent = if (isAdminMode) {
            {
                ScheduleMatchCard(
                    availableTeams = uiState.availableTeams,
                    getAvailableMatchSlots = getAvailableMatchSlots,
                    onScheduleMatch = onScheduleMatch
                )
            }
        } else {
            null
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchTopBar() {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.logo_bal
                    ),
                    contentDescription = "Logo BAL",
                    modifier = Modifier.height(40.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Text(
                    text = "Calendário e Resultados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun MatchLoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MatchScoreDialog(
    match: Match,
    availableTeams: List<Team>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val teamsById = remember(availableTeams) {
        availableTeams.associateBy(Team::id)
    }

    val homeName =
        teamsById[match.homeTeamId]?.name ?: "Mandante"

    val awayName =
        teamsById[match.awayTeamId]?.name ?: "Visitante"

    ScoreDialog(
        homeName = homeName,
        awayName = awayName,
        initialHomeScore = match.homeScore ?: 0,
        initialAwayScore = match.awayScore ?: 0,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Preview(
    name = "Matches",
    showBackground = true,
    heightDp = 800
)
@Composable
private fun MatchScreenPreview() {
    FutTrackTheme {
        MatchScreen(
            uiState = MatchUiState(
                isLoading = false,
                availableTeams = PREVIEW_TEAMS,
                groupedMatches = emptyMap(),
                filteredGroupedMatches = emptyMap(),
                selectedMonth = YearMonth.of(
                    2026,
                    9
                ),
                canSelectOlderMonth = true,
                canSelectNewerMonth = false
            ),
            isAdminMode = false,
            getAvailableMatchSlots = {
                MatchSlot.entries
            },
            onScheduleMatch = { _, _, _, _ -> },
            onDeleteMatch = {},
            onFinishMatch = { _, _, _ -> },
            onOlderMonthClick = {},
            onNewerMonthClick = {}
        )
    }
}

private val PREVIEW_TEAMS =
    listOf(
        Team(
            id = "team_brasil",
            name = "Brasil",
            isoCode = "BR"
        ),
        Team(
            id = "team_italia",
            name = "Itália",
            isoCode = "IT"
        )
    )