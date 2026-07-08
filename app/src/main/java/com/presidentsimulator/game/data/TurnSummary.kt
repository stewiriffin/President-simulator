package com.presidentsimulator.game.data

data class TurnSummary(
    val year: Int,
    val month: Int,
    val budgetDelta: Long,
    val approvalDelta: Float,
    val populationDelta: Long,
    val gdpDelta: Long,
    val netIncome: Long,
    val bulletin: List<String> = emptyList(),
)

/**
 * Snapshot shown when a war ends in victory or defeat.
 */
data class WarOutcome(
    val victory: Boolean,
    val targetCountryId: String,
    val targetName: String,
    val monthsActive: Int,
    val playerCasualties: Long,
    val enemyCasualties: Long,
    val budgetDelta: Long,
    val approvalDelta: Float,
    val finalProgress: Float,
)
