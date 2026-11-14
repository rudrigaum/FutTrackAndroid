package com.rodrigo.androidapp.futtrack.presentation.standings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.domain.model.SemifinalStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamCrest
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
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            BracketHeader(
                isEligible = status.isEligible
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                SAFE_GREEN.copy(alpha = 0.035f)
                            )
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BracketProgress(
                    firstPlaceTeam = firstPlaceTeam,
                    status = status,
                    statusColor = statusColor
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                BracketMatches(
                    firstPlaceTeam = firstPlaceTeam,
                    status = status
                )

            }
        }
    }
}

@Composable
private fun BracketHeader(
    isEligible: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        SAFE_GREEN.copy(alpha = 0.09f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BracketTreeIcon()

        Text(
            text = "Chaveamento do mata-mata",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )

        Surface(
            shape = RoundedCornerShape(50),
            color = SAFE_GREEN.copy(alpha = 0.12f)
        ) {
            Text(
                text = if (isEligible) {
                    "Semifinal mantida"
                } else {
                    "Final direta"
                },
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = SAFE_GREEN
            )
        }
    }
}

@Composable
private fun BracketTreeIcon() {
    Canvas(
        modifier = Modifier.size(28.dp)
    ) {
        val unit = size.width / 28f
        val lineWidth = 2.dp.toPx()

        val path = Path().apply {
            moveTo(9f * unit, 7f * unit)
            lineTo(15f * unit, 7f * unit)
            lineTo(15f * unit, 21f * unit)
            lineTo(21f * unit, 21f * unit)

            moveTo(15f * unit, 14f * unit)
            lineTo(21f * unit, 14f * unit)
        }

        drawPath(
            path = path,
            color = SAFE_GREEN,
            style = Stroke(
                width = lineWidth,
                cap = StrokeCap.Round
            )
        )

        listOf(
            Offset(2f * unit, 4f * unit),
            Offset(21f * unit, 11f * unit),
            Offset(21f * unit, 18f * unit)
        ).forEach { position ->
            drawRoundRect(
                color = SAFE_GREEN,
                topLeft = position,
                size = Size(
                    width = 6f * unit,
                    height = 6f * unit
                ),
                cornerRadius = CornerRadius(1.5f * unit),
                style = Stroke(width = lineWidth)
            )
        }
    }
}

@Composable
private fun BracketProgress(
    firstPlaceTeam: Team,
    status: SemifinalStatus,
    statusColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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

        BracketProgressBar(
            difference = status.differencePercentage,
            limit = status.limitPercentage,
            color = statusColor
        )


        Text(
            text = if (status.isEligible) {
                "✓ Semifinal mantida – " +
                        "${status.secondPlaceTeam.name} x ${status.thirdPlaceTeam.name}"
            } else {
                "✓ Final direta – " +
                        "${firstPlaceTeam.name} x ${status.secondPlaceTeam.name}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = statusColor
        )
    }
}

@Composable
private fun BracketProgressBar(
    difference: Double,
    limit: Double,
    color: Color
) {
    val progress = if (limit > 0.0) {
        (difference / limit)
            .toFloat()
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(
                MaterialTheme.colorScheme.outlineVariant
            )
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
private fun BracketMatches(
    firstPlaceTeam: Team,
    status: SemifinalStatus
) {
    if (status.isEligible) {
        SemifinalBracket(
            firstPlaceTeam = firstPlaceTeam,
            status = status
        )
    } else {
        DirectFinalBracket(
            firstPlaceTeam = firstPlaceTeam,
            secondPlaceTeam = status.secondPlaceTeam
        )
    }
}

@Composable
private fun SemifinalBracket(
    firstPlaceTeam: Team,
    status: SemifinalStatus
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        BracketConnector(
            modifier = Modifier.matchParentSize()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.Top
        ) {
            BracketStage(
                title = "Semifinal",
                firstTeam = status.secondPlaceTeam,
                secondTeam = status.thirdPlaceTeam,
                modifier = Modifier.weight(1f)
            )

            BracketStage(
                title = "Final",
                firstTeam = firstPlaceTeam,
                secondTeam = null,
                placeholder = "Vencedor da semifinal",
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 28.dp)
            )
        }
    }
}

