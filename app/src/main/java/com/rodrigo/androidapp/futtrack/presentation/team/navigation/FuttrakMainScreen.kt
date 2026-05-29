package com.rodrigo.androidapp.futtrack.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rodrigo.androidapp.futtrack.presentation.team.TeamListRoute

@Composable
fun FuttrakMainScreen() {
    // NavController is equivalent to managing the selected tab and navigation stack state
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Teams,
        BottomNavItem.Matches,
        BottomNavItem.Standings,
        BottomNavItem.Statistics
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    NavigationBarItem(
                        icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                        label = { Text(text = item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Teams.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Teams.route) {
                TeamListRoute() // A nossa tela de times que já está pronta!
            }
            composable(BottomNavItem.Matches.route) {
                PlaceholderScreen("Partidas")
            }
            composable(BottomNavItem.Standings.route) {
                PlaceholderScreen("Tabela")
            }
            composable(BottomNavItem.Statistics.route) {
                PlaceholderScreen("Estatísticas")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Tela em construção: $title")
    }
}