package com.rodrigo.androidapp.futtrack.presentation.match.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamCrest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MatchItem(
    match: Match,
    homeName: String,
    awayName: String,
    isAdminMode: Boolean,
    onDelete: () -> Unit,
    onScoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            MatchDetails(
                match = match,
                homeName = homeName,
                awayName = awayName,
                modifier = Modifier.weight(1f)
            )

            if (isAdminMode) {
                Spacer(modifier = Modifier.width(16.dp))

                MatchAdminActions(
                    isFinished = match.status == MatchStatus.FINISHED,
                    onDelete = onDelete,
                    onScoreClick = onScoreClick
                )
            }
        }
    }
}

@Composable
private fun MatchDetails(
    match: Match,
    homeName: String,
    awayName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        match.matchNumber?.let { matchNumber ->
            MatchHeader(
                matchNumber = matchNumber,
                match = match
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        TeamScoreRow(
            teamId = match.homeTeamId,
            teamName = homeName,
            score = match.homeScore,
            isFinished = match.status == MatchStatus.FINISHED
        )

        Spacer(modifier = Modifier.height(12.dp))

        TeamScoreRow(
            teamId = match.awayTeamId,
            teamName = awayName,
            score = match.awayScore,
            isFinished = match.status == MatchStatus.FINISHED
        )
    }
}

@Composable
private fun MatchHeader(
    matchNumber: Int,
    match: Match
) {
    Text(
        text = "Jogo $matchNumber • ${
            match.date.format(MATCH_TIME_FORMATTER)
        }",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun TeamScoreRow(
    teamId: String,
    teamName: String,
    score: Int?,
    isFinished: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                id = getTeamCrest(teamId)
            ),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = teamName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isFinished) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            modifier = Modifier.weight(1f)
        )

        if (isFinished) {
            Text(
                text = score?.toString() ?: "0",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MatchAdminActions(
    isFinished: Boolean,
    onDelete: () -> Unit,
    onScoreClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isFinished) {
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

@Preview(
    name = "Scheduled Match",
    showBackground = true
)
@Composable
private fun ScheduledMatchPreview() {
    FutTrackTheme {
        MatchItem(
            match = previewMatch(
                status = MatchStatus.SCHEDULED
            ),
            homeName = "Brasil",
            awayName = "Itália",
            isAdminMode = true,
            onDelete = {},
            onScoreClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Finished Match",
    showBackground = true
)
@Composable
private fun FinishedMatchPreview() {
    FutTrackTheme {
        MatchItem(
            match = previewMatch(
                status = MatchStatus.FINISHED,
                homeScore = 2,
                awayScore = 1
            ),
            homeName = "Brasil",
            awayName = "Alemanha",
            isAdminMode = true,
            onDelete = {},
            onScoreClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun previewMatch(
    status: MatchStatus,
    homeScore: Int? = null,
    awayScore: Int? = null
): Match {
    return Match(
        id = "preview-match",
        matchNumber = 1,
        homeTeamId = "team_brasil",
        awayTeamId = "team_italia",
        homeScore = homeScore,
        awayScore = awayScore,
        date = LocalDateTime.of(
            2026,
            9,
            5,
            8,
            30
        ),
        status = status
    )
}

private val MATCH_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("HH:mm")