@Composable
private fun BracketConnector(
    modifier: Modifier = Modifier
) {
    val connectorColor =
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)

    Canvas(modifier = modifier) {
        val gap = 28.dp.toPx()
        val middleX = size.width / 2f

        val startX = middleX - gap / 2f
        val endX = middleX + gap / 2f

        val startY = size.height * 0.64f
        val endY = size.height * 0.76f

        val path = Path().apply {
            moveTo(startX, startY)

            cubicTo(
                startX + gap * 0.55f,
                startY,
                endX - gap * 0.55f,
                endY,
                endX,
                endY
            )
        }

        drawPath(
            path = path,
            color = connectorColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
private fun DirectFinalBracket(
    firstPlaceTeam: Team,
    secondPlaceTeam: Team
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        BracketStage(
            title = "Final",
            firstTeam = firstPlaceTeam,
            secondTeam = secondPlaceTeam,
            modifier = Modifier.fillMaxWidth(0.75f)
        )
    }
}

@Composable
private fun BracketStage(
    title: String,
    firstTeam: Team,
    secondTeam: Team?,
    modifier: Modifier = Modifier,
    placeholder: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SAFE_GREEN.copy(alpha = 0.12f)
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SAFE_GREEN
            )
        }

        Text(
            text = "↔ Ida e volta",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BracketMatchPanel(
            firstTeam = firstTeam,
            secondTeam = secondTeam,
            placeholder = placeholder
        )
    }
}

@Composable
private fun BracketMatchPanel(
    firstTeam: Team,
    secondTeam: Team?,
    placeholder: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = 0.5f
            )
        ),
        shadowElevation = 3.dp
    ) {
        Column {
            BracketTeamRow(team = firstTeam)

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            if (secondTeam != null) {
                BracketTeamRow(team = secondTeam)
            } else {
                BracketPlaceholderRow(
                    text = placeholder.orEmpty()
                )
            }
        }
    }
}

@Composable
private fun BracketTeamRow(
    team: Team
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(
                horizontal = 9.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Image(
            painter = painterResource(
                id = getTeamCrest(team.id)
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = team.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BracketPlaceholderRow(
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(
                horizontal = 9.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "🏆",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun SemifinalStatus.displayColor(): Color {
    return when {
        !isEligible -> LIMIT_RED
        limitPercentage <= 0.0 -> SAFE_GREEN
        differencePercentage >= limitPercentage * WARNING_THRESHOLD ->
            WARNING_ORANGE
        else -> SAFE_GREEN
    }
}

private fun Double.asPercentage(): String {
    return "${percentageValue()}%"
}

private fun Double.percentageValue(): String {
    val pattern = if (this % 1.0 == 0.0) {
        "%.0f"
    } else {
        "%.1f"
    }

    return String.format(
        PORTUGUESE_LOCALE,
        pattern,
        this
    )
}

@Preview(
    name = "Knockout - Semifinal",
    showBackground = true,
    widthDp = 390
)
@Composable
private fun KnockoutBracketEligiblePreview() {
    FutTrackTheme {
        KnockoutBracketCard(
            firstPlaceTeam = previewBrazil,
            status = SemifinalStatus(
                secondPlaceTeam = previewItaly,
                thirdPlaceTeam = previewGermany,
                differencePercentage = 17.5,
                limitPercentage = 30.0
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Knockout - Direct Final",
    showBackground = true,
    widthDp = 390
)
@Composable
private fun KnockoutBracketDirectFinalPreview() {
    FutTrackTheme {
        KnockoutBracketCard(
            firstPlaceTeam = previewBrazil,
            status = SemifinalStatus(
                secondPlaceTeam = previewItaly,
                thirdPlaceTeam = previewGermany,
                differencePercentage = 35.0,
                limitPercentage = 30.0
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Knockout - Near Limit",
    showBackground = true,
    widthDp = 390
)
@Composable
private fun KnockoutBracketWarningPreview() {
    FutTrackTheme {
        KnockoutBracketCard(
            firstPlaceTeam = previewBrazil,
            status = SemifinalStatus(
                secondPlaceTeam = previewItaly,
                thirdPlaceTeam = previewGermany,
                differencePercentage = 28.0,
                limitPercentage = 30.0
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

private val previewBrazil = Team(
    id = "team_brasil",
    name = "Brasil",
    isoCode = "BR"
)

private val previewItaly = Team(
    id = "team_italia",
    name = "Itália",
    isoCode = "IT"
)

private val previewGermany = Team(
    id = "team_alemanha",
    name = "Alemanha",
    isoCode = "DE"
)

private val PORTUGUESE_LOCALE =
    Locale.forLanguageTag("pt-BR")

private const val WARNING_THRESHOLD = 0.8

private val SAFE_GREEN = Color(0xFF2E7D32)
private val WARNING_ORANGE = Color(0xFFEF6C00)
private val LIMIT_RED = Color(0xFFC62828)

