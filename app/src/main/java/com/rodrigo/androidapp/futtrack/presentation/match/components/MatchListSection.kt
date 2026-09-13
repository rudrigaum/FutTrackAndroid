package com.rodrigo.androidapp.futtrack.presentation.match.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MatchListSection(
    selectedMonth: YearMonth?,
    groupedMatches: Map<LocalDate, List<Match>>,
    availableTeams: List<Team>,
    isAdminMode: Boolean,
    canSelectOlderMonth: Boolean,
    canSelectNewerMonth: Boolean,
    onOlderMonthClick: () -> Unit,
    onNewerMonthClick: () -> Unit,
    onDeleteMatch: (String) -> Unit,
    onScoreClick: (Match) -> Unit,
    modifier: Modifier = Modifier,
    headerContent: (@Composable () -> Unit)? = null
) {
    var expandedDates by rememberSaveable(
        stateSaver = expandedDatesSaver
    ) {
        mutableStateOf<Set<LocalDate>>(emptySet())
    }

    val groupedDates = groupedMatches.keys.toList()

    val teamsById = remember(availableTeams) {
        availableTeams.associateBy(Team::id)
    }

    LaunchedEffect(
        selectedMonth,
        groupedDates
    ) {
        expandedDates = resolveExpandedDates(
            currentExpandedDates = expandedDates,
            groupedDates = groupedDates
        )
    }

    MatchListContent(
        selectedMonth = selectedMonth,
        groupedMatches = groupedMatches,
        teamsById = teamsById,
        expandedDates = expandedDates,
        isAdminMode = isAdminMode,
        canSelectOlderMonth = canSelectOlderMonth,
        canSelectNewerMonth = canSelectNewerMonth,
        onOlderMonthClick = onOlderMonthClick,
        onNewerMonthClick = onNewerMonthClick,
        onToggleDate = { date ->
            expandedDates = expandedDates.toggle(date)
        },
        onDeleteMatch = onDeleteMatch,
        onScoreClick = onScoreClick,
        headerContent = headerContent,
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MatchListContent(
    selectedMonth: YearMonth?,
    groupedMatches: Map<LocalDate, List<Match>>,
    teamsById: Map<String, Team>,
    expandedDates: Set<LocalDate>,
    isAdminMode: Boolean,
    canSelectOlderMonth: Boolean,
    canSelectNewerMonth: Boolean,
    onOlderMonthClick: () -> Unit,
    onNewerMonthClick: () -> Unit,
    onToggleDate: (LocalDate) -> Unit,
    onDeleteMatch: (String) -> Unit,
    onScoreClick: (Match) -> Unit,
    headerContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        headerContent?.let { content ->
            item(
                key = "header-content"
            ) {
                Box(
                    modifier = Modifier.padding(
                        bottom = 16.dp
                    )
                ) {
                    content()
                }
            }
        }

        item(
            key = "matches-title"
        ) {
            Text(
                text = "Próximos Jogos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    bottom = 16.dp
                )
            )
        }

        selectedMonth?.let { month ->
            item(
                key = "month-selector"
            ) {
                MatchMonthSelector(
                    selectedMonth = month,
                    canSelectOlderMonth = canSelectOlderMonth,
                    canSelectNewerMonth = canSelectNewerMonth,
                    onOlderMonthClick = onOlderMonthClick,
                    onNewerMonthClick = onNewerMonthClick,
                    modifier = Modifier.padding(
                        bottom = 16.dp
                    )
                )
            }
        }

        groupedMatches.forEach { (date, matchesForDate) ->
            val isExpanded = date in expandedDates

            stickyHeader(
                key = "header-$date"
            ) {
                MatchDateHeader(
                    date = date,
                    matchCount = matchesForDate.size,
                    isExpanded = isExpanded,
                    onToggle = {
                        onToggleDate(date)
                    }
                )
            }

            if (isExpanded) {
                items(
                    items = matchesForDate,
                    key = Match::id
                ) { match ->
                    MatchListItem(
                        match = match,
                        teamsById = teamsById,
                        isAdminMode = isAdminMode,
                        onDeleteMatch = onDeleteMatch,
                        onScoreClick = onScoreClick
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchListItem(
    match: Match,
    teamsById: Map<String, Team>,
    isAdminMode: Boolean,
    onDeleteMatch: (String) -> Unit,
    onScoreClick: (Match) -> Unit
) {
    val homeName =
        teamsById[match.homeTeamId]?.name ?: "Desconhecido"

    val awayName =
        teamsById[match.awayTeamId]?.name ?: "Desconhecido"

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
                onScoreClick(match)
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
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
            horizontalArrangement =
                androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MatchDateHeaderText(
                date = date,
                matchCount = matchCount
            )

            MatchDateHeaderIcon(
                isExpanded = isExpanded
            )
        }
    }
}

@Composable
private fun MatchDateHeaderText(
    date: LocalDate,
    matchCount: Int
) {
    Column {
        Text(
            text = date
                .format(MATCH_DATE_FORMATTER)
                .uppercase(PORTUGUESE_BRAZIL),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = matchCount.displayMatchCount(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MatchDateHeaderIcon(
    isExpanded: Boolean
) {
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

private fun resolveExpandedDates(
    currentExpandedDates: Set<LocalDate>,
    groupedDates: List<LocalDate>
): Set<LocalDate> {
    return currentExpandedDates.intersect(
        groupedDates.toSet()
    )
}

private fun Set<LocalDate>.toggle(
    date: LocalDate
): Set<LocalDate> {
    return if (date in this) {
        this - date
    } else {
        this + date
    }
}

private fun Int.displayMatchCount(): String {
    return "$this ${
        if (this == 1) {
            "jogo"
        } else {
            "jogos"
        }
    }"
}

@Preview(
    name = "Match List Section",
    showBackground = true,
    heightDp = 700
)
@Composable
private fun MatchListSectionPreview() {
    FutTrackTheme {
        Surface {
            MatchListSection(
                selectedMonth = YearMonth.of(
                    2026,
                    9
                ),
                groupedMatches = PREVIEW_MATCHES,
                availableTeams = PREVIEW_TEAMS,
                isAdminMode = false,
                canSelectOlderMonth = true,
                canSelectNewerMonth = false,
                onOlderMonthClick = {},
                onNewerMonthClick = {},
                onDeleteMatch = {},
                onScoreClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
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

private val PORTUGUESE_BRAZIL =
    Locale.forLanguageTag("pt-BR")

private val MATCH_DATE_FORMATTER =
    DateTimeFormatter.ofPattern(
        "EEEE, dd/MM/yyyy",
        PORTUGUESE_BRAZIL
    )

private val PREVIEW_DATE =
    LocalDate.of(
        2026,
        9,
        5
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
        )
    )

private val PREVIEW_MATCHES =
    mapOf(
        PREVIEW_DATE to listOf(
            Match(
                id = "preview-match-1",
                matchNumber = 1,
                homeTeamId = "team_brasil",
                awayTeamId = "team_italia",
                date = LocalDateTime.of(
                    2026,
                    9,
                    5,
                    8,
                    30
                )
            )
        )
    )