package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
import kotlin.random.Random

@Serializable
enum class SpeechTheme(val displayName: String, val cost: Long) {
    UNITY("Unity Address", 1_200_000_000L),
    ECONOMY("Economic Vision", 1_500_000_000L),
    SECURITY("Security Hardline", 1_400_000_000L),
    REFORM("Reform Pledge", 1_600_000_000L),
    WARTIME("Wartime Address", 1_800_000_000L),
    CONCESSION("Conciliatory Tone", 1_000_000_000L),
}

@Serializable
data class SpeechRecord(
    val id: String,
    val theme: SpeechTheme,
    val year: Int,
    val month: Int,
    val success: Boolean,
    val note: String,
)

@Serializable
data class SpeechState(
    val cooldownMonths: Int = 0,
    val speechesGiven: Int = 0,
    val pressConferencesHeld: Int = 0,
    val lastSpeechNote: String = "",
    val history: List<SpeechRecord> = emptyList(),
    val rhetoricSkill: Float = 50f,
) {
    fun summaryLine(): String =
        "Rhetoric ${rhetoricSkill.roundToInt()} · speeches $speechesGiven · pressers $pressConferencesHeld"
}

object SpeechEngine {
    const val PRESSER_COST = 800_000_000L
    const val PRESSER_COOLDOWN = 2
    const val SPEECH_COOLDOWN = 3

