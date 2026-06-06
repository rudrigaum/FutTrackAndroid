package com.rodrigo.androidapp.futtrack.presentation.standings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.R
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamCrest

@Composable
fun StandingsRoute(
    viewModel: StandingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StandingsScreen(uiState = uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(uiState: StandingsUiState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_bal),
                            contentDescription = "Logo BAL",
                            modifier = Modifier.height(40.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Baba Amigos do Lelé",
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
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                StandingRow(
                    position = "#",
                    teamId = null, // O Header não tem escudo
                    teamName = "Time",
                    points = "Pts",
                    played = "J",
                    wins = "V",
                    goalsFor = "GP",
                    goalsAgainst = "GC",
                    goalDiff = "SG",
                    isHeader = true
                )

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                LazyColumn {
                    itemsIndexed(uiState.standings) { index, standing ->
                        val position = index + 1

                        StandingRow(
                            position = position.toString(),
                            teamId = standing.team.id,
                            teamName = standing.team.name,
                            points = standing.points.toString(),
                            played = standing.matchesPlayed.toString(),
                            wins = standing.wins.toString(),
                            goalsFor = standing.goalsFor.toString(),
                            goalsAgainst = standing.goalsAgainst.toString(),
                            goalDiff = standing.goalDifference.toString(),
                            isHeader = false
                        )
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
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
    goalsFor: String,
    goalsAgainst: String,
    goalDiff: String,
    isHeader: Boolean
) {
    val textStyle = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium
    val fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = position, modifier = Modifier.width(20.dp), style = textStyle, fontWeight = fontWeight, textAlign = TextAlign.Center)

        Row(
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (teamId != null) {
                Image(
                    painter = painterResource(id = getTeamCrest(teamId)),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp).padding(end = 6.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = teamName,
                style = textStyle,
                fontWeight = if (isHeader) fontWeight else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(text = points, modifier = Modifier.width(26.dp), style = textStyle, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(text = played, modifier = Modifier.width(22.dp), style = textStyle, fontWeight = fontWeight, textAlign = TextAlign.Center)
        Text(text = wins, modifier = Modifier.width(22.dp), style = textStyle, fontWeight = fontWeight, textAlign = TextAlign.Center)
        Text(text = goalsFor, modifier = Modifier.width(22.dp), style = textStyle, fontWeight = fontWeight, textAlign = TextAlign.Center)
        Text(text = goalsAgainst, modifier = Modifier.width(22.dp), style = textStyle, fontWeight = fontWeight, textAlign = TextAlign.Center)
        Text(text = goalDiff, modifier = Modifier.width(26.dp), style = textStyle, fontWeight = fontWeight, textAlign = TextAlign.Center)
    }
}