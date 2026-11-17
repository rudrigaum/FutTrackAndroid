package com.rodrigo.androidapp.futtrack.presentation.standings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchLeg
import com.rodrigo.androidapp.futtrack.domain.model.MatchStage
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import com.rodrigo.androidapp.futtrack.domain.usecase.KnockoutProgression
import com.rodrigo.androidapp.futtrack.domain.usecase.KnockoutTieResult
import com.rodrigo.androidapp.futtrack.domain.usecase.KnockoutVictoryReason
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun KnockoutProgressionCard(
    progression: KnockoutProgression,
    standings: List<TeamStanding>,
    matches: List<Match> = emptyList(),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressionHeader()
            SemifinalOutcome(progression.semifinalResult, standings, matches)
            HorizontalDivider()
            FinalOutcome(progression, standings, matches)
        }
    }
}

@Composable
private fun ProgressionHeader() {
    Text(
        text = "Resultados do mata-mata",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SemifinalOutcome(
    result: KnockoutTieResult?,
    standings: List<TeamStanding>,
    matches: List<Match>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StageTitle("Semifinal")
        if (result == null) {
            StatusText("Final direta: não haverá semifinal")
        } else {
            val participants = standings.drop(1).take(2)
                .map { it.team.id }
                .toSet()
            StageLegs(MatchStage.SEMIFINAL, participants, matches, standings)
            StageResult(result, "Aguardando os jogos da semifinal", "Classificado", standings)
        }
    }
}

@Composable
private fun FinalOutcome(
    progression: KnockoutProgression,
    standings: List<TeamStanding>,
    matches: List<Match>
) {
    val firstPlaceTeam = standings.firstOrNull()?.team
    val opponentId = progression.finalOpponentTeamId
    val opponentName = opponentId?.let { teamName(it, standings) } ?: "A definir"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StageTitle("Final")
        if (firstPlaceTeam != null) {
            Text(
                text = "${firstPlaceTeam.name} × $opponentName",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (firstPlaceTeam != null && opponentId != null) {
            StageLegs(
                MatchStage.FINAL,
                setOf(firstPlaceTeam.id, opponentId),
                matches,
                standings
            )
        }
        FinalResult(progression.finalResult, standings, opponentId != null)
    }
}

@Composable
private fun FinalResult(
    result: KnockoutTieResult,
    standings: List<TeamStanding>,
    hasOpponent: Boolean
) {
    val pendingMessage = if (hasOpponent) {
        "Aguardando os jogos da final"
    } else {
        "Aguardando o vencedor da semifinal"
    }
    StageResult(result, pendingMessage, "Campeão", standings)
}

@Composable
private fun StageResult(
    result: KnockoutTieResult,
    pendingMessage: String,
    winnerLabel: String,
    standings: List<TeamStanding>
) {
    when (result) {
        KnockoutTieResult.Pending -> StatusText(pendingMessage)
        KnockoutTieResult.RequiresTiebreaker ->
            StatusText("Campanhas empatadas: é necessário outro critério de desempate")
        is KnockoutTieResult.Decided ->
            WinnerDetails(winnerLabel, result, standings)
    }
}

@Composable
private fun StageLegs(
    stage: MatchStage,
    expectedTeams: Set<String>,
    matches: List<Match>,
    standings: List<TeamStanding>
) {
    if (expectedTeams.size != 2) return

    val stageMatches = matches.filter { it.stage == stage }
    val firstLeg = stageMatches.singleOrNull { it.leg == MatchLeg.FIRST }
        ?.takeIf { setOf(it.homeTeamId, it.awayTeamId) == expectedTeams }
    val secondLeg = stageMatches.singleOrNull { it.leg == MatchLeg.SECOND }
        ?.takeIf { setOf(it.homeTeamId, it.awayTeamId) == expectedTeams }

    if (firstLeg == null && secondLeg == null) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        firstLeg?.let { LegScoreRow("Ida", it, standings) }
        secondLeg?.let { LegScoreRow("Volta", it, standings) }
    }
}

@Composable
private fun LegScoreRow(
    label: String,
    match: Match,
    standings: List<TeamStanding>
) {
    val homeName = teamName(match.homeTeamId, standings)
    val awayName = teamName(match.awayTeamId, standings)
    val score = if (
        match.status == MatchStatus.FINISHED &&
        match.homeScore != null &&
        match.awayScore != null
    ) {
        "$homeName ${match.homeScore} × ${match.awayScore} $awayName"
    } else {
        "$homeName × $awayName (a disputar)"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = score,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = match.date.format(MATCH_DATE_TIME_FORMATTER),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WinnerDetails(
    winnerLabel: String,
    result: KnockoutTieResult.Decided,
    standings: List<TeamStanding>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$winnerLabel: ${teamName(result.winnerTeamId, standings)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        AggregateText(result, standings)
        VictoryReasonText(result.reason)
    }
}

@Composable
private fun StatusText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun StageTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun AggregateText(
    result: KnockoutTieResult.Decided,
    standings: List<TeamStanding>
) {
    Text(
        text = "Agregado: ${teamName(result.firstTeamId, standings)} " +
                "${result.firstTeamAggregateGoals} × ${result.secondTeamAggregateGoals} " +
                teamName(result.secondTeamId, standings),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun VictoryReasonText(reason: KnockoutVictoryReason) {
    val description = when (reason) {
        KnockoutVictoryReason.AGGREGATE_SCORE -> "Decisão pelo placar agregado"
        KnockoutVictoryReason.BETTER_REGULAR_SEASON ->
            "Decisão pela melhor campanha na fase regular"
    }

    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun teamName(teamId: String, standings: List<TeamStanding>): String =
    standings.firstOrNull { it.team.id == teamId }?.team?.name
        ?: "Time indisponível"

private val MATCH_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
    "dd/MM/yyyy 'às' HH:mm",
    Locale.forLanguageTag("pt-BR")
)

@Preview(name = "Mata-mata - Aguardando", showBackground = true, widthDp = 390)
@Composable
private fun KnockoutProgressionWaitingPreview() {
    FutTrackTheme {
        KnockoutProgressionCard(
            progression = KnockoutProgression(
                semifinalResult = KnockoutTieResult.Pending,
                finalOpponentTeamId = null,
                finalResult = KnockoutTieResult.Pending
            ),
            standings = previewStandings,
            matches = listOf(
                previewSemifinalFirstLeg.copy(
                    homeScore = null,
                    awayScore = null,
                    status = MatchStatus.SCHEDULED
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Mata-mata - Semifinal concluída", showBackground = true, widthDp = 390)
@Composable
private fun KnockoutProgressionSemifinalCompletePreview() {
    FutTrackTheme {
        KnockoutProgressionCard(
            progression = KnockoutProgression(
                semifinalResult = KnockoutTieResult.Decided(
                    winnerTeamId = "germany",
                    loserTeamId = "italy",
                    firstTeamId = "italy",
                    secondTeamId = "germany",
                    firstTeamAggregateGoals = 2,
                    secondTeamAggregateGoals = 6,
                    reason = KnockoutVictoryReason.AGGREGATE_SCORE
                ),
                finalOpponentTeamId = "germany",
                finalResult = KnockoutTieResult.Pending
            ),
            standings = previewStandings,
            matches = previewSemifinalMatches,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Mata-mata - Campeão", showBackground = true, widthDp = 390)
@Composable
private fun KnockoutProgressionChampionPreview() {
    FutTrackTheme {
        KnockoutProgressionCard(
            progression = previewFinishedProgression,
            standings = previewStandings,
            matches = previewChampionMatches,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Mata-mata - Desempate pendente", showBackground = true, widthDp = 390)
@Composable
private fun KnockoutProgressionTiebreakerPreview() {
    FutTrackTheme {
        KnockoutProgressionCard(
            progression = KnockoutProgression(
                semifinalResult = KnockoutTieResult.RequiresTiebreaker,
                finalOpponentTeamId = null,
                finalResult = KnockoutTieResult.Pending
            ),
            standings = previewStandings,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Mata-mata - Final direta", showBackground = true, widthDp = 390)
@Composable
private fun KnockoutProgressionDirectFinalPreview() {
    FutTrackTheme {
        KnockoutProgressionCard(
            progression = KnockoutProgression(
                semifinalResult = null,
                finalOpponentTeamId = "italy",
                finalResult = KnockoutTieResult.Pending
            ),
            standings = previewStandings,
            modifier = Modifier.padding(16.dp)
        )
    }
}

private val previewStandings = listOf(
    TeamStanding(team = Team("brazil", "Brasil", "BR"), points = 30),
    TeamStanding(team = Team("italy", "Itália", "IT"), points = 25),
    TeamStanding(team = Team("germany", "Alemanha", "DE"), points = 20)
)

private val previewSemifinalFirstLeg = Match(
    id = "semifinal-first",
    homeTeamId = "italy",
    awayTeamId = "germany",
    homeScore = 1,
    awayScore = 2,
    date = LocalDateTime.of(2026, 11, 21, 9, 0),
    status = MatchStatus.FINISHED,
    stage = MatchStage.SEMIFINAL,
    leg = MatchLeg.FIRST
)

private val previewSemifinalMatches = listOf(
    previewSemifinalFirstLeg,
    Match(
        id = "semifinal-second",
        homeTeamId = "germany",
        awayTeamId = "italy",
        homeScore = 4,
        awayScore = 1,
        date = LocalDateTime.of(2026, 11, 28, 9, 0),
        status = MatchStatus.FINISHED,
        stage = MatchStage.SEMIFINAL,
        leg = MatchLeg.SECOND
    )
)

private val previewChampionMatches = listOf(
    previewSemifinalFirstLeg.copy(homeScore = 1, awayScore = 1),
    previewSemifinalMatches.last().copy(homeScore = 1, awayScore = 1),
    Match(
        id = "final-first",
        homeTeamId = "brazil",
        awayTeamId = "italy",
        homeScore = 2,
        awayScore = 1,
        date = LocalDateTime.of(2026, 12, 5, 9, 0),
        status = MatchStatus.FINISHED,
        stage = MatchStage.FINAL,
        leg = MatchLeg.FIRST
    ),
    Match(
        id = "final-second",
        homeTeamId = "italy",
        awayTeamId = "brazil",
        homeScore = 2,
        awayScore = 1,
        date = LocalDateTime.of(2026, 12, 12, 9, 0),
        status = MatchStatus.FINISHED,
        stage = MatchStage.FINAL,
        leg = MatchLeg.SECOND
    )
)

private val previewFinishedProgression = KnockoutProgression(
    semifinalResult = KnockoutTieResult.Decided(
        winnerTeamId = "italy",
        loserTeamId = "germany",
        firstTeamId = "italy",
        secondTeamId = "germany",
        firstTeamAggregateGoals = 2,
        secondTeamAggregateGoals = 2,
        reason = KnockoutVictoryReason.BETTER_REGULAR_SEASON
    ),
    finalOpponentTeamId = "italy",
    finalResult = KnockoutTieResult.Decided(
        winnerTeamId = "brazil",
        loserTeamId = "italy",
        firstTeamId = "brazil",
        secondTeamId = "italy",
        firstTeamAggregateGoals = 3,
        secondTeamAggregateGoals = 3,
        reason = KnockoutVictoryReason.BETTER_REGULAR_SEASON
    )
)
