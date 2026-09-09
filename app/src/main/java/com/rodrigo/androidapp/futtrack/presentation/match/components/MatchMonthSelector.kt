package com.rodrigo.androidapp.futtrack.presentation.match.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MatchMonthSelector(
    selectedMonth: YearMonth,
    canSelectOlderMonth: Boolean,
    canSelectNewerMonth: Boolean,
    onOlderMonthClick: () -> Unit,
    onNewerMonthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonthNavigationButton(
                isEnabled = canSelectOlderMonth,
                isOlderMonth = true,
                onClick = onOlderMonthClick
            )

            Text(
                text = selectedMonth.displayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            MonthNavigationButton(
                isEnabled = canSelectNewerMonth,
                isOlderMonth = false,
                onClick = onNewerMonthClick
            )
        }
    }
}

@Composable
private fun MonthNavigationButton(
    isEnabled: Boolean,
    isOlderMonth: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = isEnabled
    ) {
        Icon(
            imageVector = if (isOlderMonth) {
                Icons.Default.KeyboardArrowLeft
            } else {
                Icons.Default.KeyboardArrowRight
            },
            contentDescription = if (isOlderMonth) {
                "Mês anterior"
            } else {
                "Próximo mês"
            }
        )
    }
}

private fun YearMonth.displayName(): String {
    val formattedMonth = format(MONTH_FORMATTER)

    return formattedMonth.replaceFirstChar { firstCharacter ->
        firstCharacter.titlecase(PORTUGUESE_BRAZIL)
    }
}

@Preview(
    name = "Latest Month",
    showBackground = true
)
@Composable
private fun LatestMonthPreview() {
    FutTrackTheme {
        MatchMonthSelector(
            selectedMonth = YearMonth.of(2026, 9),
            canSelectOlderMonth = true,
            canSelectNewerMonth = false,
            onOlderMonthClick = {},
            onNewerMonthClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Month Between Others",
    showBackground = true
)
@Composable
private fun IntermediateMonthPreview() {
    FutTrackTheme {
        MatchMonthSelector(
            selectedMonth = YearMonth.of(2026, 8),
            canSelectOlderMonth = true,
            canSelectNewerMonth = true,
            onOlderMonthClick = {},
            onNewerMonthClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

private val PORTUGUESE_BRAZIL =
    Locale.forLanguageTag("pt-BR")

private val MONTH_FORMATTER =
    DateTimeFormatter.ofPattern(
        "MMMM yyyy",
        PORTUGUESE_BRAZIL
    )