package com.example.foodgacha.domain.reel

import kotlin.math.floor
import kotlin.math.pow
import kotlin.random.Random

data class SpinProfile(
    val tiles: Int,
    val durationMs: Long,
    val friction: Double,
    val stopFraction: Double
)

object CaseMechanics {
    /**
     * Generates randomized CS:GO spin profile matching web implementation:
     * - tiles: 30..40
     * - durationMs: 7500..9500 ms
     * - friction: 2.7..3.3
     * - stopFraction: 0.10..0.90 (safe zone avoiding card boundaries)
     */
    fun createSpinProfile(random: Random = Random.Default): SpinProfile {
        val tiles = 30 + random.nextInt(11)
        val durationMs = 7500L + (random.nextDouble() * 2001.0).toLong()
        val friction = 2.7 + random.nextDouble() * 0.6
        val stopFraction = (floor(random.nextDouble() * 81.0) + 10.0) / 100.0
        return SpinProfile(
            tiles = tiles,
            durationMs = durationMs,
            friction = friction,
            stopFraction = stopFraction
        )
    }

    /**
     * Deceleration easing curve: 1 - (1 - progress)^friction
     */
    fun spinProgress(progress: Double, friction: Double): Double {
        val clamped = progress.coerceIn(0.0, 1.0)
        return 1.0 - (1.0 - clamped).pow(friction)
    }

    /**
     * Computes rarity tier (0: Rare, 1: Mythical, 2: Legendary, 3+: Ancient)
     */
    fun priceRarity(priceInThousands: Int): Int {
        return when {
            priceInThousands <= 40 -> 0
            priceInThousands <= 65 -> 1
            priceInThousands <= 100 -> 2
            priceInThousands <= 130 -> 3
            else -> 4
        }
    }
}
