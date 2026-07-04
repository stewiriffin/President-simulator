package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.AnalyticsState
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.HistoricalSnapshot
import com.presidentsimulator.game.data.SaveLoadFeedback
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Analytics ledger and GameState serialization engine.
 * [GameViewModel] applies transforms via immutable copies and persists payloads locally.
 */
class AnalyticsSaveViewModel {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Appends a KPI snapshot for the current month and trims the rolling window
     * to [AnalyticsState.MAX_HISTORY_SIZE] entries.
     */
    fun recordHistoricalSnapshot(state: GameState): GameState {
        val snapshot = HistoricalSnapshot(
            month = state.month,
            year = state.year,
            budget = state.vitals.budget,
            approval = state.vitals.approval,
            population = state.vitals.population,
            gdp = calculateGDP(state),
        )
        val history = (state.analytics.history + snapshot)
            .takeLast(AnalyticsState.MAX_HISTORY_SIZE)
        return state.copy(analytics = state.analytics.copy(history = history))
    }

    /**
     * Dynamic economic-health score from infrastructure, factory throughput, and tax policy.
     */
    fun calculateGDP(state: GameState): Long {
        val economy = state.economy
        val production = state.production
        val legal = state.legal

        val industrialBase =
            economy.factories * 2_400_000_000L +
                economy.farms * 900_000_000L +
                economy.housing * 700_000_000L +
                production.powerPlants * 1_500_000_000L +
                production.mines * 1_100_000_000L

        val throughput =
            production.lastGoodsProduced * 4_000_000L +
                production.lastMaterialsProduced * 1_200_000L +
                production.lastEnergyProduced * 800_000L +
                production.lastFoodProduced * 600_000L

        val taxEfficiency = (0.55 + economy.taxRate * 0.9).coerceIn(0.4, 1.2)
        val productionModifier = legal.combinedProductionModifier.toDouble().coerceIn(0.5, 1.5)
        val tradeBonus = state.tradeExportBonus

        return (
            (industrialBase + throughput + tradeBonus) * taxEfficiency * productionModifier
            ).toLong().coerceAtLeast(0L)
    }

    fun exportGameStateToJson(state: GameState): String =
        json.encodeToString(GameState.serializer(), state)

    fun importGameStateFromJson(jsonString: String): GameState =
        json.decodeFromString(GameState.serializer(), jsonString)

    fun feedbackForSave(jsonPayload: String): SaveLoadFeedback = SaveLoadFeedback(
        message = "Game progress saved (${formatBytes(jsonPayload.length)}).",
        payloadBytes = jsonPayload.length,
        success = true,
    )

    fun feedbackForLoad(jsonPayload: String): SaveLoadFeedback = SaveLoadFeedback(
        message = "Loaded automated save (${formatBytes(jsonPayload.length)}).",
        payloadBytes = jsonPayload.length,
        success = true,
    )

    fun feedbackForLoadFailure(reason: String): SaveLoadFeedback = SaveLoadFeedback(
        message = "Load failed: $reason",
        payloadBytes = 0,
        success = false,
    )

    fun feedbackForMissingSave(): SaveLoadFeedback = SaveLoadFeedback(
        message = "No automated save found.",
        payloadBytes = 0,
        success = false,
    )

    companion object {
        const val PREFS_NAME = "nation_state_simulator_save"
        const val KEY_AUTOMATED_SAVE = "automated_save_json"
        const val KEY_LAST_PAYLOAD_BYTES = "last_payload_bytes"

        fun formatBytes(bytes: Int): String = when {
            bytes >= 1_048_576 -> "%.2f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024 -> "%.1f KB".format(bytes / 1_024.0)
            else -> "$bytes bytes"
        }

        fun trendDelta(history: List<HistoricalSnapshot>, selector: (HistoricalSnapshot) -> Long): Long {
            if (history.size < 2) return 0L
            return selector(history.last()) - selector(history.first())
        }

        fun trendDeltaFloat(
            history: List<HistoricalSnapshot>,
            selector: (HistoricalSnapshot) -> Float,
        ): Float {
            if (history.size < 2) return 0f
            return selector(history.last()) - selector(history.first())
        }
    }
}
