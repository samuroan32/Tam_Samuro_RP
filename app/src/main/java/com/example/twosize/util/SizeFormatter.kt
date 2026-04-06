package com.example.twosize.util

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

object SizeFormatter {
    enum class Mode { READABLE, EXACT }

    fun format(mm: BigInteger, mode: Mode = Mode.READABLE): String {
        return when (mode) {
            Mode.READABLE -> readable(mm)
            Mode.EXACT -> "${mm} mm"
        }
    }

    private fun readable(mm: BigInteger): String {
        if (mm < BigInteger("1000")) return "$mm mm"
        if (mm < BigInteger("1000000")) {
            return "${toScaled(mm, BigDecimal(10))} cm"
        }
        if (mm < BigInteger("1000000000")) {
            return "${toScaled(mm, BigDecimal(1000))} m"
        }
        if (mm < BigInteger("1000000000000")) {
            return "${toScaled(mm, BigDecimal(1000000))} km"
        }
        val millionMm = BigDecimal(mm).divide(BigDecimal("1000000000"), 2, RoundingMode.HALF_UP)
        return "$millionMm million km"
    }

    private fun toScaled(mm: BigInteger, divisor: BigDecimal): String {
        return BigDecimal(mm).divide(divisor, 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }
}
