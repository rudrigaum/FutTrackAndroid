package com.rodrigo.androidapp.futtrack.presentation.standings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.presentation.components.FutTrackTopAppBar
import com.rodrigo.androidapp.futtrack.presentation.standings.components.KnockoutBracketCard
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamCrest

@Composable
fun StandingsRoute(
    viewModel: StandingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StandingsScreen(
        uiState = uiState
    )
}

@Composable
fun StandingsScreen(
    uiState: StandingsUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            FutTrackTopAppBar(
                title = "Classificação"
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            StandingsLoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            StandingsContent(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun StandingsLoadingContent(
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
private fun StandingsContent(
    uiState: StandingsUiState,
    modifier: Modifier = Modifier
) {
    val firstPlaceTeam = uiState.standings
        .firstOrNull()
        ?.team

    val semifinalStatus = uiState.semifinalStatus

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            bottom = 24.dp
        )
    ) {
        item(
            key = STANDINGS_HEADER_KEY
        ) {
            StandingRow(
                position = "#",
                teamId = null,
                teamName = "Time",
                points = "Pts",
                played = "J",
                wins = "V",
                draws = "E",
                losses = "D",
                goalsFor = "GP",
                goalsAgainst = "GC",
                goalDiff = "SG",
                isHeader = true
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        itemsIndexed(
            items = uiState.standings,
            key = { _, standing ->
                standing.team.id
            }
        ) { index, standing ->
            StandingRow(
                position = (index + 1).toString(),
                teamId = standing.team.id,
                teamName = standing.team.name,
                points = standing.points.toString(),
                played = standing.matchesPlayed.toString(),
                wins = standing.wins.toString(),
                draws = standing.draws.toString(),
                losses = standing.losses.toString(),
                goalsFor = standing.goalsFor.toString(),
                goalsAgainst = standing.goalsAgainst.toString(),
                goalDiff = standing.goalDifference.toString(),
                isHeader = false
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        if (firstPlaceTeam != null && semifinalStatus != null) {
            item(
                key = KNOCKOUT_BRACKET_KEY
            ) {
                KnockoutBracketCard(
                    firstPlaceTeam = firstPlaceTeam,
                    status = semifinalStatus,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 20.dp,
                        end = 16.dp
                    )
                )
            }
        }
    }
}


@Composable
fun StandingRow(
    position: String,
    teamId: String?,
    teamName: String,
    points: String,
    played: String,
    wins: String,
    draws: String,
    losses: String,
    goalsFor: String,
    goalsAgainst: String,
    goalDiff: String,
    isHeader: Boolean,
    modifier: Modifier = Modifier
) {
    val teamTextStyle = if (isHeader) {
        MaterialTheme.typography.labelMedium
    } else {
        MaterialTheme.typography.bodyMedium
    }

    val defaultFontWeight = if (isHeader) {
        FontWeight.Bold
    } else {
        FontWeight.Normal
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompact = maxWidth < 360.dp
        val teamWidth = if (isCompact) 102.dp else 108.dp
        val statisticTextStyle = if (isCompact) {
            MaterialTheme.typography.labelSmall
        } else {
            MaterialTheme.typography.bodySmall
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isHeader) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
                .padding(horizontal = 4.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PositionCell(
                text = position,
                style = statisticTextStyle,
                fontWeight = defaultFontWeight
            )

            Spacer(modifier = Modifier.width(2.dp))

            TeamCell(
                teamId = teamId,
                teamName = teamName,
                textStyle = teamTextStyle,
                modifier = Modifier.width(teamWidth)
            )

            Spacer(modifier = Modifier.width(2.dp))

            StatisticsRow(
                points = points,
                played = played,
                wins = wins,
                draws = draws,
                losses = losses,
                goalsFor = goalsFor,
                goalsAgainst = goalsAgainst,
                goalDiff = goalDiff,
                textStyle = statisticTextStyle,
                defaultFontWeight = defaultFontWeight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PositionCell(
    text: String,
    style: TextStyle,
    fontWeight: FontWeight
) {
    Text(
        text = text,
        modifier = Modifier.width(16.dp),
        style = style,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

@Composable
private fun TeamCell(
    teamId: String?,
    teamName: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (teamId != null) {
            Image(
                painter = painterResource(id = getTeamCrest(teamId)),
                contentDescription = null,
                modifier = Modifier
                    .size(26.dp)
                    .padding(end = 6.dp),
                contentScale = ContentScale.Fit
            )
        }

        Text(
            text = teamName,
            style = textStyle,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatisticsRow(
    points: String,
    played: String,
    wins: String,
    draws: String,
    losses: String,
    goalsFor: String,
    goalsAgainst: String,
    goalDiff: String,
    textStyle: TextStyle,
    defaultFontWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatisticCell(points, textStyle, FontWeight.Bold, Modifier.weight(1f))
        StatisticCell(played, textStyle, defaultFontWeight, Modifier.weight(1f))
        StatisticCell(wins, textStyle, defaultFontWeight, Modifier.weight(1f))
        StatisticCell(draws, textStyle, defaultFontWeight, Modifier.weight(1f))
        StatisticCell(losses, textStyle, defaultFontWeight, Modifier.weight(1f))
        StatisticCell(goalsFor, textStyle, defaultFontWeight, Modifier.weight(1f))
        StatisticCell(goalsAgainst, textStyle, defaultFontWeight, Modifier.weight(1f))
        StatisticCell(
            text = goalDiff,
            style = textStyle,
            fontWeight = defaultFontWeight,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatisticCell(
    text: String,
    style: TextStyle,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip
    )
}

@Preview(
    name = "Standing Row - Three Digits",
    showBackground = true,
    widthDp = 390
)
@Composable
private fun StandingRowPreview() {
    FutTrackTheme {
        StandingRow(
            position = "1",
            teamId = "team_brasil",
            teamName = "Brasil",
            points = "184",
            played = "108",
            wins = "54",
            draws = "22",
            losses = "32",
            goalsFor = "112",
            goalsAgainst = "84",
            goalDiff = "42",
            isHeader = false
        )
    }
}

@Preview(
    name = "Standing Row - Compact Width",
    showBackground = true,
    widthDp = 320
)
@Composable
private fun StandingRowCompactPreview() {
    FutTrackTheme {
        StandingRow(
            position = "3",
            teamId = "team_alemanha",
            teamName = "Alemanha",
            points = "122",
            played = "108",
            wins = "35",
            draws = "17",
            losses = "56",
            goalsFor = "91",
            goalsAgainst = "134",
            goalDiff = "-43",
            isHeader = false
        )
    }
}

private const val STANDINGS_HEADER_KEY =
    "standings_header"

private const val KNOCKOUT_BRACKET_KEY =
    "knockout_bracket"