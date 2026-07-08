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
        working += (state.society.healthFunding - 0.45f) * 6f

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
        )
        val blended = demo.blendedApproval()

        var next = state.copy(
            demographics = demo,
            vitals = state.vitals.copy(approval = blended.coerceIn(0f, 100f)),
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
                    reason = "Victory: A golden age — you guided Veltria to $VICTORY_YEAR with " +
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
                        "alliance network secured Veltria's global leadership.",
                ),
            )
        }

        return state
    }

    fun runCampaignAction(state: GameState, action: CampaignAction): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.vitals.budget < action.cost) return state

        val demo = state.demographics
        val boosted = when (action) {
            CampaignAction.WORKER_RALLY -> demo.withClamp(
                working = demo.workingClass + action.boost,
                reasons = demo.recentReasons + "Worker rally boosted working-class support",
            )
            CampaignAction.BUSINESS_ROUNDTABLE -> demo.withClamp(
                business = demo.businessElite + action.boost,
                reasons = demo.recentReasons + "Business roundtable swayed elites",
            )
            CampaignAction.TROOP_VISIT -> demo.withClamp(
                mil = demo.military + action.boost,
                reasons = demo.recentReasons + "Troop visit lifted military morale",
            )
            CampaignAction.CAMPUS_TOUR -> demo.withClamp(
                academic = demo.academics + action.boost,
                reasons = demo.recentReasons + "Campus tour won academic backing",
            )
            CampaignAction.NATIONAL_ADDRESS -> demo.withClamp(
                working = demo.workingClass + action.boost,
                business = demo.businessElite + action.boost,
                mil = demo.military + action.boost * 0.6f,
                academic = demo.academics + action.boost * 0.8f,
                reasons = demo.recentReasons + "National address lifted every bloc",
            )
        }
        val blended = boosted.blendedApproval()
        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - action.cost,
                approval = blended.coerceIn(0f, 100f),
            ),
            demographics = boosted,
        )
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

        val winSeat = approval >= ELECTION_APPROVAL &&
            economyOk &&
            stabilityOk &&
            cohortFloor >= ELECTION_COHORT_FLOOR

        return if (winSeat) {
            state.copy(
                nextElectionYear = state.year + ELECTION_TERM_YEARS,
                demographics = state.demographics.copy(
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
) {
    WORKER_RALLY("Worker Rally", 2_500_000_000L, 5f),
    BUSINESS_ROUNDTABLE("Business Roundtable", 3_000_000_000L, 6f),
    TROOP_VISIT("Troop Visit", 2_000_000_000L, 5.5f),
    CAMPUS_TOUR("Campus Tour", 2_200_000_000L, 5f),
    NATIONAL_ADDRESS("National Address", 5_000_000_000L, 3.5f),
}
