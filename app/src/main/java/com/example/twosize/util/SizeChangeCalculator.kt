package com.example.twosize.util

import kotlin.random.Random
import java.math.BigInteger

enum class ChangeMode {
    GENTLE, BALANCED, EXTREME
}

data class ChangeResult(
    val delta: BigInteger,
    val newSize: BigInteger
)

class SizeChangeCalculator(
    private val random: Random = Random.Default
) {
    data class Config(
        val minPercent: Int,
        val maxPercent: Int,
        val floorChangeMm: BigInteger
    )

    private val configByMode = mapOf(
        ChangeMode.GENTLE to Config(1, 8, BigInteger.ONE),
        ChangeMode.BALANCED to Config(5, 25, BigInteger("10")),
        ChangeMode.EXTREME to Config(20, 120, BigInteger("100"))
    )

    fun grow(current: BigInteger, mode: ChangeMode): ChangeResult {
        val delta = computeDelta(current, mode)
        return ChangeResult(delta, current + delta)
    }

    fun shrink(current: BigInteger, mode: ChangeMode): ChangeResult {
        val delta = computeDelta(current, mode)
        val next = (current - delta).coerceAtLeast(BigInteger.ONE)
        val actualDelta = current - next
        return ChangeResult(actualDelta, next)
    }

    private fun computeDelta(current: BigInteger, mode: ChangeMode): BigInteger {
        val config = configByMode.getValue(mode)
        val percent = random.nextInt(config.minPercent, config.maxPercent + 1)
        val candidate = current * BigInteger.valueOf(percent.toLong()) / BigInteger("100")
        return maxBigInt(candidate, config.floorChangeMm)
    }

    private fun maxBigInt(left: BigInteger, right: BigInteger): BigInteger =
        if (left > right) left else right
}
