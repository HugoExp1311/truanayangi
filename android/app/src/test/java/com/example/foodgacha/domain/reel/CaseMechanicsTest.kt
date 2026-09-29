package com.example.foodgacha.domain.reel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CaseMechanicsTest {

    @Test
    fun createSpinProfile_generatesValuesWithinExpectedRanges() {
        val random = Random(42)
        val profile = CaseMechanics.createSpinProfile(random)

        assertTrue("Tiles should be in 30..40", profile.tiles in 30..40)
        assertTrue("Duration should be in 7500..9500 ms", profile.durationMs in 7500..9500)
        assertTrue("Friction should be in 2.7..3.3", profile.friction in 2.7..3.3)
        assertTrue("StopFraction should be in 0.10..0.90", profile.stopFraction in 0.10..0.90)
    }

    @Test
    fun spinProgress_startsAtZeroAndEndsAtOne() {
        val friction = 3.0
        val p0 = CaseMechanics.spinProgress(0.0, friction)
        val p1 = CaseMechanics.spinProgress(1.0, friction)
        val pMid = CaseMechanics.spinProgress(0.5, friction)

        assertEquals(0.0, p0, 0.0001)
        assertEquals(1.0, p1, 0.0001)
        // Deceleration curve: at t=0.5, value is 1 - 0.5^3 = 0.875 > 0.5
        assertTrue("Easing should decelerate with steep initial progress", pMid > 0.5)
    }
}
