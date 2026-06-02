package com.rodrigo.androidapp.futtrack.presentation.match

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.core.AppConfig
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.rodrigo.androidapp.futtrack.R

@Composable
fun getTeamCrest(teamId: String): Int {
    return when (teamId) {
        "team_brasil" -> R.drawable.ic_escudo_brasil
        "team_italia" -> R.drawable.ic_escudo_italia
        "team_alemanha" -> R.drawable.ic_escudo_alemanha
        else -> R.drawable.ic_escudo_brasil
    }
}

@Composable
fun MatchRoute(
    viewModel: MatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatchScreen(
        uiState = uiState,
        onScheduleMatch = { home, away, date ->
            viewModel.scheduleNewMatch(home.id, away.id, date)
        },
        onDeleteMatch = { matchId ->
            viewModel.deleteMatch(matchId)
        },
        onFinishMatch = { matchId, homeScore, awayScore ->
            viewModel.finishMatch(matchId, homeScore, awayScore)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MatchScreen(
    uiState: MatchUiState,
    onScheduleMatch: (Team, Team, LocalDateTime) -> Unit,
    onDeleteMatch: (String) -> Unit,
    onFinishMatch: (String, Int, Int) -> Unit
) {
    var expandedHome by remember { mutableStateOf(false) }
    var selectedHome by remember { mutableStateOf<Team?>(null) }
    var expandedAway by remember { mutableStateOf(false) }
    var selectedAway by remember { mutableStateOf<Team?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val datePickerState = rememberDatePickerState()
    var matchToScore by remember { mutableStateOf<Match?>(null) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (AppConfig.IS_ADMIN) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Agendar Novo Jogo", style = MaterialTheme.typography.titleMedium)

                            TeamDropdown("Time Mandante", uiState.availableTeams, selectedHome, expandedHome, { expandedHome = it }) { selectedHome = it; expandedHome = false }
                            TeamDropdown("Time Visitante", uiState.availableTeams, selectedAway, expandedAway, { expandedAway = it }) { selectedAway = it; expandedAway = false }

                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "Calendário", modifier = Modifier.padding(end = 8.dp))
                                Text(selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Selecionar Data")
                            }

                            Button(
                                onClick = {
                                    if (selectedHome != null && selectedAway != null && selectedDate != null) {
                                        val matchDateTime = selectedDate!!.atTime(9, 0)
                                        onScheduleMatch(selectedHome!!, selectedAway!!, matchDateTime)

                                        selectedHome = null
                                        selectedAway = null
                                        selectedDate = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectedHome != null && selectedAway != null && selectedHome != selectedAway && selectedDate != null
                            ) {
                                Text("Agendar")
                            }
                        }
                    }
                }

                Text("Próximos Jogos", style = MaterialTheme.typography.titleMedium)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    uiState.groupedMatches.toSortedMap().forEach { (date, matchesForDate) ->
                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val dateStr = date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale("pt", "BR"))).uppercase()
                                Text(
                                    text = dateStr,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(matchesForDate) { match ->
                            val homeName = uiState.availableTeams.find { it.id == match.homeTeamId }?.name ?: "Desconhecido"
                            val awayName = uiState.availableTeams.find { it.id == match.awayTeamId }?.name ?: "Desconhecido"

                            Box(modifier = Modifier.padding(vertical = 4.dp)) {
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
        }
    }

    matchToScore?.let { match ->
        val homeName = uiState.availableTeams.find { it.id == match.homeTeamId }?.name ?: "Mandante"
        val awayName = uiState.availableTeams.find { it.id == match.awayTeamId }?.name ?: "Visitante"

        ScoreDialog(
            homeName = homeName,
            awayName = awayName,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = getTeamCrest(match.homeTeamId)),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = homeName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (match.status == MatchStatus.FINISHED) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (match.status == MatchStatus.FINISHED) {
                        Text(
                            text = match.homeScore.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = getTeamCrest(match.awayTeamId)),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = awayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (match.status == MatchStatus.FINISHED) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (match.status == MatchStatus.FINISHED) {
                        Text(
                            text = match.awayScore.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (AppConfig.IS_ADMIN) {
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.status == MatchStatus.FINISHED) {
                        IconButton(onClick = onScoreClick) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar Placar", tint = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
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
}

@Composable
fun ScoreDialog(
    homeName: String,
    awayName: String,
    initialHomeScore: Int,
    initialAwayScore: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
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