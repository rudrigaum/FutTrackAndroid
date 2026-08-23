package com.rodrigo.androidapp.futtrack.presentation.standings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.domain.model.SemifinalStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import java.util.Locale

@Composable
fun SemifinalStatusCard(
    status: SemifinalStatus,
    modifier: Modifier = Modifier
) {
    val statusColor = semifinalStatusColor(
        differencePercentage = status.differencePercentage,
        limitPercentage = status.limitPercentage
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "🏆 Situação da semifinal",
                style = MaterialTheme.typography.titleMedium
            )

            SemifinalPercentageSummary(
                differencePercentage = status.differencePercentage,
                limitPercentage = status.limitPercentage
            )

            SemifinalProgress(
                differencePercentage = status.differencePercentage,
                limitPercentage = status.limitPercentage,
                statusColor = statusColor
            )

            SemifinalResult(
                status = status,
                statusColor = statusColor
            )
        }
    }
}

@Composable
private fun SemifinalPercentageSummary(
    differencePercentage: Double,
    limitPercentage: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Diferença: ${formatPercentage(differencePercentage)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Limite: ${formatPercentage(limitPercentage)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SemifinalProgress(
    differencePercentage: Double,
    limitPercentage: Double,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = calculateProgress(
        differencePercentage = differencePercentage,
        limitPercentage = limitPercentage
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PROGRESS_HEIGHT)
                .clip(
                    RoundedCornerShape(
                        PROGRESS_CORNER_RADIUS
                    )
                )
                .background(
                    MaterialTheme.colorScheme.outlineVariant
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(PROGRESS_HEIGHT)
                    .clip(
                        RoundedCornerShape(
                            PROGRESS_CORNER_RADIUS
                        )
                    )
                    .background(statusColor)
            )
        }

        Text(
            text = buildProgressLabel(
                differencePercentage = differencePercentage,
                limitPercentage = limitPercentage
            ),
            style = MaterialTheme.typography.bodySmall,
            color = statusColor
        )
    }
}

@Composable
private fun SemifinalResult(
    status: SemifinalStatus,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    val resultText = if (status.isEligible) {
        "✓ Semifinal mantida — " +
                "${status.secondPlaceTeam.name} x ${status.thirdPlaceTeam.name}"
    } else {
        "✕ Sem semifinal — limite de " +
                "${formatPercentage(status.limitPercentage)} ultrapassado"
    }

    Text(
        text = resultText,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = statusColor
    )
}

private fun calculateProgress(
    differencePercentage: Double,
    limitPercentage: Double
): Float {
    if (limitPercentage <= 0.0) {
        return 0f
    }

    return (differencePercentage / limitPercentage)
        .toFloat()
        .coerceIn(
            minimumValue = 0f,
            maximumValue = 1f
        )
}

private fun semifinalStatusColor(
    differencePercentage: Double,
    limitPercentage: Double
): Color {
    if (limitPercentage <= 0.0) {
        return SAFE_GREEN
    }

    val warningPercentage =
        limitPercentage * WARNING_THRESHOLD

    return when {
        differencePercentage > limitPercentage ->
            LIMIT_RED

        differencePercentage >= warningPercentage ->
            WARNING_ORANGE

        else ->
            SAFE_GREEN
    }
}

private fun buildProgressLabel(
    differencePercentage: Double,
    limitPercentage: Double
): String {
    return "${formatPercentageValue(differencePercentage)} / " +
            "${formatPercentage(limitPercentage)}"
}

private fun formatPercentage(
    value: Double
): String {
    return "${formatPercentageValue(value)}%"
}

private fun formatPercentageValue(
    value: Double
): String {
    val hasDecimalValue =
        value % 1.0 != 0.0

    return if (hasDecimalValue) {
        String.format(
            PORTUGUESE_LOCALE,
            "%.1f",
            value
        )
    } else {
        String.format(
            PORTUGUESE_LOCALE,
            "%.0f",
            value
        )
    }
}

@Preview(
    name = "Semifinal Maintained",
    showBackground = true
)
@Composable
private fun SemifinalStatusCardEligiblePreview() {
    FutTrackTheme {
        SemifinalStatusCard(
            status = SemifinalStatus(
                secondPlaceTeam = italy,
                thirdPlaceTeam = germany,
                differencePercentage = 5.6,
                limitPercentage = 30.0
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Semifinal Near Limit",
    showBackground = true
)
@Composable
private fun SemifinalStatusCardWarningPreview() {
    FutTrackTheme {
        SemifinalStatusCard(
            status = SemifinalStatus(
                secondPlaceTeam = italy,
                thirdPlaceTeam = germany,
                differencePercentage = 28.0,
                limitPercentage = 30.0
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Semifinal At Limit",
    showBackground = true
)
@Composable
private fun SemifinalStatusCardLimitPreview() {
    FutTrackTheme {
        SemifinalStatusCard(
            status = SemifinalStatus(
                secondPlaceTeam = italy,
                thirdPlaceTeam = germany,
                differencePercentage = 30.0,
                limitPercentage = 30.0
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Semifinal Not Available",
    showBackground = true
)
@Composable
private fun SemifinalStatusCardNotEligiblePreview() {
    FutTrackTheme {
        SemifinalStatusCard(
            status = SemifinalStatus(
                secondPlaceTeam = italy,
                thirdPlaceTeam = germany,
                differencePercentage = 30.7,
                limitPercentage = 30.0
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

private val italy = Team(
    id = "team_italy",
    name = "Itália",
    isoCode = "ITA"
)

private val germany = Team(
    id = "team_germany",
    name = "Alemanha",
    isoCode = "GER"
)

private val PORTUGUESE_LOCALE =
    Locale("pt", "BR")

private const val WARNING_THRESHOLD = 0.8

private val PROGRESS_HEIGHT = 10.dp
private val PROGRESS_CORNER_RADIUS = 5.dp

private val SAFE_GREEN =
    Color(0xFF2E7D32)

private val WARNING_ORANGE =
    Color(0xFFEF6C00)

private val LIMIT_RED =
    Color(0xFFC62828)