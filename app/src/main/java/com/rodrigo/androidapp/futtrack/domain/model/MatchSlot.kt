package com.rodrigo.androidapp.futtrack.domain.model

import java.time.LocalTime

enum class MatchSlot(
    val matchNumber: Int,
    val startTime: LocalTime
) {
    GAME_1(
        matchNumber = 1,
        startTime = LocalTime.of(8, 30)
    ),
    GAME_2(
        matchNumber = 2,
        startTime = LocalTime.of(8, 43)
    ),
    GAME_3(
        matchNumber = 3,
        startTime = LocalTime.of(8, 56)
    ),
    GAME_4(
        matchNumber = 4,
        startTime = LocalTime.of(9, 9)
    ),
    GAME_5(
        matchNumber = 5,
        startTime = LocalTime.of(9, 22)
    ),
    GAME_6(
        matchNumber = 6,
        startTime = LocalTime.of(9, 37)
    );

    companion object {
        fun fromMatchNumber(matchNumber: Int): MatchSlot? {
            return entries.find { slot ->
                slot.matchNumber == matchNumber
            }
        }
    }
}