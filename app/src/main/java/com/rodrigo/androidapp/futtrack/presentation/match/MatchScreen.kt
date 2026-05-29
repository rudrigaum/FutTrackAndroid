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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
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
        onDeleteMatch = { matchId ->
            viewModel.deleteMatch(matchId)
        },
        onFinishMatch = { matchId, homeScore, awayScore ->
            viewModel.finishMatch(matchId, homeScore, awayScore)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(
    uiState: MatchUiState,
    onScheduleMatch: (Team, Team) -> Unit,
    onDeleteMatch: (String) -> Unit,
    onFinishMatch: (String, Int, Int) -> Unit
) {
    var expandedHome by remember { mutableStateOf(false) }
    var selectedHome by remember { mutableStateOf<Team?>(null) }

    var expandedAway by remember { mutableStateOf(false) }
    var selectedAway by remember { mutableStateOf<Team?>(null) }

    var matchToScore by remember { mutableStateOf<Match?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Partidas") }) }
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

                        TeamDropdown("Time Mandante", uiState.availableTeams, selectedHome, expandedHome, { expandedHome = it }) { selectedHome = it; expandedHome = false }
                        TeamDropdown("Time Visitante", uiState.availableTeams, selectedAway, expandedAway, { expandedAway = it }) { selectedAway = it; expandedAway = false }

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

                Text("Jogos", style = MaterialTheme.typography.titleMedium)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(uiState.scheduledMatches) { match ->
                        val homeName = uiState.availableTeams.find { it.id == match.homeTeamId }?.name ?: "Desconhecido"
                        val awayName = uiState.availableTeams.find { it.id == match.awayTeamId }?.name ?: "Desconhecido"

                        MatchItem(
                            match = match,
                            homeName = homeName,
                            awayName = awayName,
                            onDelete = { onDeleteMatch(match.id) },
                            onScoreClick = { matchToScore = match }
                        )
                    }
                }
            }
        }
    }

    matchToScore?.let { match ->
        val homeName = uiState.availableTeams.find { it.id == match.homeTeamId }?.name ?: "Mandante"
        val awayName = uiState.availableTeams.find { it.id == match.awayTeamId }?.name ?: "Visitante"

        ScoreDialog(
            homeName = homeName,
            awayName = awayName,
            // Passamos os gols atuais (se existirem) para pré-preencher o modal
            initialHomeScore = match.homeScore ?: 0,
            initialAwayScore = match.awayScore ?: 0,
            onDismiss = { matchToScore = null },
            onConfirm = { homeScore, awayScore ->
                onFinishMatch(match.id, homeScore, awayScore)
                matchToScore = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDropdown(label: String, teams: List<Team>, selectedTeam: Team?, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, onTeamSelected: (Team) -> Unit) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = selectedTeam?.name ?: "Selecione...", onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            teams.forEach { team -> DropdownMenuItem(text = { Text(team.name) }, onClick = { onTeamSelected(team) }) }
        }
    }
}

@Composable
fun MatchItem(match: Match, homeName: String, awayName: String, onDelete: () -> Unit, onScoreClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (match.status == MatchStatus.FINISHED) {
                    Text(text = "$homeName ${match.homeScore} x ${match.awayScore} $awayName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Finalizado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(text = "$homeName vs $awayName", style = MaterialTheme.typography.bodyLarge)
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    Text(text = match.date.format(formatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (match.status == MatchStatus.FINISHED) {
                    // Ícone de Editar para jogos finalizados
                    IconButton(onClick = onScoreClick) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar Placar")
                    }
                } else {
                    // Botão de lançar placar para jogos em aberto
                    OutlinedButton(onClick = onScoreClick, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Placar")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Deletar Partida", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ScoreDialog(
    homeName: String,
    awayName: String,
    initialHomeScore: Int, // Novo parâmetro
    initialAwayScore: Int, // Novo parâmetro
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    // Inicia o state com os valores que vieram da partida
    var homeScore by remember { mutableIntStateOf(initialHomeScore) }
    var awayScore by remember { mutableIntStateOf(initialAwayScore) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialHomeScore > 0 || initialAwayScore > 0) "Editar Placar" else "Lançar Placar Final") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ScoreInputRow(teamName = homeName, score = homeScore, onScoreChange = { homeScore = it })
                ScoreInputRow(teamName = awayName, score = awayScore, onScoreChange = { awayScore = it })
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(homeScore, awayScore) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun ScoreInputRow(teamName: String, score: Int, onScoreChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(text = teamName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (score > 0) onScoreChange(score - 1) }) { Text("-", style = MaterialTheme.typography.titleLarge) }
            Text(text = score.toString(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
            IconButton(onClick = { onScoreChange(score + 1) }) { Icon(Icons.Default.Add, contentDescription = "Adicionar") }
        }
    }
}