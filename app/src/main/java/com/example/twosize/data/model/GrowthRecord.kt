package com.example.twosize.data.model

import com.google.firebase.database.Exclude
import java.math.BigInteger

data class GrowthRecord(
    val eventId: String = "",
    val userId: String = "",
    val type: String = "join",
    val deltaMm: String = "0",
    val sizeAfterMm: String = "0",
    val createdAt: Long = 0L
) {
    @get:Exclude
    val deltaBigInt: BigInteger
        get() = deltaMm.toBigIntegerOrNull() ?: BigInteger.ZERO
}
