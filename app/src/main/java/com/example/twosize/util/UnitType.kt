package com.example.twosize.util

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

enum class UnitType(val label: String, val millimetersPerUnit: BigDecimal) {
    MM("mm", BigDecimal("1")),
    CM("cm", BigDecimal("10")),
    M("m", BigDecimal("1000")),
    KM("km", BigDecimal("1000000"));

    companion object {
        val options: List<UnitType> = entries

        fun fromLabel(label: String): UnitType = options.firstOrNull { it.label == label } ?: MM
    }
}

object SizeParser {
    fun decimalToMillimeters(raw: String, unitType: UnitType): BigInteger? {
        val value = raw.trim().toBigDecimalOrNull() ?: return null
        if (value <= BigDecimal.ZERO) return null
        val mm = value.multiply(unitType.millimetersPerUnit)
            .setScale(0, RoundingMode.HALF_UP)
        return mm.toBigInteger()
    }
}
