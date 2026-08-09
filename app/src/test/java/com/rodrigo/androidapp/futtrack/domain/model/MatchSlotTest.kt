package com.rodrigo.androidapp.futtrack.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class MatchSlotTest {

    @Test
    fun `should contain six official match slots`() {
        assertEquals(
            6,
            MatchSlot.entries.size
        )
    }

    @Test
    fun `should map each match number to its official start time`() {
        val expectedSlots = mapOf(
            1 to LocalTime.of(8, 0),
            2 to LocalTime.of(8, 13),
            3 to LocalTime.of(8, 26),
            4 to LocalTime.of(8, 39),
            5 to LocalTime.of(8, 52),
            6 to LocalTime.of(9, 7)
        )

        expectedSlots.forEach { (matchNumber, expectedStartTime) ->
            val slot = MatchSlot.fromMatchNumber(matchNumber)

            assertEquals(
                matchNumber,
                slot?.matchNumber
            )

            assertEquals(
                expectedStartTime,
                slot?.startTime
            )
        }
    }

    @Test
    fun `should return null when match number does not exist`() {
        val slot = MatchSlot.fromMatchNumber(7)

        assertNull(slot)
    }
}