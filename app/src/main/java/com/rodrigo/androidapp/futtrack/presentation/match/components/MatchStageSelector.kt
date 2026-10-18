
package com.rodrigo.androidapp.futtrack.presentation.match.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.domain.model.MatchLeg
import com.rodrigo.androidapp.futtrack.domain.model.MatchStage
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme

@Composable
fun MatchStageSelector(
    selectedStage: MatchStage,
    selectedLeg: MatchLeg?,
    onSelectionChange: (MatchStage, MatchLeg?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SelectionDropdown(
            label = "Fase da competição",
            selectedLabel = selectedStage.displayLabel(),
            options = MatchStage.entries,
            optionLabel = MatchStage::displayLabel,
            onSelected = { stage ->
                onSelectionChange(stage, null)
            }
        )

        if (selectedStage != MatchStage.REGULAR) {
            SelectionDropdown(
                label = "Jogo",
                selectedLabel = selectedLeg?.displayLabel()
                    ?: "Selecione ida ou volta",
                options = MatchLeg.entries,
                optionLabel = MatchLeg::displayLabel,
                onSelected = { leg ->
                    onSelectionChange(selectedStage, leg)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SelectionDropdown(
    label: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
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
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(optionLabel(option))
                    },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

private fun MatchStage.displayLabel(): String {
    return when (this) {
        MatchStage.REGULAR -> "Fase regular"
        MatchStage.SEMIFINAL -> "Semifinal"
        MatchStage.FINAL -> "Final"
    }
}

private fun MatchLeg.displayLabel(): String {
    return when (this) {
        MatchLeg.FIRST -> "Ida"
        MatchLeg.SECOND -> "Volta"
    }
}

@Preview(
    name = "Regular Match",
    showBackground = true
)
@Composable
private fun RegularMatchStageSelectorPreview() {
    var stage by remember {
        mutableStateOf(MatchStage.REGULAR)
    }

    var leg by remember {
        mutableStateOf<MatchLeg?>(null)
    }

    FutTrackTheme {
        MatchStageSelector(
            selectedStage = stage,
            selectedLeg = leg,
            onSelectionChange = { newStage, newLeg ->
                stage = newStage
                leg = newLeg
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Knockout Match",
    showBackground = true
)
@Composable
private fun KnockoutMatchStageSelectorPreview() {
    var stage by remember {
        mutableStateOf(MatchStage.SEMIFINAL)
    }

    var leg by remember {
        mutableStateOf<MatchLeg?>(MatchLeg.FIRST)
    }

    FutTrackTheme {
        MatchStageSelector(
            selectedStage = stage,
            selectedLeg = leg,
            onSelectionChange = { newStage, newLeg ->
                stage = newStage
                leg = newLeg
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}
