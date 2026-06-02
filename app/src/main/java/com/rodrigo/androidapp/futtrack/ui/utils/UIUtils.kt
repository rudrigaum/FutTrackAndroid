package com.rodrigo.androidapp.futtrack.ui.utils

import com.rodrigo.androidapp.futtrack.R

fun getTeamCrest(teamId: String): Int {
    return when (teamId) {
        "team_brasil" -> R.drawable.ic_escudo_brasil
        "team_italia" -> R.drawable.ic_escudo_italia
        "team_alemanha" -> R.drawable.ic_escudo_alemanha
        else -> R.drawable.ic_escudo_brasil
    }
}