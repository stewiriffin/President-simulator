package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/**
 * Armed forces readiness, hardware, and deployment posture.
 * Monthly upkeep is deducted from the treasury during each time tick.
 */
@Serializable
data class MilitaryState(
    val personnel: Long = 450_000L,
    /** Currency cost per soldier per month at 100% salary funding. */
    val upkeepPerUnit: Long = 8_000L,
    /**
     * Salary / funding multiplier (0.5–1.5).
     * Higher funding raises morale and monthly personnel upkeep.
     */
    val salaryFunding: Float = 1.0f,
    val tanks: Int = 800,
    val jets: Int = 120,
    val ships: Int = 45,
    val nuclearArsenal: Int = 0,
    val deployment: DeploymentStatus = DeploymentStatus.DEFENSIVE,
    val defcon: Int = 4,
) {
    /** Legacy alias used by older UI formatters. */
    val armySize: Long get() = personnel

    /** Derived morale (0–100) from salary funding. */
    val morale: Float
        get() = (35f + salaryFunding.coerceIn(0.5f, 1.5f) * 45f).coerceIn(0f, 100f)

    val monthlyUpkeep: Long
        get() {
            val funding = salaryFunding.coerceIn(0.5f, 1.5f)
            val personnelCost = personnel * upkeepPerUnit * funding
            val hardwareCost = tanks * 250_000L +
                jets * 1_200_000L +
                ships * 2_000_000L +
                nuclearArsenal * 50_000_000L
            val postureMultiplier = when (deployment) {
                DeploymentStatus.DEFENSIVE -> 1.0
                DeploymentStatus.MOBILIZED -> 1.45
            }
            return ((personnelCost + hardwareCost) * postureMultiplier).toLong()
        }

    /**
     * Abstract combat power used by battle resolution.
     * Personnel contribute the bulk; armor, air, naval, and nuclear assets amplify force projection.
     */
    val combatStrength: Double
        get() {
            val base = personnel / 1_000.0 +
                tanks * 2.5 +
                jets * 8.0 +
                ships * 5.0 +
                nuclearArsenal * 25.0
            val postureBonus = when (deployment) {
                DeploymentStatus.DEFENSIVE -> 0.85
                DeploymentStatus.MOBILIZED -> 1.20
            }
            val defconBonus = when (defcon) {
                1 -> 1.15
                2 -> 1.10
                3 -> 1.05
                else -> 1.0
            }
            val moraleBonus = 0.80 + (morale / 100.0) * 0.40
            return base * postureBonus * defconBonus * moraleBonus
        }
}

/** Purchasable military hardware categories for the Defense ministry UI. */
enum class MilitaryHardware(
    val displayName: String,
    val unitStrength: Float,
    val unitCost: Long,
) {
    TANKS("Tanks", 2.5f, 15_000_000L),
    FIGHTER_JETS("Fighter Jets", 8.0f, 80_000_000L),
    NAVAL_SHIPS("Naval Ships", 5.0f, 120_000_000L),
    NUCLEAR_ARSENAL("Nuclear Arsenal", 25.0f, 2_000_000_000L),
}

@Serializable
enum class DeploymentStatus {
    DEFENSIVE,
    MOBILIZED,
}

/**
 * Active conflict against a single rival.
 * [warProgress] is -100 (total defeat) … +100 (total victory).
 */
@Serializable
data class WarState(
    val targetCountryId: String,
    val warProgress: Float = 0f,
    val playerCasualties: Long = 0L,
    val enemyCasualties: Long = 0L,
    val monthsActive: Int = 0,
)

/**
 * Bilateral relationship with one neighboring power.
 * [relationshipScore] ranges from -100 (hostile) to +100 (allied).
 */
@Serializable
data class RivalNation(
    val id: String,
    val name: String,
    val flagEmoji: String,
    val relationshipScore: Int,
    val militaryStrength: Double,
    val hasTradeTreaty: Boolean = false,
    val hasNonAggressionPact: Boolean = false,
) {
    val stance: DiplomaticStance
        get() = when {
            relationshipScore >= 40 -> DiplomaticStance.FRIENDLY
            relationshipScore <= -40 -> DiplomaticStance.HOSTILE
            else -> DiplomaticStance.NEUTRAL
        }
}

enum class DiplomaticStance(val label: String) {
    FRIENDLY("Friendly"),
    NEUTRAL("Neutral"),
    HOSTILE("Hostile"),
}

enum class TreatyType(
    val displayName: String,
    val budgetCost: Long,
    val influenceCost: Int,
    val relationshipBonus: Int,
) {
    TRADE(
        displayName = "Trade Treaty",
        budgetCost = 2_000_000_000L,
        influenceCost = 15,
        relationshipBonus = 20,
    ),
    NON_AGGRESSION(
        displayName = "Non-Aggression Pact",
        budgetCost = 1_000_000_000L,
        influenceCost = 20,
        relationshipBonus = 25,
    ),
    PEACE(
        displayName = "Peace Treaty",
        budgetCost = 3_000_000_000L,
        influenceCost = 10,
        relationshipBonus = 40,
    ),
}

/**
 * Foreign affairs portfolio: rival list, soft-power influence pool, and optional war.
 */
@Serializable
data class DiplomacyState(
    val rivals: List<RivalNation> = defaultRivals(),
    val diplomaticInfluence: Int = 50,
    val activeWar: WarState? = null,
) {
    fun rivalById(id: String): RivalNation? = rivals.find { it.id == id }

    fun updateRival(id: String, transform: (RivalNation) -> RivalNation): DiplomacyState =
        copy(rivals = rivals.map { if (it.id == id) transform(it) else it })

    companion object {
        fun defaultRivals(): List<RivalNation> = listOf(
            RivalNation(
                id = "northland",
                name = "Northland Federation",
                flagEmoji = "🟦",
                relationshipScore = 25,
                militaryStrength = 520.0,
                hasTradeTreaty = true,
            ),
            RivalNation(
                id = "eastmark",
                name = "Eastmark Republic",
                flagEmoji = "🟥",
                relationshipScore = -15,
                militaryStrength = 680.0,
            ),
            RivalNation(
                id = "southreach",
                name = "Southreach Union",
                flagEmoji = "🟩",
                relationshipScore = 10,
                militaryStrength = 410.0,
                hasNonAggressionPact = true,
            ),
            RivalNation(
                id = "westoria",
                name = "Westoria Empire",
                flagEmoji = "🟨",
                relationshipScore = -55,
                militaryStrength = 900.0,
            ),
        )
    }
}
