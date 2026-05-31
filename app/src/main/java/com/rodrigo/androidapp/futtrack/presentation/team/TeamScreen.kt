package com.rodrigo.androidapp.futtrack.presentation.team

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.domain.model.Player
import com.rodrigo.androidapp.futtrack.domain.model.Team

@Composable
fun TeamRoute(
    viewModel: TeamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeamScreen(
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
fun TeamScreen(
    uiState: TeamUiState,
    onTeamClick: (String) -> Unit,
    onDismissDialog: () -> Unit
) {
    // Estado local para sabermos qual time está selecionado no momento
    var selectedTeam by remember { mutableStateOf<Team?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Times") }) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.teams) { team ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTeam = team
                                onTeamClick(team.id)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = team.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal exibindo os jogadores
    if (selectedTeam != null) {
        AlertDialog(
            onDismissRequest = {
                selectedTeam = null
                onDismissDialog()
            },
            title = { Text("Elenco: ${selectedTeam?.name}") },
            text = {
                if (uiState.selectedTeamPlayers.isEmpty()) {
                    Text("Nenhum jogador cadastrado ainda.")
                } else {
                    LazyColumn {
                        items(uiState.selectedTeamPlayers) { player ->
                            Column {
                                Text(
                                    text = "${player.name} ${if (player.isGoalkeeper) "(GOL)" else ""}",
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Divider()
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
                    Text("Fechar")
                }
            }
        )
    }
}