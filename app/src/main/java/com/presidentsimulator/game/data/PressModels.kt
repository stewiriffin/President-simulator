package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
enum class HeadlineTone {
    POSITIVE,
    NEUTRAL,
    NEGATIVE,
    SCANDAL,
}

@Serializable
enum class PressOutlet(val displayName: String, val hostilityBias: Float) {
    STATE_BROADCAST("State Broadcast", -8f),
    NATIONAL_BROADSHEET("National Broadsheet", 0f),
    TABLOID("Evening Tabloid", 12f),
    FOREIGN_DESK("Foreign Desk", 6f),
    DIGITAL_WIRE("Digital Wire", 4f),
}

@Serializable
enum class PressTopic {
    WAR,
    ECONOMY,
    SECURITY,
    ELECTION,
    SCANDAL,
    DIPLOMACY,
    SOCIETY,
    GENERAL,
}

/**
 * A rolling narrative that can escalate across months if left unhandled.
 */
@Serializable
data class PressNarrativeArc(
    val id: String,
    val topic: PressTopic,
    val title: String,
    val intensity: Float = 20f,
    val monthsActive: Int = 1,
    val lastTouchedMonthKey: String = "",
)

@Serializable
data class PressHeadline(
    val id: String,
    val title: String,
    val lede: String,
    val tone: HeadlineTone,
    val year: Int,
    val month: Int,
    val outlet: PressOutlet = PressOutlet.NATIONAL_BROADSHEET,
    val topic: PressTopic = PressTopic.GENERAL,
    /** Linked narrative arc id, if any. */
    val arcId: String? = null,
    /** If true, player already spun or suppressed this story. */
    val handled: Boolean = false,
    /** Spin backfired — story got worse. */
    val backfired: Boolean = false,
    /** Suppress leaked — story returned as scandal. */
    val leaked: Boolean = false,
)

/**
 * National media desk — sentiment, credibility, leaks, and rolling front page.
 */
@Serializable
data class PressState(
    /** 0 = hostile press, 100 = glowing coverage. */
    val mediaSentiment: Float = 52f,
    /**
     * How much the public trusts official clarifications.
     * Low credibility makes spin more likely to backfire.
     */
    val credibility: Float = 55f,
    /**
     * Chance that a suppressed story resurfaces as a leak (0–100).
     */
    val leakRisk: Float = 8f,
    /**
     * Effective press freedom (0 = total control, 100 = free).
     * Censorship laws clamp the ceiling; free press raises scandal volume.
     */
    val pressFreedom: Float = 62f,
    val headlines: List<PressHeadline> = emptyList(),
    val narrativeArcs: List<PressNarrativeArc> = emptyList(),
    val spinCooldownMonths: Int = 0,
    val suppressCooldownMonths: Int = 0,
    val storiesSpun: Int = 0,
    val storiesSuppressed: Int = 0,
    val spinBackfires: Int = 0,
    val suppressLeaks: Int = 0,
    val scandalsSurvived: Int = 0,
    val lastDeskNote: String = "",
    /** Running log of desk actions for the analytics / briefing flavor. */
    val deskLog: List<String> = emptyList(),
) {
    val frontPage: List<PressHeadline>
        get() = headlines.filter { !it.handled || it.backfired || it.leaked }.take(5)
            .ifEmpty { headlines.take(5) }

    val hostileCount: Int
        get() = headlines.count {
            !it.handled && (it.tone == HeadlineTone.NEGATIVE || it.tone == HeadlineTone.SCANDAL)
        }

    val openScandalCount: Int
        get() = headlines.count { !it.handled && it.tone == HeadlineTone.SCANDAL }

    val hottestArc: PressNarrativeArc?
        get() = narrativeArcs.maxByOrNull { it.intensity }

    val sentimentLabel: String
        get() = when {
            mediaSentiment >= 70f -> "Friendly"
            mediaSentiment >= 55f -> "Balanced"
            mediaSentiment >= 40f -> "Skeptical"
            mediaSentiment >= 25f -> "Hostile"
            else -> "Toxic"
        }

    val credibilityLabel: String
        get() = when {
            credibility >= 65f -> "Trusted"
            credibility >= 45f -> "Mixed"
            credibility >= 30f -> "Thin"
            else -> "Shot"
        }

    fun electionMediaSwing(): Float {
        // Friendly press softens election night; toxic press hardens it.
        return ((mediaSentiment - 50f) * 0.08f) - (openScandalCount * 1.5f) -
            (hottestArc?.intensity?.div(25f) ?: 0f)
    }

    fun appendLog(line: String): PressState =
        copy(deskLog = (deskLog + line).takeLast(12))
}

fun PressState.summaryLine(): String =
    "Press ${sentimentLabel.lowercase()} · credibility ${credibility.roundToInt()} · " +
        "${hostileCount} hostile · leak risk ${leakRisk.roundToInt()}"
