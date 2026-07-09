package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

@Serializable
enum class EconomicSector(val displayName: String) {
    INDUSTRY("Heavy Industry"),
    AGRICULTURE("Agriculture"),
    HOUSING("Housing"),
    ENERGY("Energy"),
    MINING("Mining"),
    TECHNOLOGY("Technology"),
    DEFENSE("Defense"),
}

/**
 * Persistent sector XP from ministry investments — drives production bonuses and UI levels.
 */
@Serializable
data class SectorInvestmentState(
    val xpBySector: Map<String, Int> = emptyMap(),
) {
    fun xp(sector: EconomicSector): Int = xpBySector[sector.name] ?: 0

    fun level(sector: EconomicSector): Int = (xp(sector) / 100).coerceIn(0, 10)

    fun progressPercent(sector: EconomicSector): Int =
        (xp(sector) % 100).coerceIn(0, 99)

    fun productionMultiplier(): Float {
        val totalLevels = EconomicSector.entries.sumOf { level(it) }
        return 1f + totalLevels * 0.012f
    }

    fun withXp(sector: EconomicSector, amount: Int): SectorInvestmentState {
        val next = (xp(sector) + amount).coerceAtMost(1_000)
        return copy(xpBySector = xpBySector + (sector.name to next))
    }
}

object SectorInvestment {
    const val XP_PER_BUILD = 18
    const val XP_PER_UNIVERSITY = 25
    const val XP_PER_TANK_BATCH = 12

    fun sectorForInfrastructure(type: InfrastructureType): EconomicSector? = when (type) {
        InfrastructureType.FACTORY -> EconomicSector.INDUSTRY
        InfrastructureType.FARM -> EconomicSector.AGRICULTURE
        InfrastructureType.HOUSING -> EconomicSector.HOUSING
        InfrastructureType.POWER_PLANT -> EconomicSector.ENERGY
        InfrastructureType.MINE -> EconomicSector.MINING
    }
}

fun GameState.awardSectorXp(sector: EconomicSector, amount: Int): GameState =
    copy(
        economy = economy.copy(
            sectorInvestment = economy.sectorInvestment.withXp(sector, amount),
        ),
    )
