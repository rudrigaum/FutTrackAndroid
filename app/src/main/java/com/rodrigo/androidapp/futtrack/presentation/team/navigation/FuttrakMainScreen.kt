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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rodrigo.androidapp.futtrack.presentation.match.MatchRoute
import com.rodrigo.androidapp.futtrack.presentation.standings.StandingsRoute
import com.rodrigo.androidapp.futtrack.presentation.team.TeamListRoute
import com.rodrigo.androidapp.futtrack.presentation.team.TeamPlayersRoute

@Composable
fun FuttrakMainScreen() {
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
                TeamListRoute(
                    onNavigateToTeamPlayers = { teamId, teamName ->
                        navController.navigate("team_players/$teamId/$teamName")
                    }
                )
            }

            composable(
                route = "team_players/{teamId}/{teamName}",
                arguments = listOf(
                    navArgument("teamId") { type = NavType.StringType },
                    navArgument("teamName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                val teamName = backStackEntry.arguments?.getString("teamName") ?: ""

                TeamPlayersRoute(
                    teamId = teamId,
                    teamName = teamName,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Matches.route) {
                MatchRoute()
            }
            composable(BottomNavItem.Standings.route) {
                StandingsRoute()
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