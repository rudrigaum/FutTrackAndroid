package com.rodrigo.androidapp.futtrack.ui.utils

import androidx.compose.ui.graphics.Color
import com.rodrigo.androidapp.futtrack.R

fun getTeamCrest(teamId: String): Int {
    return when (teamId) {
        "team_brasil" -> R.drawable.ic_escudo_brasil
        "team_italia" -> R.drawable.ic_escudo_italia
        "team_alemanha" -> R.drawable.ic_escudo_alemanha
        else -> R.drawable.ic_escudo_brasil
    }
}

fun getTeamJerseyColor(teamId: String, isGoalkeeper: Boolean = false): Color {
    return when (teamId) {
        "team_brasil" -> if (isGoalkeeper) Color(0xFF009B3A) else Color(0xFFFDE100) // GOL: Verde | LINHA: Amarelo
        "team_italia" -> if (isGoalkeeper) Color(0xFFD32F2F) else Color(0xFF005DB4) // GOL: Vermelho | LINHA: Azul
        "team_alemanha" -> if (isGoalkeeper) Color(0xFF000000) else Color(0xFFFFFFFF) // GOL: Preto | LINHA: Branco
        else -> Color(0xFFCCCCCC)
    }
}

fun getTeamNumberColor(teamId: String, isGoalkeeper: Boolean = false): Color {
    return when (teamId) {
        "team_brasil" -> if (isGoalkeeper) Color(0xFFFDE100) else Color(0xFF009B3A) // GOL: Amarelo | LINHA: Verde
        "team_italia" -> if (isGoalkeeper) Color(0xFF005DB4) else Color(0xFFFFFFFF) // GOL: Azul | LINHA: Branco
        "team_alemanha" -> if (isGoalkeeper) Color(0xFFFFD700) else Color(0xFF000000) // GOL: Amarelo Ouro | LINHA: Preto
        else -> Color(0xFF000000)
    }
}
