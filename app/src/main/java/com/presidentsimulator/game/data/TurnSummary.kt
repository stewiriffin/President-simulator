package com.presidentsimulator.game.data

data class TurnSummary(
    val year: Int,
    val month: Int,
    val budgetDelta: Long,
    val approvalDelta: Float,
    val populationDelta: Long,
    val gdpDelta: Long,
    val netIncome: Long,
)
