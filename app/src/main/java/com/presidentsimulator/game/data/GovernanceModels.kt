package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/** Legacy save id — migrated to [PlayerNation.id] on load. */
const val LEGACY_PLAYER_COUNTRY_ID = "player"

@Serializable
data class PlayerNation(
    val id: String = "veltra",
    val name: String = "Veltra",
    val flagEmoji: String = "🏛",
    val governmentLabel: String = "Republic",
    val nationalPerk: NationalPerk = NationalPerk.TRADE_HUB,
) {
    fun matchesCountryId(countryId: String): Boolean =
        countryId == id || countryId == LEGACY_PLAYER_COUNTRY_ID

    fun resolvedPerk(): NationalPerk = NationalPerkEffects.forNationId(id)
}

/**
 * Formal multi-nation coalition.
 */
@Serializable
data class Alliance(
    val allianceId: String,
    val name: String,
    val leaderCountryId: String,
    val memberCountryIds: List<String>,
    val sharedDefconLevel: Int,
) {
    val memberCount: Int get() = memberCountryIds.size
}

@Serializable
enum class ResolutionType(
    val displayName: String,
    val description: String,
    val influenceCost: Int,
    val votingDurationTicks: Int,
    val requiresTarget: Boolean,
) {
    GLOBAL_TAX(
        displayName = "Global Development Tax",
        description = "Levies a shared tax on all member economies to fund global programs.",
        influenceCost = 35,
        votingDurationTicks = 4,
        requiresTarget = false,
    ),
    NUCLEAR_EMBARGO(
        displayName = "Nuclear Embargo",
        description = "Bans nuclear weapons programs worldwide, reducing military force projection.",
        influenceCost = 45,
        votingDurationTicks = 5,
        requiresTarget = false,
    ),
    PEACEKEEPING_DEPLOYMENT(
        displayName = "Peacekeeping Deployment",
        description = "Deploys international peacekeepers, cooling active wars and boosting approval.",
        influenceCost = 40,
        votingDurationTicks = 4,
        requiresTarget = false,
    ),
    TRADE_SANCTIONS(
        displayName = "Trade Sanctions",
        description = "Imposes sanctions on a target nation, damaging their economy and relations.",
        influenceCost = 30,
        votingDurationTicks = 3,
        requiresTarget = true,
    ),
    WEAPONS_BAN(
        displayName = "Conventional Weapons Ban",
        description = "Limits heavy weapons procurement, reducing global military build-up.",
        influenceCost = 38,
        votingDurationTicks = 4,
        requiresTarget = false,
    ),
}

/**
 * A resolution currently on the UN floor.
 * [votesFor] / [votesAgainst] store country IDs that have cast ballots.
 */
@Serializable
data class UNResolution(
    val resolutionId: String,
    val type: ResolutionType,
    val proposerCountryId: String,
    val targetCountryId: String? = null,
    val votesFor: List<String> = emptyList(),
    val votesAgainst: List<String> = emptyList(),
    val votingTimeRemaining: Int,
) {
    val votesForCount: Int get() = votesFor.size
    val votesAgainstCount: Int get() = votesAgainst.size
    val totalVotes: Int get() = votesForCount + votesAgainstCount

    fun hasVoted(countryId: String): Boolean =
        countryId in votesFor || countryId in votesAgainst

    fun forFraction(): Float {
        val total = totalVotes
        if (total <= 0) return 0.5f
        return votesForCount.toFloat() / total.toFloat()
    }
}

/**
 * United Nations assembly state, coalitions, and UN diplomatic influence.
 */
@Serializable
data class GlobalGovernanceState(
    val activeResolution: UNResolution? = null,
    val activeAlliances: List<Alliance> = emptyList(),
    val diplomaticInfluence: Int = 45,
    val nuclearEmbargoActive: Boolean = false,
    val globalTaxActive: Boolean = false,
    val peacekeepingActive: Boolean = false,
    val weaponsBanActive: Boolean = false,
    /** Months remaining for timed UN effects (keys = [ResolutionType.name]). */
    val resolutionMonthsRemaining: Map<String, Int> = emptyMap(),
    val lastResolutionResult: String = "",
) {
    fun allianceById(id: String): Alliance? = activeAlliances.find { it.allianceId == id }

    fun alliancesFor(countryId: String): List<Alliance> =
        activeAlliances.filter { countryId in it.memberCountryIds }

    fun monthsRemaining(type: ResolutionType): Int =
        resolutionMonthsRemaining[type.name] ?: 0
}
