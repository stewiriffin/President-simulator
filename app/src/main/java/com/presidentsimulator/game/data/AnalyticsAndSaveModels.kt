package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/**
 * Immutable KPI capture for a single simulation month.
 */
@Serializable
data class HistoricalSnapshot(
    val month: Int,
    val year: Int,
    val budget: Long,
    val approval: Float,
    val population: Long,
    val gdp: Long,
) {
    val dateLabel: String
        get() = "${GameState.monthName(month)} $year"
}

/**
 * Rolling ledger of recent monthly snapshots (1–2 in-game years).
 */
@Serializable
data class AnalyticsState(
    val history: List<HistoricalSnapshot> = emptyList(),
) {
    companion object {
        const val MAX_HISTORY_SIZE = 24
    }
}

/**
 * UI-facing feedback after a save or load operation.
 */
data class SaveLoadFeedback(
    val message: String = "No save operations yet.",
    val payloadBytes: Int = 0,
    val success: Boolean = false,
)
