package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
enum class LegacyPillar(val displayName: String) {
    PROSPERITY("Prosperity"),
    SECURITY("Security"),
    DIPLOMACY("Diplomacy"),
    SOCIETY("Society"),
    MANDATE("Mandate"),
}

@Serializable
enum class LegacyTone {
    TRIUMPH,
    MILESTONE,
    STAIN,
    TRAGEDY,
    TURNING_POINT,
}

@Serializable
data class LegacyEntry(
    val id: String,
    val year: Int,
    val month: Int,
    val title: String,
    val detail: String,
    val pillar: LegacyPillar,
    val tone: LegacyTone,
    val scoreDelta: Int,
)

@Serializable
data class LegacyScores(
    val prosperity: Int = 50,
    val security: Int = 50,
    val diplomacy: Int = 50,
    val society: Int = 50,
    val mandate: Int = 50,
) {
    val overall: Int
        get() = ((prosperity + security + diplomacy + society + mandate) / 5f).roundToInt()

    val grade: String
        get() = when {
            overall >= 85 -> "Historic"
            overall >= 70 -> "Strong"
            overall >= 55 -> "Mixed"
            overall >= 40 -> "Fragile"
            else -> "Disgrace"
        }

    fun adjust(pillar: LegacyPillar, delta: Int): LegacyScores = when (pillar) {
        LegacyPillar.PROSPERITY -> copy(prosperity = (prosperity + delta).coerceIn(0, 100))
        LegacyPillar.SECURITY -> copy(security = (security + delta).coerceIn(0, 100))
        LegacyPillar.DIPLOMACY -> copy(diplomacy = (diplomacy + delta).coerceIn(0, 100))
        LegacyPillar.SOCIETY -> copy(society = (society + delta).coerceIn(0, 100))
        LegacyPillar.MANDATE -> copy(mandate = (mandate + delta).coerceIn(0, 100))
    }
}

@Serializable
data class LegacyState(
    val scores: LegacyScores = LegacyScores(),
    val entries: List<LegacyEntry> = emptyList(),
    val warsWon: Int = 0,
    val warsLost: Int = 0,
    val electionsWon: Int = 0,
    val disastersHandled: Int = 0,
    val lawsEnacted: Int = 0,
    val peakApproval: Float = 55f,
    val lowestApproval: Float = 55f,
    val lastLegacyNote: String = "",
) {
    val recentEntries: List<LegacyEntry>
        get() = entries.takeLast(8).asReversed()

    fun summaryLine(): String =
        "Legacy ${scores.grade} (${scores.overall}) · ${entries.size} chapters"
}
