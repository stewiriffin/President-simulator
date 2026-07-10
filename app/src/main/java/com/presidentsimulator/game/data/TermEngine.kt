package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
enum class SoftDefeatTrack(val displayName: String) {
    NONE("None"),
    LAME_DUCK("Lame Duck"),
    FORCED_RESIGNATION("Forced Resignation"),
    SUCCESSION("Succession Crisis"),
    TERM_LIMIT("Term Limit"),
}

@Serializable
data class TermState(
    val termsServed: Int = 1,
    val termLimit: Int = 2,
    val canRunAgain: Boolean = true,
    val successorNamed: String = "",
    val softDefeatHeat: Float = 0f,
    val softDefeatTrack: SoftDefeatTrack = SoftDefeatTrack.NONE,
    val lastTermNote: String = "",
) {
    val termsRemaining: Int
        get() = (termLimit - termsServed).coerceAtLeast(0)

    val atTermLimit: Boolean
        get() = termsServed >= termLimit

    fun summaryLine(): String =
        "Term $termsServed/$termLimit · " +
            if (canRunAgain) "eligible" else "cannot run" +
            if (softDefeatHeat >= 20f) " · pressure ${softDefeatHeat.roundToInt()}" else ""
}

object TermEngine {

    fun onElectionVictory(state: GameState): GameState {
        val terms = state.term.copy(termsServed = state.term.termsServed + 1)
        val atLimit = terms.termsServed >= terms.termLimit
        return state.copy(
            term = terms.copy(
                canRunAgain = !atLimit,
                softDefeatHeat = if (atLimit) (terms.softDefeatHeat + 15f).coerceAtMost(100f) else 0f,
                softDefeatTrack = if (atLimit) SoftDefeatTrack.TERM_LIMIT else SoftDefeatTrack.NONE,
                lastTermNote = if (atLimit) {
                    "Final term — name a successor or rewrite the rules."
                } else {
                    "Mandate renewed · term ${terms.termsServed} of ${terms.termLimit}."
                },
            ),
        )
    }

    fun processMonth(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state
        var term = state.term
        var heat = term.softDefeatHeat
        var track = term.softDefeatTrack
        var note = term.lastTermNote

        // Soft defeat pressure from prolonged failure
        if (state.vitals.approval < 28f) heat += 2.5f
        if (state.internalSecurity.coupRisk >= 70f) heat += 3f
        if (state.opposition.noConfidenceHeat >= 70f) heat += 2f
        if (state.legacy.scores.overall < 35) heat += 1.5f
        if (state.vitals.approval >= 55f && state.internalSecurity.coupRisk < 40f) {
            heat = (heat - 1.2f).coerceAtLeast(0f)
        }

        if (term.atTermLimit && !term.canRunAgain && term.successorNamed.isBlank()) {
            heat += 1.5f
            track = SoftDefeatTrack.TERM_LIMIT
            note = "Term-limited with no successor — lame-duck pressure rises."
        } else if (heat >= 55f && state.vitals.approval < 32f) {
            track = SoftDefeatTrack.LAME_DUCK
            note = "Lame-duck narrative takes hold."
        } else if (heat >= 75f && state.cabinet.cohesion < 30f) {
            track = SoftDefeatTrack.FORCED_RESIGNATION
            note = "Cabinet and street pressure for resignation."
        }

        heat = heat.coerceIn(0f, 100f)

        // Soft defeat resolution — not instant game over, but a staged exit
        if (heat >= 95f && track != SoftDefeatTrack.NONE && track != SoftDefeatTrack.SUCCESSION) {
            return triggerSoftDefeat(state, track, heat)
        }

        return state.copy(
            term = term.copy(
                softDefeatHeat = heat,
                softDefeatTrack = track,
                lastTermNote = note,
            ),
        )
    }

    fun nameSuccessor(state: GameState, name: String): GameState {
        if (name.isBlank()) return state
        return state.copy(
            term = state.term.copy(
                successorNamed = name.trim(),
                softDefeatHeat = (state.term.softDefeatHeat - 12f).coerceAtLeast(0f),
                softDefeatTrack = SoftDefeatTrack.SUCCESSION,
                lastTermNote = "Successor named: ${name.trim()}.",
            ),
            vitals = state.vitals.copy(
                approval = (state.vitals.approval + 1.5f).coerceIn(0f, 100f),
            ),
            demographics = state.demographics.withClamp(
                reasons = state.demographics.recentReasons + "Named successor ${name.trim()}",
            ),
        )
    }

    fun extendTermLimit(state: GameState): GameState {
        val cost = 8_000_000_000L
        if (state.vitals.budget < cost) return state
        if (state.legal.ideology == Ideology.DEMOCRACY && state.opposition.hasMajority.not()) {
            // Democracies need majority to extend — still allow but harsher
        }
        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - cost,
                approval = (state.vitals.approval - 6f).coerceIn(0f, 100f),
            ),
            term = state.term.copy(
                termLimit = state.term.termLimit + 1,
                canRunAgain = true,
                softDefeatHeat = (state.term.softDefeatHeat + 10f).coerceAtMost(100f),
                lastTermNote = "Term limit extended to ${state.term.termLimit + 1} — democracy bruises.",
            ),
            press = state.press.copy(
                mediaSentiment = (state.press.mediaSentiment - 8f).coerceIn(0f, 100f),
                credibility = (state.press.credibility - 5f).coerceIn(10f, 100f),
            ),
            opposition = state.opposition.copy(
                parties = state.opposition.parties.map { p ->
                    if (!p.isRuling) p.copy(hostility = (p.hostility + 12f).coerceAtMost(100f)) else p
                },
            ),
            demographics = state.demographics.withClamp(
                academic = state.demographics.academics - 4f,
                reasons = state.demographics.recentReasons + "Term limit extended",
            ),
        )
    }

    private fun triggerSoftDefeat(
        state: GameState,
        track: SoftDefeatTrack,
        heat: Float,
    ): GameState {
        val reason = when (track) {
            SoftDefeatTrack.LAME_DUCK ->
                "Soft defeat: lame-duck collapse — you lost the room before the ballot."
            SoftDefeatTrack.FORCED_RESIGNATION ->
                "Soft defeat: forced resignation under cabinet and street pressure."
            SoftDefeatTrack.TERM_LIMIT ->
                if (state.term.successorNamed.isNotBlank()) {
                    "Term ends — power passes to ${state.term.successorNamed}. Legacy closes."
                } else {
                    "Soft defeat: term-limited with no viable succession."
                }
            SoftDefeatTrack.SUCCESSION ->
                "Succession complete — ${state.term.successorNamed} takes the oath."
            SoftDefeatTrack.NONE -> return state
        }
        val victory = track == SoftDefeatTrack.SUCCESSION ||
            (track == SoftDefeatTrack.TERM_LIMIT && state.term.successorNamed.isNotBlank() &&
                state.legacy.scores.overall >= 55)
        return state.copy(
            term = state.term.copy(softDefeatHeat = heat, lastTermNote = reason),
            gameOver = GameOverState(
                isGameOver = true,
                isVictory = victory,
                reason = reason + " Legacy: ${state.legacy.scores.grade} (${state.legacy.scores.overall}).",
            ),
        )
    }
}
