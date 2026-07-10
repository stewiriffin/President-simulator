package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.DemographicsState
import com.presidentsimulator.game.data.GameOverState
import com.presidentsimulator.game.data.GameState
import kotlin.math.roundToInt

/**
 * Cohort approval simulation, election cadence, and alternate victory paths.
 */
class DemographicsCampaignViewModel {

    fun processDemographicsTick(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state

        val reasons = mutableListOf<String>()
        var working = state.demographics.workingClass
        var business = state.demographics.businessElite
        var military = state.demographics.military
        var academics = state.demographics.academics

        val cooldowns = state.demographics.campaignCooldownMonths
            .mapValues { (_, months) -> (months - 1).coerceAtLeast(0) }
            .filterValues { it > 0 }

        var opposition = state.demographics.oppositionMomentum
        if (isElectionSeason(state)) {
            opposition = (opposition + when {
                state.vitals.approval < 45f -> 1.8f
                state.vitals.approval < 55f -> 1.1f
                else -> 0.4f
            }).coerceAtMost(25f)
            if (opposition > 8f) {
                reasons += "Election season — opposition momentum is building"
            }
        } else {
            opposition = (opposition * 0.85f).coerceAtLeast(0f)
        }

        val tax = state.economy.taxRate
        working += (0.20f - tax) * 40f
        business += (0.28f - tax) * 55f
        if (tax > 0.30f) reasons += "High taxes squeeze households and firms"
        if (tax < 0.18f) reasons += "Low taxes buoy business and consumers"

        if (state.production.foodShortage) {
            working -= 4f
            academics -= 1.5f
            reasons += "Food shortages erode working-class support"
        }
        if (state.production.energyShortage) {
            business -= 3f
            working -= 1.5f
            reasons += "Energy shortages disrupt industry"
        }

        working += (state.economy.factories - 20) * 0.15f
        business += state.trade.activeDeals.size * 0.8f
        military += (state.military.salaryFunding - 1f) * 8f
        military += if (state.diplomacy.activeWar != null) 1.5f else -0.3f
        if (state.military.morale < 45f) {
            military -= 3f
            reasons += "Troop morale is critically low"
        }

        academics += (state.society.educationFunding - 0.5f) * 10f
        academics += state.research.unlockedTechIds.size * 0.4f
        academics += (state.society.universities - 6) * 0.6f
        academics += (state.society.cultureFunding - 0.4f) * 6f
        working += (state.society.healthFunding - 0.45f) * 6f
        working += (state.society.cultureScore - 50f) * 0.04f
        if (state.society.cultureFunding >= 0.65f) {
            reasons += "Strong cultural funding lifts civic mood"
        }

        if (state.legal.isActive("universal_healthcare")) working += 1.2f
        if (state.legal.isActive("minimum_wage")) {
            working += 1.5f
            business -= 1.2f
        }
        if (state.legal.isActive("industrial_subsidies")) business += 1.4f
        if (state.legal.isActive("censorship")) academics -= 2f
        if (state.legal.isActive("conscription")) {
            military += 1.5f
            academics -= 1.5f
            working -= 0.8f
        }
        if (state.legal.isActive("defense_spending_act")) military += 1.2f

        // Mean-revert slightly toward 50 so deltas don't explode.
        working += (50f - working) * 0.04f
        business += (50f - business) * 0.04f
        military += (50f - military) * 0.04f
        academics += (50f - academics) * 0.04f

        val demo = state.demographics.withClamp(
            working = working,
            business = business,
            mil = military,
            academic = academics,
            reasons = state.demographics.recentReasons + reasons,
        ).copy(
            campaignCooldownMonths = cooldowns,
            oppositionMomentum = opposition,
        )
        val blended = (demo.blendedApproval() - opposition * 0.35f).coerceIn(0f, 100f)

        var next = state.copy(
            demographics = demo,
            vitals = state.vitals.copy(approval = blended),
        )
        next = evaluateCampaignOutcomes(next)
        return next
    }

    fun evaluateCampaignOutcomes(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state

        // Election every 4 years on January of election year.
        if (state.month == 1 && state.year >= state.nextElectionYear) {
            return resolveElection(state)
        }

        // Legacy / golden-age victory: reach target year with strong mandate.
        if (state.year >= VICTORY_YEAR &&
            state.month == 12 &&
            state.vitals.approval >= VICTORY_APPROVAL &&
            state.internalSecurity.instabilityScore <= VICTORY_MAX_INSTABILITY
        ) {
            return state.copy(
                gameOver = GameOverState(
                    isGameOver = true,
                    isVictory = true,
                    reason = "Victory: A golden age — you guided ${state.playerNation.name} to $VICTORY_YEAR with " +
                        "${state.vitals.approval.roundToInt()}% approval and lasting stability.",
                ),
            )
        }

        // Diplomatic hegemony victory.
        val allies = state.diplomacy.rivals.count { it.relationshipScore >= 70 }
        if (allies >= HEGEMONY_ALLY_COUNT &&
            state.governance.activeAlliances.isNotEmpty() &&
            state.vitals.approval >= 60f &&
            state.year >= HEGEMONY_MIN_YEAR
        ) {
            return state.copy(
                gameOver = GameOverState(
                    isGameOver = true,
                    isVictory = true,
                    reason = "Victory: Diplomatic hegemony — $allies major partners and a formal " +
                        "alliance network secured ${state.playerNation.name}'s global leadership.",
                ),
            )
        }

        return state
    }

