package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
enum class CabinetPortfolio(
    val displayName: String,
    val shortName: String,
    val targetRoute: String,
) {
    ECONOMY("Minister of Finance", "Finance", "economy"),
    INDUSTRY("Minister of Industry", "Industry", "economy"),
    DEFENSE("Minister of Defense", "Defense", "military"),
    FOREIGN("Foreign Minister", "Foreign", "diplomacy"),
    INTERIOR("Minister of Interior", "Interior", "secret_service"),
    SCIENCE("Minister of Science", "Science", "science"),
    HEALTH("Minister of Health", "Health", "laws_society"),
    EDUCATION("Minister of Education", "Education", "laws_society"),
    CULTURE("Minister of Culture", "Culture", "laws_society"),
}

@Serializable
enum class MinisterTrait(val displayName: String, val blurb: String) {
    COMPETENT("Competent", "Steady competence bonus"),
    LOYAL("Loyal", "Harder to resign; resists scandals"),
    CORRUPT("Corrupt", "Skims budget; scandal heat rises"),
    REFORMER("Reformer", "Boosts society gains; annoys elites"),
    HAWK("Hawk", "Military edge; diplomacy friction"),
    DIPLOMAT("Diplomat", "Softer relations drift"),
    POPULIST("Populist", "Approval bump; fiscal waste"),
    TECHNOCRAT("Technocrat", "Science and industry focus"),
    CRONY("Crony", "Loyal but mediocre"),
    IDEALIST("Idealist", "High integrity; fragile under heat"),
}

@Serializable
data class Minister(
    val id: String,
    val name: String,
    val portfolio: CabinetPortfolio,
    /** 0–100 job performance. */
    val competence: Float = 55f,
    /** 0–100 loyalty to the president. */
    val loyalty: Float = 55f,
    val traits: List<MinisterTrait> = emptyList(),
    val monthsInOffice: Int = 0,
    /** Scandal pressure; resigns or leaks when high. */
    val scandalHeat: Float = 0f,
    val originNote: String = "",
) {
    val isCorrupt: Boolean get() = MinisterTrait.CORRUPT in traits
    val isLoyal: Boolean get() = MinisterTrait.LOYAL in traits || MinisterTrait.CRONY in traits

    val grade: String
        get() = when {
            competence >= 80f -> "A"
            competence >= 65f -> "B"
            competence >= 50f -> "C"
            competence >= 35f -> "D"
            else -> "F"
        }

    fun effectWeight(): Float = ((competence - 50f) / 100f).coerceIn(-0.35f, 0.45f)
}

@Serializable
data class MinisterCandidate(
    val id: String,
    val name: String,
    val portfolio: CabinetPortfolio,
    val competence: Float,
    val loyalty: Float,
    val traits: List<MinisterTrait>,
    val hireCost: Long,
    val pitch: String,
)

/**
 * Aggregated cabinet modifiers applied across the sim.
 */
@Serializable
data class CabinetEffects(
    val productionMultiplier: Float = 1f,
    val scienceMultiplier: Float = 1f,
    val militaryStrengthMultiplier: Float = 1f,
    val militaryUpkeepMultiplier: Float = 1f,
    val societyGainMultiplier: Float = 1f,
    val diplomacyDrift: Float = 0f,
    val instabilityDelta: Float = 0f,
    val approvalDelta: Float = 0f,
    val budgetSkimPerMonth: Long = 0L,
)

