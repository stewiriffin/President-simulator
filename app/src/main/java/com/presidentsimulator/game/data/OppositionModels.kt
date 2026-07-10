package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
enum class PartyLean(val displayName: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right"),
    NATIONALIST("Nationalist"),
    GREEN("Green"),
}

@Serializable
enum class OppositionTactic(val displayName: String) {
    FILIBUSTER("Filibuster"),
    STREET_PROTEST("Street Protest"),
    MEDIA_BLITZ("Media Blitz"),
    NO_CONFIDENCE("No-Confidence Drive"),
    WHIP_VOTE("Whip Against Bills"),
    POLICY_OFFER("Policy Olive Branch"),
}

@Serializable
data class PoliticalParty(
    val id: String,
    val name: String,
    val lean: PartyLean,
    /** Seat share of a 100-seat chamber. */
    val seats: Int,
    /** 0–100 national popularity. */
    val popularity: Float,
    val leaderName: String,
    val hostility: Float = 40f,
    val isRuling: Boolean = false,
    val platformTags: List<String> = emptyList(),
    val monthsAsLeader: Int = 0,
) {
    val isOpposition: Boolean get() = !isRuling
}

@Serializable
data class OppositionState(
    val parties: List<PoliticalParty> = emptyList(),
    val filibusterMonths: Int = 0,
    val noConfidenceHeat: Float = 0f,
    val lastOppositionAction: String = "",
    val lastPlayerCounter: String = "",
    val negotiateCooldownMonths: Int = 0,
    val smearCooldownMonths: Int = 0,
    val concessionCooldownMonths: Int = 0,
    val oppositionLog: List<String> = emptyList(),
    val billsBlockedThisTerm: Int = 0,
    val protestsThisTerm: Int = 0,
) {
    val chamberSeats: Int get() = parties.sumOf { it.seats }.coerceAtLeast(1)

    val rulingParty: PoliticalParty?
        get() = parties.find { it.isRuling } ?: parties.maxByOrNull { it.seats }

    val mainOpposition: PoliticalParty?
        get() = parties.filter { !it.isRuling }.maxByOrNull { it.seats }

    val oppositionSeats: Int
        get() = parties.filter { !it.isRuling }.sumOf { it.seats }

    val rulingSeats: Int
        get() = rulingParty?.seats ?: 0

    val majorityMargin: Int
        get() = rulingSeats - (chamberSeats / 2)

    val hasMajority: Boolean
        get() = rulingSeats > chamberSeats / 2

    val filibusterActive: Boolean
        get() = filibusterMonths > 0

    fun partyById(id: String): PoliticalParty? = parties.find { it.id == id }

    fun appendLog(line: String): OppositionState =
        copy(oppositionLog = (oppositionLog + line).takeLast(16))

    fun summaryLine(): String {
        val opp = mainOpposition
        return when {
            opp == null -> "No organized opposition"
            filibusterActive -> "Filibuster · ${opp.name} (${opp.leaderName})"
            noConfidenceHeat >= 50f -> "No-confidence heat ${noConfidenceHeat.roundToInt()}%"
            else -> "${opp.name} · ${opp.leaderName} · ${oppositionSeats} opp seats"
        }
    }

    /** Penalty applied to parliamentary support scores. */
    fun lawSupportPenalty(): Float {
        var penalty = 0f
        if (filibusterActive) penalty += 12f
        penalty += (noConfidenceHeat * 0.08f)
        penalty += parties.filter { !it.isRuling }.sumOf { it.hostility.toDouble() }.toFloat() * 0.04f
        if (!hasMajority) penalty += 8f
        return penalty.coerceIn(0f, 35f)
    }
}
