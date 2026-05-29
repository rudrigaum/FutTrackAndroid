package com.rodrigo.androidapp.futtrack.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
sealed class BottomNavItem(
    val route: String, // Usaremos String provisoriamente para o roteamento até configurarmos o Serialization
    val title: String,
    val icon: ImageVector
) {
    data object Teams : BottomNavItem(
        route = "teams_route",
        title = "Times",
        icon = Icons.Default.Person
    )

    data object Matches : BottomNavItem(
        route = "matches_route",
        title = "Partidas",
        icon = Icons.Default.DateRange
    )

    data object Standings : BottomNavItem(
        route = "standings_route",
        title = "Tabela",
        icon = Icons.Default.List
    )

    data object Statistics : BottomNavItem(
        route = "statistics_route",
        title = "Estatísticas",
        icon = Icons.Default.Star
    )
}