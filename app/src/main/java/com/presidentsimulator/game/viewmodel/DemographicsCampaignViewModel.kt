package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.ElectionNightResult
import com.presidentsimulator.game.data.ElectionPollSnapshot
import com.presidentsimulator.game.data.ElectionSeasonState
import com.presidentsimulator.game.data.GameOverState
import com.presidentsimulator.game.data.SoftDefeatTrack
import com.presidentsimulator.game.data.TermEngine
import com.presidentsimulator.game.data.LegacyLedger
import com.presidentsimulator.game.data.GameState
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Cohort approval simulation, election theater, and alternate victory paths.
 */
class DemographicsCampaignViewModel(
    private val random: Random = Random.Default,
) {

    fun processDemographicsTick(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.demographics.election.hasPendingNight) return state

        val reasons = mutableListOf<String>()
        var working = state.demographics.workingClass
        var business = state.demographics.businessElite
        var military = state.demographics.military
        var academics = state.demographics.academics

        val cooldowns = state.demographics.campaignCooldownMonths
            .mapValues { (_, months) -> (months - 1).coerceAtLeast(0) }
            .filterValues { it > 0 }

        var opposition = state.demographics.oppositionMomentum
        var election = state.demographics.election
        val inSeason = isElectionSeason(state)

        if (inSeason) {
            if (election.challengerName.isBlank()) {
                election = spawnChallenger(state, election)
                reasons += "Challenger declared: ${election.challengerName} (${election.challengerParty})"
            }
            opposition = (opposition + when {
                state.vitals.approval < 45f -> 1.8f
                state.vitals.approval < 55f -> 1.1f
                else -> 0.4f
            }).coerceAtMost(25f)
            if (opposition > 8f) {
                reasons += "Election season — opposition momentum is building"
            }
            if (random.nextFloat() < 0.35f) {
                opposition = (opposition + 1.5f).coerceAtMost(25f)
                election = election.copy(oppositionAttackAds = election.oppositionAttackAds + 1)
                reasons += "${election.challengerName}'s campaign hits the airwaves"
            }
        } else {
            opposition = (opposition * 0.85f).coerceAtLeast(0f)
            if (election.challengerName.isNotBlank() && monthsUntilElection(state) > ELECTION_SEASON_MONTHS) {
                election = ElectionSeasonState()
            }
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

        working += (50f - working) * 0.04f
        business += (50f - business) * 0.04f
        military += (50f - military) * 0.04f
        academics += (50f - academics) * 0.04f

        var demo = state.demographics.withClamp(
            working = working,
            business = business,
            mil = military,
            academic = academics,
            reasons = state.demographics.recentReasons + reasons,
        ).copy(
            campaignCooldownMonths = cooldowns,
            oppositionMomentum = opposition,
            election = election,
        )

        if (inSeason) {
            demo = demo.copy(election = recordPoll(state.copy(demographics = demo), demo.election))
        }

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
        if (state.demographics.election.hasPendingNight) return state

        if (state.month == 1 && state.year >= state.nextElectionYear) {
            if (!state.term.canRunAgain) {
                val track = if (state.term.successorNamed.isNotBlank()) {
                    SoftDefeatTrack.SUCCESSION
                } else {
                    SoftDefeatTrack.TERM_LIMIT
                }
                return TermEngine.processMonth(
                    state.copy(
                        term = state.term.copy(softDefeatHeat = 100f, softDefeatTrack = track),
                    ),
                )
            }
            return stageElectionNight(state)
        }

        val victoryYear = state.scenario.victoryYearOverride ?: VICTORY_YEAR
        if (state.year >= victoryYear &&
            state.month == 12 &&
            state.vitals.approval >= VICTORY_APPROVAL &&
            state.internalSecurity.instabilityScore <= VICTORY_MAX_INSTABILITY
        ) {
            return state.copy(
                gameOver = GameOverState(
                    isGameOver = true,
                    isVictory = true,
                    reason = "Victory: A golden age — you guided ${state.playerNation.name} to $victoryYear with " +
                        "${state.vitals.approval.roundToInt()}% approval · legacy ${state.legacy.scores.grade}.",
                ),
            )
        }

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
                    reason = "Victory: Diplomatic hegemony — $allies major partners and a standing alliance crown your era.",
                ),
            )
        }
        return state
    }

    fun runCampaignAction(state: GameState, action: CampaignAction): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.demographics.election.hasPendingNight) return state
        if (campaignCooldownMonths(state, action) > 0) return state
        val seasonPremium = if (isElectionSeason(state)) 1.25f else 1f
        val totalCost = (action.cost * seasonPremium).toLong()
        if (state.vitals.budget < totalCost) return state

        val boost = action.boost * if (isElectionSeason(state)) 1.15f else 1f
        val demo = state.demographics
        val boosted = when (action) {
            CampaignAction.WORKER_RALLY -> demo.withClamp(
                working = demo.workingClass + boost,
                reasons = demo.recentReasons + "Worker rally energized the base",
            )
            CampaignAction.BUSINESS_ROUNDTABLE -> demo.withClamp(
                business = demo.businessElite + boost,
                reasons = demo.recentReasons + "Business roundtable secured elite donors",
            )
            CampaignAction.TROOP_VISIT -> demo.withClamp(
                mil = demo.military + boost,
                reasons = demo.recentReasons + "Troop visit steadied the ranks",
            )
            CampaignAction.CAMPUS_TOUR -> demo.withClamp(
                academic = demo.academics + boost,
                reasons = demo.recentReasons + "Campus tour won academic voices",
            )
            CampaignAction.NATIONAL_ADDRESS -> demo.withClamp(
                working = demo.workingClass + boost * 0.6f,
                business = demo.businessElite + boost * 0.4f,
                mil = demo.military + boost * 0.4f,
                academic = demo.academics + boost * 0.5f,
                reasons = demo.recentReasons + "National address moved the needle",
            )
            CampaignAction.DEBATE -> demo.withClamp(
                working = demo.workingClass + boost * 0.5f,
                academic = demo.academics + boost * 0.8f,
                business = demo.businessElite + boost * 0.3f,
                reasons = demo.recentReasons + "Debate performance shifted undecided voters",
            )
            CampaignAction.ATTACK_AD -> demo.withClamp(
                working = demo.workingClass + boost * 0.4f,
                business = demo.businessElite + boost * 0.2f,
                reasons = demo.recentReasons + "Attack ads dented the challenger's lead",
            )
        }

        val oppositionDrop = when (action) {
            CampaignAction.ATTACK_AD -> 2.5f
            CampaignAction.DEBATE -> 2.0f
            CampaignAction.NATIONAL_ADDRESS -> 1.5f
            else -> 0.8f
        }
        var election = boosted.election
        if (action == CampaignAction.DEBATE) {
            election = election.copy(debatesHeld = election.debatesHeld + 1)
        }
        if (action == CampaignAction.ATTACK_AD) {
            election = election.copy(attackAdsRun = election.attackAdsRun + 1)
        }

        val updatedDemo = boosted.copy(
            campaignCooldownMonths = boosted.campaignCooldownMonths + (action.name to action.cooldownMonths),
            oppositionMomentum = (boosted.oppositionMomentum - oppositionDrop).coerceAtLeast(0f),
            election = election,
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

    fun confirmElectionNight(state: GameState): GameState {
        val night = state.demographics.election.pendingNight ?: return state
        return if (night.victory) {
            val won = state.copy(
                nextElectionYear = state.year + ELECTION_TERM_YEARS,
                demographics = state.demographics.copy(
                    oppositionMomentum = 0f,
                    election = ElectionSeasonState(),
                    recentReasons = state.demographics.recentReasons +
                        "Re-elected over ${night.challengerName} with ${night.playerShare.roundToInt()}% of the vote",
                ),
            )
            val withLegacy = LegacyLedger.recordElectionVictory(won, night.challengerName)
            TermEngine.onElectionVictory(withLegacy)
        } else {
            val lost = state.copy(
                demographics = state.demographics.copy(
                    election = state.demographics.election.copy(pendingNight = null),
                ),
                gameOver = GameOverState(
                    isGameOver = true,
                    isVictory = false,
                    reason = "Game Over: Election defeat to ${night.challengerName} — " +
                        "you took ${night.playerShare.roundToInt()}% to their ${night.challengerShare.roundToInt()}%.",
                ),
            )
            LegacyLedger.recordElectionDefeat(lost, night.challengerName)
        }
    }

    fun campaignCooldownMonths(state: GameState, action: CampaignAction): Int =
        state.demographics.campaignCooldownMonths[action.name] ?: 0

    fun isElectionSeason(state: GameState): Boolean =
        monthsUntilElection(state) in 1..ELECTION_SEASON_MONTHS

    fun monthsUntilElection(state: GameState): Int {
        val monthsLeft = (state.nextElectionYear - state.year) * 12 + (12 - state.month)
        return monthsLeft.coerceAtLeast(0)
    }

    private fun stageElectionNight(state: GameState): GameState {
        val approval = state.vitals.approval
        val economyOk = state.netIncome >= 0 || state.vitals.budget > 20_000_000_000L
        val stabilityOk = state.internalSecurity.coupRisk < 55f
        val working = state.demographics.workingClass
        val business = state.demographics.businessElite
        val military = state.demographics.military
        val academics = state.demographics.academics
        val cohortFloor = listOf(working, business, military, academics).minOrNull() ?: approval

        val oppositionPenalty = state.demographics.oppositionMomentum * 0.4f
        val debateBonus = state.demographics.election.debatesHeld * 1.2f
        val adBonus = state.demographics.election.attackAdsRun * 0.8f
        val oppAdPenalty = state.demographics.election.oppositionAttackAds * 0.6f
        val mediaSwing = state.press.electionMediaSwing()
        val effectiveApproval = (
            approval - oppositionPenalty + debateBonus + adBonus - oppAdPenalty + mediaSwing
            ).coerceIn(0f, 100f)

        val winSeat = effectiveApproval >= ELECTION_APPROVAL &&
            economyOk &&
            stabilityOk &&
            cohortFloor >= ELECTION_COHORT_FLOOR

        val playerShare = if (winSeat) {
            (48f + (effectiveApproval - 48f) * 0.55f).coerceIn(48f, 62f)
        } else {
            (42f - (48f - effectiveApproval) * 0.4f).coerceIn(35f, 49f)
        }
        val challengerShare = (100f - playerShare - 2f).coerceIn(35f, 58f)
        val challenger = state.demographics.election.challengerName.ifBlank { "the opposition" }

        val narrative = buildString {
            if (winSeat) {
                append("Networks call it for you. ")
                append("$challenger concedes after a ${playerShare.roundToInt()}–${challengerShare.roundToInt()} split. ")
                if (!economyOk) append("Markets were nervous, but the coalition held. ")
                if (state.demographics.election.debatesHeld > 0) append("Debate nights paid off. ")
                if (mediaSwing >= 2f) append("Friendly press softened the landing. ")
            } else {
                append("The map turns against you. ")
                append("$challenger surges to ${challengerShare.roundToInt()}%. ")
                if (!economyOk) append("The economy dominated late ads. ")
                if (!stabilityOk) append("Security fears haunted swing districts. ")
                if (mediaSwing <= -2f) append("Hostile headlines haunted late voters. ")
                if (cohortFloor < ELECTION_COHORT_FLOOR) {
                    append("Your weakest bloc collapsed below the floor. ")
                }
            }
        }

        val night = ElectionNightResult(
            victory = winSeat,
            playerShare = playerShare,
            challengerShare = challengerShare,
            workingShare = working,
            businessShare = business,
            militaryShare = military,
            academicsShare = academics,
            economyOk = economyOk,
            stabilityOk = stabilityOk,
            oppositionPenalty = oppositionPenalty,
            challengerName = challenger,
            narrative = narrative.trim(),
        )

        return state.copy(
            demographics = state.demographics.copy(
                election = state.demographics.election.copy(pendingNight = night),
            ),
        )
    }

    private fun spawnChallenger(state: GameState, election: ElectionSeasonState): ElectionSeasonState {
        val main = state.opposition.mainOpposition
        if (main != null) {
            return election.copy(
                challengerName = main.leaderName,
                challengerParty = main.name,
            )
        }
        val names = listOf(
            "Helena Voss", "Marcus Quill", "Irena Sol", "David Renn",
            "Sofia Hart", "Julian Crowe", "Amara Finch", "Victor Lang",
        )
        val parties = listOf(
            "National Renewal", "People's Front", "Unity Coalition",
            "Reform Alliance", "Civic Mandate", "Liberty Bloc",
        )
        val seed = state.playerNation.id.hashCode() + state.nextElectionYear
        val rng = Random(seed)
        return election.copy(
            challengerName = names[rng.nextInt(names.size)],
            challengerParty = parties[rng.nextInt(parties.size)],
        )
    }

    private fun recordPoll(state: GameState, election: ElectionSeasonState): ElectionSeasonState {
        val oppositionPenalty = state.demographics.oppositionMomentum * 0.35f
        val player = (state.vitals.approval - oppositionPenalty * 0.5f).coerceIn(30f, 65f)
        val challenger = (42f + oppositionPenalty * 0.8f + election.oppositionAttackAds * 0.4f -
            election.attackAdsRun * 0.5f - election.debatesHeld * 0.8f).coerceIn(28f, 58f)
        val undecided = (100f - player - challenger).coerceIn(5f, 25f)
        val snap = ElectionPollSnapshot(
            year = state.year,
            month = state.month,
            playerShare = player,
            challengerShare = challenger,
            undecided = undecided,
        )
        return election.copy(pollHistory = (election.pollHistory + snap).takeLast(8))
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
    DEBATE("Presidential Debate", 4_000_000_000L, 4.5f, cooldownMonths = 4),
    ATTACK_AD("Attack Ad Blitz", 3_500_000_000L, 3.5f, cooldownMonths = 2),
}
