package com.rodrigo.androidapp.futtrack.presentation.match

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.Team
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MatchRoute(
    viewModel: MatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatchScreen(
        uiState = uiState,
        onScheduleMatch = { home, away ->
            val tomorrow = LocalDateTime.now().plusDays(1)
            viewModel.scheduleNewMatch(home.id, away.id, tomorrow)
        },
        // Nova action passada da UI para a ViewModel
        onDeleteMatch = { matchId ->
            viewModel.deleteMatch(matchId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(
    uiState: MatchUiState,
    onScheduleMatch: (Team, Team) -> Unit,
    onDeleteMatch: (String) -> Unit // Novo parâmetro
) {
    var expandedHome by remember { mutableStateOf(false) }
    var selectedHome by remember { mutableStateOf<Team?>(null) }

    var expandedAway by remember { mutableStateOf(false) }
    var selectedAway by remember { mutableStateOf<Team?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Partidas") })
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Agendar Novo Jogo", style = MaterialTheme.typography.titleMedium)

                        TeamDropdown(
                            label = "Time Mandante",
                            teams = uiState.availableTeams,
                            selectedTeam = selectedHome,
                            expanded = expandedHome,
                            onExpandedChange = { expandedHome = it },
                            onTeamSelected = { selectedHome = it; expandedHome = false }
                        )

                        TeamDropdown(
                            label = "Time Visitante",
                            teams = uiState.availableTeams,
                            selectedTeam = selectedAway,
                            expanded = expandedAway,
                            onExpandedChange = { expandedAway = it },
                            onTeamSelected = { selectedAway = it; expandedAway = false }
                        )

                        Button(
                            onClick = {
                                if (selectedHome != null && selectedAway != null) {
                                    onScheduleMatch(selectedHome!!, selectedAway!!)
                                    selectedHome = null
                                    selectedAway = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedHome != null && selectedAway != null && selectedHome != selectedAway
                        ) {
                            Text("Agendar")
                        }
                    }
                }

                Text("Próximos Jogos", style = MaterialTheme.typography.titleMedium)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.scheduledMatches) { match ->
                        val homeName = uiState.availableTeams.find { it.id == match.homeTeamId }?.name ?: "Desconhecido"
                        val awayName = uiState.availableTeams.find { it.id == match.awayTeamId }?.name ?: "Desconhecido"

                        MatchItem(
                            match = match,
                            homeName = homeName,
                            awayName = awayName,
                            // Conecta o clique da lixeira à action da tela
                            onDelete = { onDeleteMatch(match.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDropdown(
    label: String,
    teams: List<Team>,
    selectedTeam: Team?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTeamSelected: (Team) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedTeam?.name ?: "Selecione...",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            teams.forEach { team ->
                DropdownMenuItem(
                    text = { Text(team.name) },
                    onClick = { onTeamSelected(team) }
                )
            }
        }
    }
}

@Composable
fun MatchItem(
    match: Match,
    homeName: String,
    awayName: String,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = homeName, style = MaterialTheme.typography.bodyLarge)
                    Text(text = " vs ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = awayName, style = MaterialTheme.typography.bodyLarge)
                }

                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                Text(
                    text = match.date.format(formatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Deletar Partida",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}