package com.example.twosize.data.model

import com.google.firebase.database.Exclude
import java.math.BigInteger

data class Participant(
    val userId: String = "",
    val displayName: String = "",
    val startingSizeMm: String = "0",
    val currentSizeMm: String = "0",
    val totalGrowthMm: String = "0",
    val totalShrinkMm: String = "0",
    val biggestIncreaseMm: String = "0",
    val biggestDecreaseMm: String = "0",
    val actionCount: Int = 0,
    val online: Boolean = false,
    val lastUpdatedAt: Long = 0L
) {
    @get:Exclude
    val startingSizeBigInt: BigInteger
        get() = startingSizeMm.toBigIntegerOrNull() ?: BigInteger.ZERO

    @get:Exclude
    val currentSizeBigInt: BigInteger
        get() = currentSizeMm.toBigIntegerOrNull() ?: BigInteger.ZERO
}
