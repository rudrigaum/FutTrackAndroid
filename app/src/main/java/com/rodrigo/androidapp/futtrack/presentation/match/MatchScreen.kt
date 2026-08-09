package com.rodrigo.androidapp.futtrack.presentation.match

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.presentation.auth.AuthViewModel
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamCrest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        onFinishMatch = viewModel::finishMatch
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun MatchScreen(
    uiState: MatchUiState,
    isAdminMode: Boolean,
    getAvailableMatchSlots: (LocalDate) -> List<MatchSlot>,
    onScheduleMatch: (Team, Team, LocalDate, MatchSlot) -> Unit,
    onDeleteMatch: (String) -> Unit,
    onFinishMatch: (String, Int, Int) -> Unit
) {
    var expandedHome by remember { mutableStateOf(false) }
    var selectedHome by remember { mutableStateOf<Team?>(null) }

    var expandedAway by remember { mutableStateOf(false) }
    var selectedAway by remember { mutableStateOf<Team?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    var expandedMatchSlot by remember { mutableStateOf(false) }
    var selectedMatchSlot by remember { mutableStateOf<MatchSlot?>(null) }

    var expandedDates by rememberSaveable(
        stateSaver = expandedDatesSaver
    ) {
        mutableStateOf<Set<LocalDate>>(emptySet())
    }

    var hasInitializedExpandedDates by rememberSaveable {
        mutableStateOf(false)
    }

    val datePickerState = rememberDatePickerState()
    var matchToScore by remember { mutableStateOf<Match?>(null) }

    val availableMatchSlots = selectedDate
        ?.let(getAvailableMatchSlots)
        .orEmpty()

    val groupedDates = uiState.groupedMatches.keys.toList()

    LaunchedEffect(groupedDates) {
        when {
            groupedDates.isEmpty() -> {
                expandedDates = emptySet()
                hasInitializedExpandedDates = false
            }

            !hasInitializedExpandedDates -> {
                expandedDates = setOf(groupedDates.first())
                hasInitializedExpandedDates = true
            }

            else -> {
                expandedDates =
                    expandedDates.intersect(groupedDates.toSet())
            }
        }
    }

    LaunchedEffect(
        availableMatchSlots,
        selectedMatchSlot
    ) {
        if (
            selectedMatchSlot != null &&
            selectedMatchSlot !in availableMatchSlots
        ) {
            selectedMatchSlot = null
        }
    }

    val slotPlaceholder = when {
        selectedDate == null -> "Selecione a data primeiro"
        availableMatchSlots.isEmpty() -> "Rodada completa"
        else -> "Selecione..."
    }

    val canSelectSlot =
        selectedDate != null &&
                availableMatchSlots.isNotEmpty()

    val canSchedule =
        selectedHome != null &&
                selectedAway != null &&
                selectedHome != selectedAway &&
                selectedDate != null &&
                selectedMatchSlot != null &&
                selectedMatchSlot in availableMatchSlots

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()

                            if (newDate != selectedDate) {
                                selectedMatchSlot = null
                                expandedMatchSlot = false
                            }

                            selectedDate = newDate
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
                if (isAdminMode) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Agendar Novo Jogo",
                                style = MaterialTheme.typography.titleMedium
                            )

                            TeamDropdown(
                                label = "Time Mandante",
                                teams = uiState.availableTeams,
                                selectedTeam = selectedHome,
                                expanded = expandedHome,
                                onExpandedChange = {
                                    expandedHome = it
                                },
                                onTeamSelected = {
                                    selectedHome = it
                                    expandedHome = false
                                }
                            )

                            TeamDropdown(
                                label = "Time Visitante",
                                teams = uiState.availableTeams,
                                selectedTeam = selectedAway,
                                expanded = expandedAway,
                                onExpandedChange = {
                                    expandedAway = it
                                },
                                onTeamSelected = {
                                    selectedAway = it
                                    expandedAway = false
                                }
                            )

                            OutlinedButton(
                                onClick = {
                                    showDatePicker = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Calendário",
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                Text(
                                    text = selectedDate?.format(
                                        DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy"
                                        )
                                    ) ?: "Selecionar Data"
                                )
                            }

                            MatchSlotDropdown(
                                availableSlots = availableMatchSlots,
                                selectedSlot = selectedMatchSlot,
                                placeholder = slotPlaceholder,
                                expanded = expandedMatchSlot,
                                enabled = canSelectSlot,
                                onExpandedChange = {
                                    expandedMatchSlot = it
                                },
                                onSlotSelected = { slot ->
                                    selectedMatchSlot = slot
                                    expandedMatchSlot = false
                                }
                            )

                            Button(
                                onClick = {
                                    val home = selectedHome
                                    val away = selectedAway
                                    val date = selectedDate
                                    val slot = selectedMatchSlot

                                    if (
                                        home != null &&
                                        away != null &&
                                        home != away &&
                                        date != null &&
                                        slot != null
                                    ) {
                                        onScheduleMatch(
                                            home,
                                            away,
                                            date,
                                            slot
                                        )

                                        selectedHome = null
                                        selectedAway = null
                                        selectedMatchSlot = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = canSchedule
                            ) {
                                Text("Agendar")
                            }
                        }
                    }
                }

                Text(
                    text = "Próximos Jogos",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    uiState.groupedMatches.forEach { (date, matchesForDate) ->
                        val isExpanded = date in expandedDates

                        stickyHeader(
                            key = "header-$date"
                        ) {
                            MatchDateHeader(
                                date = date,
                                matchCount = matchesForDate.size,
                                isExpanded = isExpanded,
                                onToggle = {
                                    expandedDates = if (isExpanded) {
                                        expandedDates - date
                                    } else {
                                        expandedDates + date
                                    }
                                }
                            )
                        }

                        if (isExpanded) {
                            items(
                                items = matchesForDate,
                                key = Match::id
                            ) { match ->
                                val homeName = uiState.availableTeams
                                    .find { team ->
                                        team.id == match.homeTeamId
                                    }
                                    ?.name
                                    ?: "Desconhecido"

                                val awayName = uiState.availableTeams
                                    .find { team ->
                                        team.id == match.awayTeamId
                                    }
                                    ?.name
                                    ?: "Desconhecido"

                                Box(
                                    modifier = Modifier.padding(
                                        vertical = 4.dp
                                    )
                                ) {
                                    MatchItem(
                                        match = match,
                                        homeName = homeName,
                                        awayName = awayName,
                                        isAdminMode = isAdminMode,
                                        onDelete = {
                                            onDeleteMatch(match.id)
                                        },
                                        onScoreClick = {
                                            matchToScore = match
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    matchToScore?.let { match ->
        val homeName = uiState.availableTeams
            .find { team ->
                team.id == match.homeTeamId
            }
            ?.name
            ?: "Mandante"

        val awayName = uiState.availableTeams
            .find { team ->
                team.id == match.awayTeamId
            }
            ?.name
            ?: "Visitante"

        ScoreDialog(
            homeName = homeName,
            awayName = awayName,
            initialHomeScore = match.homeScore ?: 0,
            initialAwayScore = match.awayScore ?: 0,
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
private fun MatchDateHeader(
    date: LocalDate,
    matchCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = date
                        .format(MATCH_DATE_FORMATTER)
                        .uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "$matchCount ${
                        if (matchCount == 1) "jogo" else "jogos"
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = if (isExpanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = if (isExpanded) {
                    "Recolher jogos"
                } else {
                    "Expandir jogos"
                },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            label = {
                Text(label)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            }
        ) {
            teams.forEach { team ->
                DropdownMenuItem(
                    text = {
                        Text(team.name)
                    },
                    onClick = {
                        onTeamSelected(team)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchSlotDropdown(
    availableSlots: List<MatchSlot>,
    selectedSlot: MatchSlot?,
    placeholder: String,
    expanded: Boolean,
    enabled: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSlotSelected: (MatchSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { shouldExpand ->
            if (enabled) {
                onExpandedChange(shouldExpand)
            }
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedSlot?.displayLabel() ?: placeholder,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = {
                Text("Número do Jogo")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            }
        ) {
            availableSlots.forEach { slot ->
                DropdownMenuItem(
                    text = {
                        Text(slot.displayLabel())
                    },
                    onClick = {
                        onSlotSelected(slot)
                    }
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
    isAdminMode: Boolean,
    onDelete: () -> Unit,
    onScoreClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                match.matchNumber?.let { number ->
                    Text(
                        text = "Jogo $number • ${
                            match.date.format(MATCH_TIME_FORMATTER)
                        }",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            id = getTeamCrest(match.homeTeamId)
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = homeName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (
                            match.status == MatchStatus.FINISHED
                        ) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            id = getTeamCrest(match.awayTeamId)
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = awayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (
                            match.status == MatchStatus.FINISHED
                        ) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
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

            if (isAdminMode) {
                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (match.status == MatchStatus.FINISHED) {
                        IconButton(
                            onClick = onScoreClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Placar",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onScoreClick,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Placar")
                        }
                    }

                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Deletar Partida",
                            tint = MaterialTheme.colorScheme.error
                        )
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
    var homeScore by remember {
        mutableIntStateOf(initialHomeScore)
    }

    var awayScore by remember {
        mutableIntStateOf(initialAwayScore)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialHomeScore > 0 || initialAwayScore > 0) {
                    "Editar Placar"
                } else {
                    "Lançar Placar Final"
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScoreInputRow(
                    teamName = homeName,
                    score = homeScore,
                    onScoreChange = {
                        homeScore = it
                    }
                )

                ScoreInputRow(
                    teamName = awayName,
                    score = awayScore,
                    onScoreChange = {
                        awayScore = it
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(homeScore, awayScore)
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ScoreInputRow(
    teamName: String,
    score: Int,
    onScoreChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = teamName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (score > 0) {
                        onScoreChange(score - 1)
                    }
                }
            ) {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            IconButton(
                onClick = {
                    onScoreChange(score + 1)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar"
                )
            }
        }
    }
}

@Preview(
    name = "Expanded Match Date Header",
    showBackground = true
)
@Composable
private fun ExpandedMatchDateHeaderPreview() {
    FutTrackTheme {
        MatchDateHeader(
            date = LocalDate.of(2026, 8, 15),
            matchCount = 6,
            isExpanded = true,
            onToggle = {}
        )
    }
}

@Preview(
    name = "Collapsed Match Date Header",
    showBackground = true
)
@Composable
private fun CollapsedMatchDateHeaderPreview() {
    FutTrackTheme {
        MatchDateHeader(
            date = LocalDate.of(2026, 8, 22),
            matchCount = 6,
            isExpanded = false,
            onToggle = {}
        )
    }
}

@Preview(
    name = "Match Slot Dropdown",
    showBackground = true
)
@Composable
private fun MatchSlotDropdownPreview() {
    FutTrackTheme {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            MatchSlotDropdown(
                availableSlots = MatchSlot.entries,
                selectedSlot = MatchSlot.GAME_3,
                placeholder = "Selecione...",
                expanded = false,
                enabled = true,
                onExpandedChange = {},
                onSlotSelected = {}
            )
        }
    }
}

private fun MatchSlot.displayLabel(): String {
    return "Jogo $matchNumber — ${
        startTime.format(MATCH_TIME_FORMATTER)
    }"
}

private val expandedDatesSaver =
    listSaver<Set<LocalDate>, String>(
        save = { dates ->
            dates.map(LocalDate::toString)
        },
        restore = { savedDates ->
            savedDates
                .map(LocalDate::parse)
                .toSet()
        }
    )

private val MATCH_DATE_FORMATTER =
    DateTimeFormatter.ofPattern(
        "EEEE, dd/MM/yyyy",
        Locale("pt", "BR")
    )

private val MATCH_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("HH:mm")