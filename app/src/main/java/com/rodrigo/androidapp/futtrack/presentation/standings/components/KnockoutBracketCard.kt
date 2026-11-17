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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.domain.model.SemifinalStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import java.util.Locale

@Composable
fun KnockoutBracketCard(
    firstPlaceTeam: Team,
    status: SemifinalStatus,
    modifier: Modifier = Modifier
) {
    val statusColor = status.displayColor()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            QualificationHeader(status, statusColor)
            QualificationProgress(status, statusColor)
            QualificationOutcome(firstPlaceTeam, status, statusColor)
        }
    }
}

@Composable
private fun QualificationHeader(
    status: SemifinalStatus,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Critério da semifinal",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Surface(
            shape = RoundedCornerShape(50),
            color = statusColor.copy(alpha = 0.12f)
        ) {
            Text(
                text = if (status.isEligible) "Semifinal" else "Final direta",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun QualificationProgress(
    status: SemifinalStatus,
    statusColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Diferença: ${status.differencePercentage.asPercentage()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Limite: ${status.limitPercentage.asPercentage()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        QualificationProgressBar(
            difference = status.differencePercentage,
            limit = status.limitPercentage,
            color = statusColor
        )
    }
}

@Composable
private fun QualificationProgressBar(
    difference: Double,
    limit: Double,
    color: Color
) {
    val progress = if (limit > 0.0) {
        (difference / limit).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(10.dp)
                .background(color)
        )
    }
}

@Composable
private fun QualificationOutcome(
    firstPlaceTeam: Team,
    status: SemifinalStatus,
    statusColor: Color
) {
    val message = if (status.isEligible) {
        "Semifinal mantida — ${status.secondPlaceTeam.name} × ${status.thirdPlaceTeam.name}"
    } else {
        "Final direta — ${firstPlaceTeam.name} × ${status.secondPlaceTeam.name}"
    }

    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = statusColor,
        fontWeight = FontWeight.Medium
    )
}

private fun SemifinalStatus.displayColor(): Color = when {
    !isEligible -> LIMIT_RED
    limitPercentage <= 0.0 -> SAFE_GREEN
    differencePercentage >= limitPercentage * WARNING_THRESHOLD -> WARNING_ORANGE
    else -> SAFE_GREEN
}

private fun Double.asPercentage(): String {
    val pattern = if (this % 1.0 == 0.0) "%.0f" else "%.1f"
    return String.format(PORTUGUESE_LOCALE, pattern, this) + "%"
}

@Preview(name = "Semifinal confirmed", showBackground = true, widthDp = 390)
@Composable
private fun SemifinalConfirmedPreview() {
    QualificationPreview(difference = 16.0)
}

@Preview(name = "Direct final", showBackground = true, widthDp = 390)
@Composable
private fun DirectFinalPreview() {
    QualificationPreview(difference = 35.0)
}

@Preview(name = "Near threshold", showBackground = true, widthDp = 390)
@Composable
private fun NearThresholdPreview() {
    QualificationPreview(difference = 28.0)
}

@Composable
private fun QualificationPreview(difference: Double) {
    val brazil = Team(id = "team_brasil", name = "Brasil", isoCode = "BR")
    val italy = Team(id = "team_italia", name = "Itália", isoCode = "IT")
    val germany = Team(id = "team_alemanha", name = "Alemanha", isoCode = "DE")

    FutTrackTheme {
        KnockoutBracketCard(
            firstPlaceTeam = brazil,
            status = SemifinalStatus(
                secondPlaceTeam = italy,
                thirdPlaceTeam = germany,
                differencePercentage = difference,
                limitPercentage = 30.0
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

private val PORTUGUESE_LOCALE = Locale.forLanguageTag("pt-BR")
private const val WARNING_THRESHOLD = 0.8
private val SAFE_GREEN = Color(0xFF2E7D32)
private val WARNING_ORANGE = Color(0xFFEF6C00)
private val LIMIT_RED = Color(0xFFC62828)
