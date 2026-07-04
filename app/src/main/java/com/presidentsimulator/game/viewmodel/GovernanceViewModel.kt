package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.Alliance
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.GlobalGovernanceState
import com.presidentsimulator.game.data.PLAYER_COUNTRY_ID
import com.presidentsimulator.game.data.ResolutionType
import com.presidentsimulator.game.data.UNResolution
import java.util.UUID
import kotlin.random.Random

/**
 * Pure UN assembly and alliance simulation engine.
 * [GameViewModel] applies results through immutable [GameState.copy] updates.
 */
class GovernanceViewModel(
    private val random: Random = Random.Default,
) {

    /**
     * Monthly governance pipeline:
     * 1. Advance active resolution voting
     * 2. Simulate undecided AI ballots
     * 3. Resolve passed/failed resolutions and apply global modifiers
     * 4. Regenerate a small amount of diplomatic influence
     */
    fun processGovernanceTick(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state

        var next = state
        val resolution = next.governance.activeResolution
        if (resolution != null) {
            next = simulateAiVotes(next)
            val updated = next.governance.activeResolution
            if (updated != null) {
                val decremented = updated.copy(
                    votingTimeRemaining = updated.votingTimeRemaining - 1,
                )
                next = next.copy(
                    governance = next.governance.copy(activeResolution = decremented),
                )
                if (decremented.votingTimeRemaining <= 0) {
                    next = resolveResolution(next, decremented)
                }
            }
        }

        next = applyPassiveGovernanceEffects(next)
        next = next.copy(
            governance = next.governance.copy(
                diplomaticInfluence = (
                    next.governance.diplomaticInfluence + INFLUENCE_REGEN_PER_TICK
                    ).coerceIn(0, MAX_INFLUENCE),
            ),
        )
        return next
    }

    /**
     * Spends diplomatic influence to place a resolution on the UN floor.
     */
    fun proposeResolution(
        state: GameState,
        type: ResolutionType,
        targetCountryId: String? = null,
    ): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.governance.activeResolution != null) return state
        if (state.governance.diplomaticInfluence < type.influenceCost) return state
        if (type.requiresTarget) {
            if (targetCountryId.isNullOrBlank()) return state
            if (state.diplomacy.rivalById(targetCountryId) == null) return state
        }

        val resolution = UNResolution(
            resolutionId = UUID.randomUUID().toString(),
            type = type,
            proposerCountryId = PLAYER_COUNTRY_ID,
            targetCountryId = if (type.requiresTarget) targetCountryId else null,
            votesFor = listOf(PLAYER_COUNTRY_ID),
            votesAgainst = emptyList(),
            votingTimeRemaining = type.votingDurationTicks,
        )

        return state.copy(
            governance = state.governance.copy(
                activeResolution = resolution,
                diplomaticInfluence = state.governance.diplomaticInfluence - type.influenceCost,
                lastResolutionResult = "",
            ),
        )
    }

    /**
     * Spends treasury funds to lock an AI nation's vote.
     * [voteFor] true = vote for the resolution, false = vote against.
     */
    fun bribeCountryVote(
        state: GameState,
        countryId: String,
        voteFor: Boolean,
    ): GameState {
        if (state.gameOver.isGameOver) return state
        val resolution = state.governance.activeResolution ?: return state
        if (countryId == PLAYER_COUNTRY_ID) return state
        if (state.diplomacy.rivalById(countryId) == null) return state
        if (resolution.hasVoted(countryId)) return state
        if (state.vitals.budget < BRIBE_COST) return state

        val votesFor = resolution.votesFor.filterNot { it == countryId }.toMutableList()
        val votesAgainst = resolution.votesAgainst.filterNot { it == countryId }.toMutableList()
        if (voteFor) {
            votesFor += countryId
        } else {
            votesAgainst += countryId
        }

        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - BRIBE_COST),
            governance = state.governance.copy(
                activeResolution = resolution.copy(
                    votesFor = votesFor,
                    votesAgainst = votesAgainst,
                ),
            ),
            diplomacy = state.diplomacy.updateRival(countryId) {
                it.copy(
                    relationshipScore = (it.relationshipScore - 4).coerceIn(-100, 100),
                )
            },
        )
    }

    /**
     * Forms a player-led alliance with accepting invitees.
     * Nations at war with the player or with very low relations refuse.
     */
    fun formAlliance(
        state: GameState,
        name: String,
        invitees: List<String>,
    ): GameState {
        if (state.gameOver.isGameOver) return state
        val trimmedName = name.trim().ifBlank { "Coalition ${state.governance.activeAlliances.size + 1}" }
        if (invitees.isEmpty()) return state
        if (state.governance.diplomaticInfluence < ALLIANCE_INFLUENCE_COST) return state

        val accepted = invitees.filter { countryId ->
            val rival = state.diplomacy.rivalById(countryId) ?: return@filter false
            val atWarWithPlayer = state.diplomacy.activeWar?.targetCountryId == countryId
            !atWarWithPlayer && rival.relationshipScore >= ALLIANCE_MIN_RELATION
        }
        if (accepted.isEmpty()) return state

        val members = (listOf(PLAYER_COUNTRY_ID) + accepted).distinct()
        val sharedDefcon = members
            .map { memberId ->
                if (memberId == PLAYER_COUNTRY_ID) {
                    state.military.defcon
                } else {
                    // Rivals do not expose DEFCON; approximate from hostility.
                    val rival = state.diplomacy.rivalById(memberId)
                    when {
                        rival == null -> 4
                        rival.relationshipScore >= 40 -> 5
                        rival.relationshipScore <= -40 -> 2
                        else -> 4
                    }
                }
            }
            .minOrNull()
            ?: state.military.defcon

        val alliance = Alliance(
            allianceId = UUID.randomUUID().toString(),
            name = trimmedName,
            leaderCountryId = PLAYER_COUNTRY_ID,
            memberCountryIds = members,
            sharedDefconLevel = sharedDefcon.coerceIn(1, 5),
        )

        var diplomacy = state.diplomacy
        accepted.forEach { countryId ->
            diplomacy = diplomacy.updateRival(countryId) {
                it.copy(
                    relationshipScore = (it.relationshipScore + 8).coerceIn(-100, 100),
                    hasNonAggressionPact = true,
                )
            }
        }

        return state.copy(
            vitals = state.vitals.copy(
                approval = (state.vitals.approval + 2f).coerceIn(0f, 100f),
            ),
            diplomacy = diplomacy,
            governance = state.governance.copy(
                activeAlliances = state.governance.activeAlliances + alliance,
                diplomaticInfluence = (state.governance.diplomaticInfluence - ALLIANCE_INFLUENCE_COST)
                    .coerceAtLeast(0),
            ),
            military = state.military.copy(
                defcon = minOf(state.military.defcon, sharedDefcon).coerceIn(1, 5),
            ),
        )
    }

    fun dissolveAlliance(state: GameState, allianceId: String): GameState {
        if (state.gameOver.isGameOver) return state
        val alliance = state.governance.allianceById(allianceId) ?: return state
        if (alliance.leaderCountryId != PLAYER_COUNTRY_ID) return state
        return state.copy(
            governance = state.governance.copy(
                activeAlliances = state.governance.activeAlliances.filterNot {
                    it.allianceId == allianceId
                },
            ),
        )
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private fun simulateAiVotes(state: GameState): GameState {
        val resolution = state.governance.activeResolution ?: return state
        val undecided = state.diplomacy.rivals
            .map { it.id }
            .filterNot { resolution.hasVoted(it) }
        if (undecided.isEmpty()) return state

        // Each tick, a subset of undecided nations cast ballots.
        val votersThisTick = undecided.filter { random.nextFloat() < AI_VOTE_CHANCE_PER_TICK }
        if (votersThisTick.isEmpty()) return state

        var votesFor = resolution.votesFor.toMutableList()
        var votesAgainst = resolution.votesAgainst.toMutableList()

        votersThisTick.forEach { countryId ->
            val rival = state.diplomacy.rivalById(countryId) ?: return@forEach
            val supportChance = aiSupportChance(
                state = state,
                resolution = resolution,
                countryId = countryId,
                relationshipWithPlayer = rival.relationshipScore,
            )
            if (random.nextFloat() < supportChance) {
                votesFor += countryId
            } else {
                votesAgainst += countryId
            }
        }

        return state.copy(
            governance = state.governance.copy(
                activeResolution = resolution.copy(
                    votesFor = votesFor.distinct(),
                    votesAgainst = votesAgainst.distinct(),
                ),
            ),
        )
    }

    private fun aiSupportChance(
        state: GameState,
        resolution: UNResolution,
        countryId: String,
        relationshipWithPlayer: Int,
    ): Float {
        var chance = 0.45f + relationshipWithPlayer / 200f
        when (resolution.type) {
            ResolutionType.GLOBAL_TAX -> chance -= 0.05f
            ResolutionType.NUCLEAR_EMBARGO -> chance += 0.08f
            ResolutionType.PEACEKEEPING_DEPLOYMENT -> chance += 0.10f
            ResolutionType.TRADE_SANCTIONS -> {
                val target = resolution.targetCountryId
                if (target != null) {
                    val targetRival = state.diplomacy.rivalById(target)
                    // Nations that dislike the target more readily support sanctions.
                    if (targetRival != null && targetRival.relationshipScore < 0) {
                        chance += 0.12f
                    } else {
                        chance -= 0.08f
                    }
                }
            }
            ResolutionType.WEAPONS_BAN -> chance += 0.04f
        }
        val alliedWithPlayer = state.governance.activeAlliances.any { alliance ->
            PLAYER_COUNTRY_ID in alliance.memberCountryIds &&
                countryId in alliance.memberCountryIds
        }
        if (alliedWithPlayer) {
            chance += 0.12f
        }
        return chance.coerceIn(0.08f, 0.92f)
    }

    private fun resolveResolution(state: GameState, resolution: UNResolution): GameState {
        // Force remaining undecided nations to abstain (no vote).
        val passed = resolution.votesForCount > resolution.votesAgainstCount
        val resultText = if (passed) {
            "PASSED: ${resolution.type.displayName} " +
                "(${resolution.votesForCount}–${resolution.votesAgainstCount})"
        } else {
            "FAILED: ${resolution.type.displayName} " +
                "(${resolution.votesForCount}–${resolution.votesAgainstCount})"
        }

        var next = state.copy(
            governance = state.governance.copy(
                activeResolution = null,
                lastResolutionResult = resultText,
            ),
        )

        if (!passed) {
            next = next.copy(
                vitals = next.vitals.copy(
                    approval = (next.vitals.approval - 2f).coerceIn(0f, 100f),
                ),
            )
            return next
        }

        return applyPassedResolution(next, resolution)
    }

    private fun applyPassedResolution(state: GameState, resolution: UNResolution): GameState {
        return when (resolution.type) {
            ResolutionType.GLOBAL_TAX -> state.copy(
                governance = state.governance.copy(globalTaxActive = true),
                vitals = state.vitals.copy(
                    budget = state.vitals.budget - GLOBAL_TAX_COST,
                    approval = (state.vitals.approval + 3f).coerceIn(0f, 100f),
                ),
            )
            ResolutionType.NUCLEAR_EMBARGO -> state.copy(
                governance = state.governance.copy(nuclearEmbargoActive = true),
                vitals = state.vitals.copy(
                    approval = (state.vitals.approval + 4f).coerceIn(0f, 100f),
                ),
            )
            ResolutionType.PEACEKEEPING_DEPLOYMENT -> {
                val war = state.diplomacy.activeWar
                val cooledWar = war?.copy(
                    warProgress = (war.warProgress * 0.7f).coerceIn(-100f, 100f),
                )
                state.copy(
                    governance = state.governance.copy(peacekeepingActive = true),
                    diplomacy = state.diplomacy.copy(activeWar = cooledWar),
                    vitals = state.vitals.copy(
                        approval = (state.vitals.approval + 5f).coerceIn(0f, 100f),
                    ),
                    military = state.military.copy(
                        defcon = (state.military.defcon + 1).coerceIn(1, 5),
                    ),
                )
            }
            ResolutionType.TRADE_SANCTIONS -> {
                val targetId = resolution.targetCountryId ?: return state
                state.copy(
                    diplomacy = state.diplomacy.updateRival(targetId) { rival ->
                        rival.copy(
                            relationshipScore = (rival.relationshipScore - 25).coerceIn(-100, 100),
                            militaryStrength = (rival.militaryStrength * 0.9).coerceAtLeast(80.0),
                            hasTradeTreaty = false,
                        )
                    },
                    vitals = state.vitals.copy(
                        approval = (state.vitals.approval + 2f).coerceIn(0f, 100f),
                    ),
                )
            }
            ResolutionType.WEAPONS_BAN -> state.copy(
                governance = state.governance.copy(weaponsBanActive = true),
                vitals = state.vitals.copy(
                    approval = (state.vitals.approval + 3f).coerceIn(0f, 100f),
                ),
                military = state.military.copy(
                    tanks = (state.military.tanks * 0.95).toInt().coerceAtLeast(0),
                    jets = (state.military.jets * 0.95).toInt().coerceAtLeast(0),
                ),
            )
        }
    }

    private fun applyPassiveGovernanceEffects(state: GameState): GameState {
        var budget = state.vitals.budget
        var approval = state.vitals.approval
        val governance = state.governance

        if (governance.globalTaxActive) {
            budget -= GLOBAL_TAX_UPKEEP
            approval += 0.2f
        }
        if (governance.peacekeepingActive) {
            budget -= PEACEKEEPING_UPKEEP
            approval += 0.3f
        }

        return state.copy(
            vitals = state.vitals.copy(
                budget = budget,
                approval = approval.coerceIn(0f, 100f),
            ),
        )
    }

    companion object {
        const val MAX_INFLUENCE = 100
        const val INFLUENCE_REGEN_PER_TICK = 3
        const val BRIBE_COST = 4_000_000_000L
        const val ALLIANCE_MIN_RELATION = 15
        const val ALLIANCE_INFLUENCE_COST = 10
        const val GLOBAL_TAX_COST = 8_000_000_000L
        const val GLOBAL_TAX_UPKEEP = 1_000_000_000L
        const val PEACEKEEPING_UPKEEP = 1_500_000_000L
        const val AI_VOTE_CHANCE_PER_TICK = 0.55f
        const val NUCLEAR_EMBARGO_STRENGTH_PENALTY = 0.88
        const val WEAPONS_BAN_STRENGTH_PENALTY = 0.94

        fun canPropose(state: GameState, type: ResolutionType): Boolean {
            if (state.gameOver.isGameOver) return false
            if (state.governance.activeResolution != null) return false
            return state.governance.diplomaticInfluence >= type.influenceCost
        }

        fun undecidedNations(state: GameState): List<String> {
            val resolution = state.governance.activeResolution ?: return emptyList()
            return state.diplomacy.rivals
                .map { it.id }
                .filterNot { resolution.hasVoted(it) }
        }

        fun allianceMilitaryPower(state: GameState, alliance: Alliance): Double {
            return alliance.memberCountryIds.sumOf { memberId ->
                if (memberId == PLAYER_COUNTRY_ID) {
                    state.effectiveCombatStrength
                } else {
                    state.diplomacy.rivalById(memberId)?.militaryStrength ?: 0.0
                }
            }
        }

        fun countryDisplayName(state: GameState, countryId: String): String {
            if (countryId == PLAYER_COUNTRY_ID) return "Your Nation"
            return state.diplomacy.rivalById(countryId)?.name ?: countryId
        }
    }
}
