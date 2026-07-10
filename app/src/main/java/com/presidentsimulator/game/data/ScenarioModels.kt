package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

@Serializable
enum class ScenarioDifficulty(val displayName: String) {
    STANDARD("Standard"),
    HARD("Hard"),
    NIGHTMARE("Nightmare"),
}

@Serializable
data class ScenarioPack(
    val id: String,
    val title: String,
    val tagline: String,
    val difficulty: ScenarioDifficulty,
    val recommendedNationId: String? = null,
)

@Serializable
data class ScenarioState(
    val scenarioId: String = "standard",
    val title: String = "Standard Mandate",
    val challengeSeed: Int = 0,
    val victoryYearOverride: Int? = null,
    val notes: List<String> = emptyList(),
)
