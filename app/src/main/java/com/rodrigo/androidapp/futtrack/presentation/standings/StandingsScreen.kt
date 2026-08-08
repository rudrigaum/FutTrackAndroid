package com.rodrigo.androidapp.futtrack.presentation.standings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamCrest

@Composable
fun StandingsRoute(
    viewModel: StandingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StandingsScreen(uiState = uiState)
}

@Composable
fun StandingsScreen(
    uiState: StandingsUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            FutTrackTopAppBar(title = "Baba Amigos do Lelé")
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
    Column(modifier = modifier) {
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

        LazyColumn {
            itemsIndexed(
                items = uiState.standings,
                key = { _, standing -> standing.team.id }
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
    val textStyle = if (isHeader) {
        MaterialTheme.typography.labelMedium
    } else {
        MaterialTheme.typography.bodyMedium
    }

    val fontWeight = if (isHeader) {
        FontWeight.Bold
    } else {
        FontWeight.Normal
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isHeader) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .padding(
                horizontal = 4.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatisticCell(
            text = position,
            width = 18.dp,
            style = textStyle,
            fontWeight = fontWeight
        )

        TeamCell(
            teamId = teamId,
            teamName = teamName,
            isHeader = isHeader,
            textStyle = textStyle,
            modifier = Modifier.weight(1f)
        )

        StatisticCell(
            text = points,
            width = 28.dp,
            style = textStyle,
            fontWeight = FontWeight.Bold
        )

        StatisticCell(
            text = played,
            width = 20.dp,
            style = textStyle,
            fontWeight = fontWeight
        )

        StatisticCell(
            text = wins,
            width = 20.dp,
            style = textStyle,
            fontWeight = fontWeight
        )

        StatisticCell(
            text = draws,
            width = 20.dp,
            style = textStyle,
            fontWeight = fontWeight
        )

        StatisticCell(
            text = losses,
            width = 20.dp,
            style = textStyle,
            fontWeight = fontWeight
        )

        StatisticCell(
            text = goalsFor,
            width = 22.dp,
            style = textStyle,
            fontWeight = fontWeight
        )

        StatisticCell(
            text = goalsAgainst,
            width = 22.dp,
            style = textStyle,
            fontWeight = fontWeight
        )

        StatisticCell(
            text = goalDiff,
            width = 28.dp,
            style = textStyle,
            fontWeight = fontWeight
        )
    }
}

@Composable
private fun TeamCell(
    teamId: String?,
    teamName: String,
    isHeader: Boolean,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
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
private fun StatisticCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    style: TextStyle,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.width(width),
        style = style,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

@Preview(
    name = "Standing Row",
    showBackground = true,
    widthDp = 390
)
@Composable
private fun StandingRowPreview() {
    FutTrackTheme {
        StandingRow(
            position = "1",
            teamId = null,
            teamName = "Brasil",
            points = "99",
            played = "60",
            wins = "29",
            draws = "12",
            losses = "19",
            goalsFor = "58",
            goalsAgainst = "44",
            goalDiff = "14",
            isHeader = false
        )
    }
}