    fun deliverSpeech(state: GameState, theme: SpeechTheme, random: Random = Random.Default): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.speech.cooldownMonths > 0) return state
        if (state.vitals.budget < theme.cost) return state

        val skill = state.speech.rhetoricSkill
        val pressMod = (state.press.mediaSentiment - 50f) / 100f
        val baseChance = 0.55f + (skill - 50f) / 200f + pressMod * 0.2f
        val themeBonus = when {
            theme == SpeechTheme.WARTIME && state.diplomacy.activeWar != null -> 0.15f
            theme == SpeechTheme.ECONOMY && state.netIncome > 0 -> 0.1f
            theme == SpeechTheme.SECURITY && state.internalSecurity.coupRisk >= 40f -> 0.1f
            theme == SpeechTheme.CONCESSION && state.opposition.mainOpposition != null -> 0.08f
            theme == SpeechTheme.UNITY && state.vitals.approval < 45f -> 0.05f
            else -> 0f
        }
        val success = random.nextFloat() < (baseChance + themeBonus).coerceIn(0.2f, 0.92f)

        var approval = if (success) 2.5f + skill * 0.02f else -1.8f
        var working = 0f
        var business = 0f
        var military = 0f
        var academic = 0f
        var media = if (success) 3f else -4f
        var oppositionHostility = 0f
        var instability = 0f

        when (theme) {
            SpeechTheme.UNITY -> {
                working += if (success) 2f else -1f
                academic += if (success) 1.5f else -0.5f
            }
            SpeechTheme.ECONOMY -> {
                business += if (success) 3f else -2f
                working += if (success) 1.5f else -1f
            }
            SpeechTheme.SECURITY -> {
                military += if (success) 3f else -1.5f
                instability -= if (success) 1.5f else -1f
            }
            SpeechTheme.REFORM -> {
                academic += if (success) 3f else -1f
                business += if (success) -1f else 0.5f
                working += if (success) 2f else -1.5f
            }
            SpeechTheme.WARTIME -> {
                military += if (success) 4f else -2f
                approval += if (success) 1f else -2f
            }
            SpeechTheme.CONCESSION -> {
                oppositionHostility -= if (success) 8f else -3f
                approval -= 0.5f
                media += if (success) 2f else -2f
            }
        }

        val note = if (success) {
            "${theme.displayName} lands — the room rises."
        } else {
            "${theme.displayName} flops — clips go viral for the wrong reasons."
        }
        val record = SpeechRecord(
            id = java.util.UUID.randomUUID().toString(),
            theme = theme,
            year = state.year,
            month = state.month,
            success = success,
            note = note,
        )

        var opposition = state.opposition
        if (oppositionHostility != 0f) {
            opposition = opposition.copy(
                parties = opposition.parties.map { p ->
                    if (p.id == opposition.mainOpposition?.id) {
                        p.copy(hostility = (p.hostility + oppositionHostility).coerceIn(0f, 100f))
                    } else p
                },
            )
        }

        val headline = PressHeadline(
            id = java.util.UUID.randomUUID().toString(),
            title = if (success) "President delivers ${theme.displayName}" else "Speech misfires on live TV",
            lede = note,
            tone = if (success) HeadlineTone.POSITIVE else HeadlineTone.NEGATIVE,
            year = state.year,
            month = state.month,
            outlet = PressOutlet.STATE_BROADCAST,
            topic = PressTopic.SOCIETY,
        )

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - theme.cost,
                approval = (state.vitals.approval + approval).coerceIn(0f, 100f),
            ),
            speech = state.speech.copy(
                cooldownMonths = SPEECH_COOLDOWN,
                speechesGiven = state.speech.speechesGiven + 1,
                rhetoricSkill = (state.speech.rhetoricSkill + if (success) 1.5f else -0.8f)
                    .coerceIn(20f, 95f),
                lastSpeechNote = note,
                history = (state.speech.history + record).takeLast(12),
            ),
            demographics = state.demographics.withClamp(
                working = state.demographics.workingClass + working,
                business = state.demographics.businessElite + business,
                mil = state.demographics.military + military,
                academic = state.demographics.academics + academic,
                reasons = state.demographics.recentReasons + note,
            ),
            press = state.press.copy(
                headlines = (listOf(headline) + state.press.headlines).take(14),
                mediaSentiment = (state.press.mediaSentiment + media).coerceIn(0f, 100f),
                credibility = (state.press.credibility + if (success) 1.5f else -2.5f)
                    .coerceIn(10f, 100f),
            ),
            opposition = opposition,
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (state.internalSecurity.instabilityScore + instability)
                    .coerceIn(0f, 100f),
            ),
        )
    }

    fun holdPressConference(state: GameState, random: Random = Random.Default): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.speech.cooldownMonths > 0) return state
        if (state.vitals.budget < PRESSER_COST) return state

        val hostile = state.press.hostileCount
        val successChance = (0.5f + (state.speech.rhetoricSkill - 50f) / 180f -
            hostile * 0.06f - state.press.leakRisk * 0.003f).coerceIn(0.15f, 0.85f)
        val success = random.nextFloat() < successChance
        val note = if (success) {
            "Press conference steadies the narrative."
        } else {
            "Reporters draw blood — the clip dominates overnight."
        }

        // Soften one unhandled negative headline on success
        val headlines = if (success) {
            var softened = false
            state.press.headlines.map { h ->
                if (!softened && !h.handled && (h.tone == HeadlineTone.NEGATIVE || h.tone == HeadlineTone.SCANDAL)) {
                    softened = true
                    h.copy(
                        tone = HeadlineTone.NEUTRAL,
                        handled = true,
                        title = "Walked back: ${h.title.take(40)}",
                        lede = "The podium pushback blunted the story.",
                    )
                } else h
            }
        } else {
            state.press.headlines
        }

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - PRESSER_COST,
                approval = (state.vitals.approval + if (success) 1.8f else -2.2f).coerceIn(0f, 100f),
            ),
            speech = state.speech.copy(
                cooldownMonths = PRESSER_COOLDOWN,
                pressConferencesHeld = state.speech.pressConferencesHeld + 1,
                rhetoricSkill = (state.speech.rhetoricSkill + if (success) 1f else -0.5f)
                    .coerceIn(20f, 95f),
                lastSpeechNote = note,
            ),
            press = state.press.copy(
                headlines = headlines,
                mediaSentiment = (state.press.mediaSentiment + if (success) 4f else -5f)
                    .coerceIn(0f, 100f),
                credibility = (state.press.credibility + if (success) 2f else -3f)
                    .coerceIn(10f, 100f),
                lastDeskNote = note,
            ),
        )
    }

    fun tickCooldowns(state: GameState): GameState {
        if (state.speech.cooldownMonths <= 0) return state
        return state.copy(
            speech = state.speech.copy(
                cooldownMonths = (state.speech.cooldownMonths - 1).coerceAtLeast(0),
            ),
        )
    }
}
