package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/** Passive national trait tied to the country chosen at new game. */
@Serializable
enum class NationalPerk(val label: String) {
    TRADE_HUB("+10% trade treaty & deal income"),
    FEDERATION_EXPORTS("+8% export revenue"),
    RAPID_MOBILIZATION("-12% recruitment cost"),
    AGRARIAN_HEARTLAND("+15% farm output"),
    POWER_PROJECTION("+15% combat strength"),
    MARITIME_COMMERCE("+20% maritime trade income"),
    INDUSTRIAL_FORGE("+10% materials output"),
    GREEN_RESILIENCE("+18% farm output & food buffer"),
}

object NationalPerkEffects {
    fun forNationId(nationId: String): NationalPerk = when (nationId) {
        "veltra" -> NationalPerk.TRADE_HUB
        "northland" -> NationalPerk.FEDERATION_EXPORTS
        "eastmark" -> NationalPerk.RAPID_MOBILIZATION
        "southreach" -> NationalPerk.AGRARIAN_HEARTLAND
        "westoria" -> NationalPerk.POWER_PROJECTION
        "aurumcoast" -> NationalPerk.MARITIME_COMMERCE
        "kryos" -> NationalPerk.INDUSTRIAL_FORGE
        "verdehaan" -> NationalPerk.GREEN_RESILIENCE
        else -> NationalPerk.TRADE_HUB
    }

    fun tradeIncomeMultiplier(perk: NationalPerk): Float = when (perk) {
        NationalPerk.TRADE_HUB -> 1.10f
        NationalPerk.FEDERATION_EXPORTS -> 1.08f
        NationalPerk.MARITIME_COMMERCE -> 1.20f
        else -> 1f
    }

    fun farmOutputMultiplier(perk: NationalPerk): Float = when (perk) {
        NationalPerk.AGRARIAN_HEARTLAND -> 1.15f
        NationalPerk.GREEN_RESILIENCE -> 1.18f
        else -> 1f
    }

    fun materialsOutputMultiplier(perk: NationalPerk): Float = when (perk) {
        NationalPerk.INDUSTRIAL_FORGE -> 1.10f
        else -> 1f
    }

    fun combatStrengthMultiplier(perk: NationalPerk): Float = when (perk) {
        NationalPerk.POWER_PROJECTION -> 1.15f
        else -> 1f
    }

    fun recruitCostMultiplier(perk: NationalPerk): Float = when (perk) {
        NationalPerk.RAPID_MOBILIZATION -> 0.88f
        else -> 1f
    }

    /** Extra food stock retained after consumption — softens shortage shocks. */
    fun foodSecurityBuffer(perk: NationalPerk): Long = when (perk) {
        NationalPerk.GREEN_RESILIENCE -> 400L
        else -> 0L
    }
}
