package com.example.twosize.util

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

data class RelationshipSummary(
    val ratioText: String,
    val summaryText: String
)

object RelationshipAnalyzer {
    fun analyze(you: BigInteger, partner: BigInteger): RelationshipSummary {
        if (partner == BigInteger.ZERO) {
            return RelationshipSummary("N/A", "Waiting for partner")
        }
        val ratio = BigDecimal(you).divide(BigDecimal(partner), 2, RoundingMode.HALF_UP)
        val ratioText = "You are ${ratio.stripTrailingZeros().toPlainString()} times larger than partner"
        val summary = when {
            ratio > BigDecimal("5") -> "You are much larger"
            ratio > BigDecimal("1.15") -> "You are larger"
            ratio < BigDecimal("0.2") -> "You are tiny compared to partner"
            ratio < BigDecimal("0.87") -> "Partner is larger"
            else -> "Almost same size"
        }
        return RelationshipSummary(ratioText, summary)
    }
}