@Serializable
data class CabinetState(
    val ministers: List<Minister> = emptyList(),
    val candidates: List<MinisterCandidate> = emptyList(),
    val fireCooldownMonths: Int = 0,
    val appointCooldownMonths: Int = 0,
    val reshuffleCooldownMonths: Int = 0,
    val resignationsThisTerm: Int = 0,
    val firingsThisTerm: Int = 0,
    val scandalsThisTerm: Int = 0,
    val lastCabinetNote: String = "",
    val cabinetLog: List<String> = emptyList(),
    val cohesion: Float = 55f,
) {
    val filledCount: Int get() = ministers.size
    val vacancyCount: Int get() = CabinetPortfolio.entries.size - ministers.size

    val vacantPortfolios: List<CabinetPortfolio>
        get() = CabinetPortfolio.entries.filter { p -> ministers.none { it.portfolio == p } }

    fun ministerFor(portfolio: CabinetPortfolio): Minister? =
        ministers.find { it.portfolio == portfolio }

    val weakestLink: Minister?
        get() = ministers.minByOrNull { it.competence * 0.6f + it.loyalty * 0.4f - it.scandalHeat }

    val hottestScandal: Minister?
        get() = ministers.filter { it.scandalHeat >= 35f }.maxByOrNull { it.scandalHeat }

    val cohesionLabel: String
        get() = when {
            cohesion >= 70f -> "United"
            cohesion >= 50f -> "Functional"
            cohesion >= 35f -> "Fractured"
            else -> "In Revolt"
        }

    fun appendLog(line: String): CabinetState =
        copy(cabinetLog = (cabinetLog + line).takeLast(16))

    fun combinedEffects(): CabinetEffects {
        if (ministers.isEmpty()) {
            return CabinetEffects(
                productionMultiplier = 0.94f,
                scienceMultiplier = 0.94f,
                militaryStrengthMultiplier = 0.95f,
                societyGainMultiplier = 0.92f,
                instabilityDelta = 0.4f,
                approvalDelta = -0.15f,
            )
        }

        var production = 1f
        var science = 1f
        var military = 1f
        var upkeep = 1f
        var society = 1f
        var diplomacy = 0f
        var instability = 0f
        var approval = 0f
        var skim = 0L

        // Vacancy penalty
        production -= vacancyCount * 0.015f
        science -= vacancyCount * 0.012f
        military -= vacancyCount * 0.01f
        society -= vacancyCount * 0.02f
        instability += vacancyCount * 0.25f

        ministers.forEach { m ->
            val w = m.effectWeight()
            when (m.portfolio) {
                CabinetPortfolio.ECONOMY -> {
                    production += w * 0.12f
                    if (MinisterTrait.POPULIST in m.traits) {
                        approval += 0.12f
                        skim += 400_000_000L
                    }
                }
                CabinetPortfolio.INDUSTRY -> {
                    production += w * 0.14f
                    if (MinisterTrait.TECHNOCRAT in m.traits) production += 0.03f
                }
                CabinetPortfolio.DEFENSE -> {
                    military += w * 0.16f
                    if (MinisterTrait.HAWK in m.traits) {
                        military += 0.04f
                        diplomacy -= 0.15f
                        upkeep += 0.03f
                    }
                }
                CabinetPortfolio.FOREIGN -> {
                    diplomacy += w * 0.35f
                    if (MinisterTrait.DIPLOMAT in m.traits) diplomacy += 0.2f
                    if (MinisterTrait.HAWK in m.traits) diplomacy -= 0.25f
                }
                CabinetPortfolio.INTERIOR -> {
                    instability -= w * 0.8f
                    if (MinisterTrait.LOYAL in m.traits) instability -= 0.2f
                }
                CabinetPortfolio.SCIENCE -> {
                    science += w * 0.18f
                    if (MinisterTrait.TECHNOCRAT in m.traits) science += 0.04f
                }
                CabinetPortfolio.HEALTH, CabinetPortfolio.EDUCATION, CabinetPortfolio.CULTURE -> {
                    society += w * 0.12f
                    if (MinisterTrait.REFORMER in m.traits) {
                        society += 0.04f
                        approval += 0.08f
                    }
                }
            }
            if (MinisterTrait.COMPETENT in m.traits) {
                when (m.portfolio) {
                    CabinetPortfolio.SCIENCE -> science += 0.03f
                    CabinetPortfolio.DEFENSE -> military += 0.03f
                    else -> production += 0.015f
                }
            }
            if (MinisterTrait.CORRUPT in m.traits) {
                skim += 650_000_000L + (m.competence * 8_000_000L).toLong()
                instability += 0.15f
            }
            if (MinisterTrait.CRONY in m.traits) {
                production -= 0.01f
                approval += 0.05f
            }
            if (MinisterTrait.IDEALIST in m.traits && m.scandalHeat > 40f) {
                approval -= 0.1f
            }
            instability += m.scandalHeat * 0.008f
        }

        // Cohesion softens or amplifies everything slightly
        val coh = ((cohesion - 50f) / 200f).coerceIn(-0.08f, 0.08f)
        production += coh
        science += coh
        military += coh * 0.5f

        return CabinetEffects(
            productionMultiplier = production.coerceIn(0.82f, 1.28f),
            scienceMultiplier = science.coerceIn(0.82f, 1.32f),
            militaryStrengthMultiplier = military.coerceIn(0.85f, 1.30f),
            militaryUpkeepMultiplier = upkeep.coerceIn(0.9f, 1.2f),
            societyGainMultiplier = society.coerceIn(0.75f, 1.35f),
            diplomacyDrift = diplomacy.coerceIn(-0.8f, 0.8f),
            instabilityDelta = instability.coerceIn(-1.5f, 2.5f),
            approvalDelta = approval.coerceIn(-0.5f, 0.5f),
            budgetSkimPerMonth = skim.coerceAtLeast(0L),
        )
    }

    fun summaryLine(): String =
        "$filledCount/${CabinetPortfolio.entries.size} seats · ${cohesionLabel.lowercase()} · " +
            (hottestScandal?.let { "heat on ${it.name}" } ?: "no open scandals")
}

fun CabinetState.effectsOrDefault(): CabinetEffects = combinedEffects()
