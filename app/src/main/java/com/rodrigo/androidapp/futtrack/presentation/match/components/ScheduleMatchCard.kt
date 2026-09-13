package com.rodrigo.androidapp.futtrack.presentation.match.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.domain.model.MatchSlot
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleMatchCard(
    availableTeams: List<Team>,
    getAvailableMatchSlots: (LocalDate) -> List<MatchSlot>,
    onScheduleMatch: (Team, Team, LocalDate, MatchSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHomeTeamId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var selectedAwayTeamId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var selectedDateValue by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var selectedMatchNumber by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var expandedHome by remember {
        mutableStateOf(false)
    }

    var expandedAway by remember {
        mutableStateOf(false)
    }

    var expandedMatchSlot by remember {
        mutableStateOf(false)
    }

    var showDatePicker by rememberSaveable {
        mutableStateOf(false)
    }

    val teamsById = remember(availableTeams) {
        availableTeams.associateBy(Team::id)
    }

    val selectedHome =
        selectedHomeTeamId?.let(teamsById::get)

    val selectedAway =
        selectedAwayTeamId?.let(teamsById::get)

    val selectedDate =
        selectedDateValue?.let(LocalDate::parse)

    val selectedMatchSlot =
        selectedMatchNumber?.let(
            MatchSlot::fromMatchNumber
        )

    val availableMatchSlots = selectedDate
        ?.let(getAvailableMatchSlots)
        .orEmpty()

    LaunchedEffect(
        availableMatchSlots,
        selectedMatchSlot
    ) {
        if (
            selectedMatchSlot != null &&
            selectedMatchSlot !in availableMatchSlots
        ) {
            selectedMatchNumber = null
        }
    }

    val slotPlaceholder = when {
        selectedDate == null ->
            "Selecione a data primeiro"

        availableMatchSlots.isEmpty() ->
            "Rodada completa"

        else ->
            "Selecione..."
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
        ScheduleDatePickerDialog(
            onDismiss = {
                showDatePicker = false
            },
            onDateSelected = { newDate ->
                if (newDate != selectedDate) {
                    selectedMatchNumber = null
                    expandedMatchSlot = false
                }

                selectedDateValue = newDate.toString()
                showDatePicker = false
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth()
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
                teams = availableTeams,
                selectedTeam = selectedHome,
                expanded = expandedHome,
                onExpandedChange = { expanded ->
                    expandedHome = expanded
                },
                onTeamSelected = { team ->
                    selectedHomeTeamId = team.id
                    expandedHome = false
                }
            )

            TeamDropdown(
                label = "Time Visitante",
                teams = availableTeams,
                selectedTeam = selectedAway,
                expanded = expandedAway,
                onExpandedChange = { expanded ->
                    expandedAway = expanded
                },
                onTeamSelected = { team ->
                    selectedAwayTeamId = team.id
                    expandedAway = false
                }
            )

            MatchDateButton(
                selectedDate = selectedDate,
                onClick = {
                    showDatePicker = true
                }
            )

            MatchSlotDropdown(
                availableSlots = availableMatchSlots,
                selectedSlot = selectedMatchSlot,
                placeholder = slotPlaceholder,
                expanded = expandedMatchSlot,
                enabled = canSelectSlot,
                onExpandedChange = { expanded ->
                    expandedMatchSlot = expanded
                },
                onSlotSelected = { slot ->
                    selectedMatchNumber = slot.matchNumber
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

                        selectedHomeTeamId = null
                        selectedAwayTeamId = null
                        selectedMatchNumber = null
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

@Composable
private fun MatchDateButton(
    selectedDate: LocalDate?,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Calendário",
            modifier = Modifier.padding(
                end = 8.dp
            )
        )

        Text(
            text = selectedDate
                ?.format(MATCH_DATE_FORMATTER)
                ?: "Selecionar Data"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamDropdown(
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
            value = selectedSlot
                ?.displayLabel()
                ?: placeholder,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val datePickerState =
        rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState
                        .selectedDateMillis
                        ?.toLocalDate()
                        ?.let(onDateSelected)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = datePickerState
        )
    }
}

private fun Long.toLocalDate(): LocalDate {
    return Instant
        .ofEpochMilli(this)
        .atZone(ZoneId.of("UTC"))
        .toLocalDate()
}

private fun MatchSlot.displayLabel(): String {
    return "Jogo $matchNumber — ${
        startTime.format(MATCH_TIME_FORMATTER)
    }"
}

@Preview(
    name = "Schedule Match Card",
    showBackground = true
)
@Composable
private fun ScheduleMatchCardPreview() {
    FutTrackTheme {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            ScheduleMatchCard(
                availableTeams = PREVIEW_TEAMS,
                getAvailableMatchSlots = {
                    MatchSlot.entries
                },
                onScheduleMatch = { _, _, _, _ -> }
            )
        }
    }
}

private val MATCH_DATE_FORMATTER =
    DateTimeFormatter.ofPattern(
        "dd/MM/yyyy"
    )

private val MATCH_TIME_FORMATTER =
    DateTimeFormatter.ofPattern(
        "HH:mm"
    )

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
        ),
        Team(
            id = "team_alemanha",
            name = "Alemanha",
            isoCode = "DE"
        )
    )