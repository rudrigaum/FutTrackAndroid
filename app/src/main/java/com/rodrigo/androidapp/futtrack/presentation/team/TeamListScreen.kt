package com.rodrigo.androidapp.futtrack.presentation.team

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.ColorFilter
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamJerseyColor
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamNumberColor
import com.rodrigo.androidapp.futtrack.R
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamCrest

@Composable
fun TeamListRoute(
    viewModel: TeamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeamListScreen(
        uiState = uiState,
        onTeamClick = { teamId ->
            viewModel.loadPlayersForTeam(teamId)
        },
        onDismissDialog = {
            viewModel.clearSelectedPlayers()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamListScreen(
    uiState: TeamUiState,
    onTeamClick: (String) -> Unit,
    onDismissDialog: () -> Unit
) {
    var selectedTeam by remember { mutableStateOf<Team?>(null) }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    items(uiState.teams) { team ->
                        TeamItem(
                            team = team,
                            onClick = {
                                selectedTeam = team
                                onTeamClick(team.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedTeam != null) {
        AlertDialog(
            onDismissRequest = {
                selectedTeam = null
                onDismissDialog()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = getTeamCrest(selectedTeam!!.id)),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elenco: ${selectedTeam?.name}")
                }
            },
            text = {
                if (uiState.selectedTeamPlayers.isEmpty()) {
                    Text("Nenhum jogador cadastrado ainda.")
                } else {
                    LazyColumn {
                        items(uiState.selectedTeamPlayers) { player ->
                            val jerseyColor = getTeamJerseyColor(selectedTeam!!.id, player.isGoalkeeper)
                            val numberColor = getTeamNumberColor(selectedTeam!!.id, player.isGoalkeeper)

                            Column {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_camisa),
                                            contentDescription = "Camisa do Jogador",
                                            modifier = Modifier.fillMaxSize(),
                                            colorFilter = ColorFilter.tint(jerseyColor)
                                        )

                                        Text(
                                            text = player.number?.toString() ?: "?",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = numberColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text(
                                        text = "${player.name} ${if (player.isGoalkeeper) "(GOL)" else ""}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedTeam = null
                    onDismissDialog()
                }) {
                    Text("Fechar", color = MaterialTheme.colorScheme.secondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun TeamItem(team: Team, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = getTeamCrest(team.id)),
                contentDescription = "Escudo do ${team.name}",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = team.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}