    fun runCampaignAction(state: GameState, action: CampaignAction): GameState {
        if (state.gameOver.isGameOver) return state
        val cooldown = state.demographics.campaignCooldownMonths[action.name] ?: 0
        if (cooldown > 0) return state

        val electionPremium = if (isElectionSeason(state)) 1.25f else 1f
        val totalCost = (action.cost * electionPremium).toLong()
        if (state.vitals.budget < totalCost) return state

        val boostMultiplier = if (isElectionSeason(state)) 1.2f else 1f
        val effectiveBoost = action.boost * boostMultiplier

        val demo = state.demographics
        val boosted = when (action) {
            CampaignAction.WORKER_RALLY -> demo.withClamp(
                working = demo.workingClass + effectiveBoost,
                reasons = demo.recentReasons + "Worker rally boosted working-class support",
            )
            CampaignAction.BUSINESS_ROUNDTABLE -> demo.withClamp(
                business = demo.businessElite + effectiveBoost,
                reasons = demo.recentReasons + "Business roundtable swayed elites",
            )
            CampaignAction.TROOP_VISIT -> demo.withClamp(
                mil = demo.military + effectiveBoost,
                reasons = demo.recentReasons + "Troop visit lifted military morale",
            )
            CampaignAction.CAMPUS_TOUR -> demo.withClamp(
                academic = demo.academics + effectiveBoost,
                reasons = demo.recentReasons + "Campus tour won academic backing",
            )
            CampaignAction.NATIONAL_ADDRESS -> demo.withClamp(
                working = demo.workingClass + effectiveBoost,
                business = demo.businessElite + effectiveBoost,
                mil = demo.military + effectiveBoost * 0.6f,
                academic = demo.academics + effectiveBoost * 0.8f,
                reasons = demo.recentReasons + "National address lifted every bloc",
            )
        }
        val oppositionDrop = if (isElectionSeason(state)) 2.5f else 0f
        val updatedDemo = boosted.copy(
            campaignCooldownMonths = boosted.campaignCooldownMonths + (action.name to action.cooldownMonths),
            oppositionMomentum = (boosted.oppositionMomentum - oppositionDrop).coerceAtLeast(0f),
        )
        val blended = (updatedDemo.blendedApproval() - updatedDemo.oppositionMomentum * 0.35f)
            .coerceIn(0f, 100f)
        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - totalCost,
                approval = blended,
            ),
            demographics = updatedDemo,
        )
    }

    fun campaignCooldownMonths(state: GameState, action: CampaignAction): Int =
        state.demographics.campaignCooldownMonths[action.name] ?: 0

    fun isElectionSeason(state: GameState): Boolean =
        monthsUntilElection(state) in 1..ELECTION_SEASON_MONTHS

    fun monthsUntilElection(state: GameState): Int {
        val monthsLeft = (state.nextElectionYear - state.year) * 12 + (12 - state.month)
        return monthsLeft.coerceAtLeast(0)
    }

    private fun resolveElection(state: GameState): GameState {
        val approval = state.vitals.approval
        val economyOk = state.netIncome >= 0 || state.vitals.budget > 20_000_000_000L
        val stabilityOk = state.internalSecurity.coupRisk < 55f
        val cohortFloor = listOf(
            state.demographics.workingClass,
            state.demographics.businessElite,
            state.demographics.military,
            state.demographics.academics,
        ).minOrNull() ?: approval

        val oppositionPenalty = state.demographics.oppositionMomentum * 0.4f
        val effectiveApproval = approval - oppositionPenalty

        val winSeat = effectiveApproval >= ELECTION_APPROVAL &&
            economyOk &&
            stabilityOk &&
            cohortFloor >= ELECTION_COHORT_FLOOR

        return if (winSeat) {
            state.copy(
                nextElectionYear = state.year + ELECTION_TERM_YEARS,
                demographics = state.demographics.copy(
                    oppositionMomentum = 0f,
                    recentReasons = state.demographics.recentReasons +
                        "Re-elected with ${approval.roundToInt()}% national mandate",
                ),
            )
        } else {
            state.copy(
                gameOver = GameOverState(
                    isGameOver = true,
                    isVictory = false,
                    reason = "Game Over: Election defeat — the public denied another term " +
                        "(approval ${approval.roundToInt()}%, weakest bloc ${cohortFloor.roundToInt()}%).",
                ),
            )
        }
    }

    companion object {
        const val ELECTION_TERM_YEARS = 4
        const val ELECTION_APPROVAL = 48f
        const val ELECTION_COHORT_FLOOR = 35f
        const val ELECTION_SEASON_MONTHS = 6
        const val VICTORY_YEAR = 2046
        const val VICTORY_APPROVAL = 70f
        const val VICTORY_MAX_INSTABILITY = 35f
        const val HEGEMONY_ALLY_COUNT = 4
        const val HEGEMONY_MIN_YEAR = 2034
    }
}

enum class CampaignAction(
    val displayName: String,
    val cost: Long,
    val boost: Float,
    val cooldownMonths: Int = 3,
) {
    WORKER_RALLY("Worker Rally", 2_500_000_000L, 5f),
    BUSINESS_ROUNDTABLE("Business Roundtable", 3_000_000_000L, 6f),
    TROOP_VISIT("Troop Visit", 2_000_000_000L, 5.5f),
    CAMPUS_TOUR("Campus Tour", 2_200_000_000L, 5f),
    NATIONAL_ADDRESS("National Address", 5_000_000_000L, 3.5f, cooldownMonths = 6),
}
