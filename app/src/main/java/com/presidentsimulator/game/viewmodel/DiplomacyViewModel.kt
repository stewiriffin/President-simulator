package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.DeploymentStatus
import com.presidentsimulator.game.data.NationalPerkEffects
import com.presidentsimulator.game.data.DiplomacyState
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.data.TreatyType
import com.presidentsimulator.game.data.WarGoal
import com.presidentsimulator.game.data.WarOutcome
import com.presidentsimulator.game.data.WarState
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Pure geopolitics simulation engine.
 *
 * [GameViewModel] owns the [StateFlow] and applies these transforms via immutable
 * [GameState.copy] updates. Methods never mutate input state.
 */
class DiplomacyViewModel(
    private val random: Random = Random.Default,
) {

    /** Set when a war ends during [simulateWarBattle]; consumed by [GameViewModel]. */
    var lastResolvedWar: WarOutcome? = null
        private set

    fun consumeLastResolvedWar(): WarOutcome? {
        val outcome = lastResolvedWar
        lastResolvedWar = null
        return outcome
    }

    /**
     * Monthly passive diplomacy:
     * - High military spending / mobilization sours relations.
     * - Trade partners drift slightly positive.
     * - Hostile neighbors may randomly harden further.
     * - Diplomatic influence regenerates slowly.
     */
    fun simulateGeopolitics(state: GameState): GameState {
        val militaryPressure = militaryPressureScore(state)
        val updatedRivals = state.diplomacy.rivals.map { rival ->
            var score = rival.relationshipScore.toFloat()

            // Aggressive posture and heavy force projection alarm neighbors.
            score -= militaryPressure

            if (rival.hasTradeTreaty) {
                score += 0.8f
            }
            if (rival.hasNonAggressionPact) {
                score += 0.4f
            }

            // Random diplomatic weather (-3 … +3).
            score += random.nextInt(-3, 4)

            // Hostile blocs tend to polarize.
            if (rival.relationshipScore <= -40 && random.nextFloat() < 0.20f) {
                score -= random.nextInt(2, 6)
            }

            // At-war target is locked at floor hostility.
            if (state.diplomacy.activeWar?.targetCountryId == rival.id) {
                score = -100f
            }

            val hostile = score <= -25f
            val militaryGrowth = if (hostile) 1.8 else 0.6
            val economicGrowth = if (rival.hasTradeTreaty) 0.004 else 0.002

            rival.copy(
                relationshipScore = score.roundToInt().coerceIn(-100, 100),
                militaryStrength = (rival.militaryStrength + militaryGrowth).coerceAtMost(1_200.0),
                economicPower = (rival.economicPower + economicGrowth).coerceAtMost(2.0),
            )
        }

        val influenceRegen = if (state.diplomacy.activeWar == null) 2 else 1
        val diplomacy = state.diplomacy.copy(
            rivals = updatedRivals,
            diplomaticInfluence = (state.diplomacy.diplomaticInfluence + influenceRegen)
                .coerceIn(0, MAX_INFLUENCE),
        )

        return state.copy(diplomacy = diplomacy)
    }

    /**
     * Declares war on [targetCountryId]: relations → -100, mobilize forces,
     * raise DEFCON, and initialise a [WarState].
     */
    fun declareWar(state: GameState, targetCountryId: String, warGoal: WarGoal = WarGoal.REPARATIONS): GameState {
        val rival = state.diplomacy.rivalById(targetCountryId) ?: return state
        if (state.diplomacy.activeWar != null) return state
        if (rival.hasNonAggressionPact) return state

        val diplomacy = state.diplomacy
            .updateRival(targetCountryId) {
                it.copy(
                    relationshipScore = -100,
                    hasTradeTreaty = false,
                    hasNonAggressionPact = false,
                )
            }
            .copy(
                activeWar = WarState(
                    targetCountryId = targetCountryId,
                    warProgress = 0f,
                    playerCasualties = 0L,
                    enemyCasualties = 0L,
                    monthsActive = 0,
                    warGoal = warGoal,
                ),
            )

        return state.copy(
            vitals = state.vitals.copy(
                approval = (state.vitals.approval - 5f).coerceIn(0f, 100f),
            ),
            military = state.military.copy(
                deployment = DeploymentStatus.MOBILIZED,
                defcon = (state.military.defcon - 2).coerceIn(1, 5),
            ),
            diplomacy = diplomacy,
        )
    }

    /**
     * Resolves one month of combat when a war is active.
     * Win probability scales with player combat strength vs enemy strength.
     * Progress moves toward ±100; extremes end the war in victory or defeat.
     */
    fun simulateWarBattle(state: GameState): GameState {
        val war = state.diplomacy.activeWar ?: return state
        val enemy = state.diplomacy.rivalById(war.targetCountryId) ?: return endWar(
            state = state,
            victory = false,
        )

        val playerPower = state.effectiveCombatStrength.coerceAtLeast(1.0)
        val enemyPower = enemy.militaryStrength.coerceAtLeast(1.0)
        val winProbability = (playerPower / (playerPower + enemyPower)).toFloat()
        val playerWonSkirmish = random.nextFloat() < winProbability

        val swing = random.nextInt(4, 13).toFloat()
        val tacticMultiplier = if (war.currentTactic == com.presidentsimulator.game.data.WarTactic.OFFENSIVE) 1.5f else 0.5f
        
        val progressDelta = (if (playerWonSkirmish) swing else -swing) * tacticMultiplier
        val nextProgress = (war.warProgress + progressDelta).coerceIn(-100f, 100f)

        val playerLossRate = if (playerWonSkirmish) 0.004 else 0.012
        val enemyLossRate = if (playerWonSkirmish) 0.014 else 0.005
        
        val casualtyMultiplier = if (war.currentTactic == com.presidentsimulator.game.data.WarTactic.OFFENSIVE) 1.5 else 0.5

        val playerCasualties = (state.military.personnel * playerLossRate * casualtyMultiplier)
            .toLong()
            .coerceAtLeast(500L)
        val enemyCasualties = (enemy.militaryStrength * 800 * enemyLossRate * casualtyMultiplier)
            .toLong()
            .coerceAtLeast(400L)

        val hardwareLossTanks = if (playerWonSkirmish) {
            random.nextInt(0, 4)
        } else {
            random.nextInt(2, 8)
        }
        val hardwareLossJets = if (playerWonSkirmish) {
            random.nextInt(0, 2)
        } else {
            random.nextInt(1, 4)
        }

        val updatedWar = war.copy(
            warProgress = nextProgress,
            playerCasualties = war.playerCasualties + playerCasualties,
            enemyCasualties = war.enemyCasualties + enemyCasualties,
            monthsActive = war.monthsActive + 1,
            lastBattleSummary = buildString {
                append(if (playerWonSkirmish) "Front advanced " else "Front fell back ")
                append("${abs(progressDelta.roundToInt())} pts · ")
                append("our losses ${playerCasualties.toCasualtyString()} · ")
                append("enemy ${enemyCasualties.toCasualtyString()}")
                if (hardwareLossTanks > 0 || hardwareLossJets > 0) {
                    append(" · hardware -$hardwareLossTanks tanks / -$hardwareLossJets jets")
                }
            },
        )

        val midState = state.copy(
            vitals = state.vitals.copy(
                population = (state.vitals.population - playerCasualties / 2)
                    .coerceAtLeast(1_000_000L),
                approval = (state.vitals.approval + if (playerWonSkirmish) 0.5f else -1.2f)
                    .coerceIn(0f, 100f),
                budget = state.vitals.budget - WAR_MONTHLY_COST,
            ),
            military = state.military.copy(
                personnel = (state.military.personnel - playerCasualties).coerceAtLeast(50_000L),
                tanks = (state.military.tanks - hardwareLossTanks).coerceAtLeast(0),
                jets = (state.military.jets - hardwareLossJets).coerceAtLeast(0),
                deployment = DeploymentStatus.MOBILIZED,
                defcon = state.military.defcon.coerceIn(1, 3),
            ),
            diplomacy = state.diplomacy.copy(activeWar = updatedWar),
        )

        return when {
            nextProgress >= 100f -> endWar(midState, victory = true)
            nextProgress <= -100f -> endWar(midState, victory = false)
            else -> midState
        }
    }

    /**
     * Spends budget and diplomatic influence to secure a treaty with [targetCountryId].
     * Peace treaties require an active war against that target (armistice path).
     */
    fun negotiateTreaty(state: GameState, targetCountryId: String, type: TreatyType): GameState {
        val rival = state.diplomacy.rivalById(targetCountryId) ?: return state
        val war = state.diplomacy.activeWar

        if (type == TreatyType.PEACE) {
            if (war == null || war.targetCountryId != targetCountryId) return state
            return signArmistice(state)
        }

        if (war?.targetCountryId == targetCountryId) return state

        when (type) {
            TreatyType.TRADE -> if (rival.hasTradeTreaty) return state
            TreatyType.NON_AGGRESSION -> if (rival.hasNonAggressionPact) return state
            TreatyType.PEACE -> Unit
        }

        // Hostile nations refuse deals without enough influence buffer.
        if (rival.relationshipScore < -30 &&
            state.diplomacy.diplomaticInfluence < type.influenceCost + 10
        ) {
            return state
        }

        if (state.vitals.budget < type.budgetCost) return state
        if (state.diplomacy.diplomaticInfluence < type.influenceCost) return state

        val updatedRival = when (type) {
            TreatyType.TRADE -> rival.copy(
                hasTradeTreaty = true,
                relationshipScore = (rival.relationshipScore + type.relationshipBonus)
                    .coerceIn(-100, 100),
            )
            TreatyType.NON_AGGRESSION -> rival.copy(
                hasNonAggressionPact = true,
                relationshipScore = (rival.relationshipScore + type.relationshipBonus)
                    .coerceIn(-100, 100),
            )
            TreatyType.PEACE -> rival
        }

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - type.budgetCost,
                approval = (state.vitals.approval + 1.5f).coerceIn(0f, 100f),
            ),
            diplomacy = state.diplomacy
                .updateRival(targetCountryId) { updatedRival }
                .copy(
                    diplomaticInfluence = state.diplomacy.diplomaticInfluence - type.influenceCost,
                ),
        )
    }

    /**
     * Ends an active war early when progress is favorable (≥60).
     * Rewards scale with front-line advantage but are below a total victory.
     */
    fun claimWarSettlement(state: GameState): GameState {
        val war = state.diplomacy.activeWar ?: return state
        if (war.warProgress < EARLY_SETTLEMENT_MIN_PROGRESS) return state
        val scale = (war.warProgress / 100f).coerceIn(0.55f, 0.90f)
        return endWar(state, victory = true, rewardScale = scale, settlementNote = "Negotiated settlement at ${war.warProgress.roundToInt()}% front advantage")
    }

    /**
     * Ends the active war via armistice / peace treaty.
     * Cost scales with how poorly the war is going.
     */
    fun signArmistice(state: GameState): GameState {
        val war = state.diplomacy.activeWar ?: return state
        val progressPenalty = ((-war.warProgress).coerceAtLeast(0f) / 100f)
        val reparations = (ARMISTICE_BASE_COST * (1f + progressPenalty)).toLong()
        if (state.vitals.budget < reparations) return state

        val diplomacy = state.diplomacy
            .updateRival(war.targetCountryId) { rival ->
                rival.copy(
                    relationshipScore = (rival.relationshipScore + 35).coerceIn(-100, 100),
                    hasNonAggressionPact = true,
                    hasTradeTreaty = false,
                )
            }
            .copy(activeWar = null)

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - reparations,
                approval = (state.vitals.approval + 4f).coerceIn(0f, 100f),
            ),
            military = state.military.copy(
                deployment = DeploymentStatus.DEFENSIVE,
                defcon = (state.military.defcon + 2).coerceIn(1, 5),
            ),
            diplomacy = diplomacy,
        )
    }

    /**
     * Cancels an existing trade or non-aggression treaty with diplomatic fallout.
     */
    fun breakTreaty(state: GameState, targetCountryId: String, type: TreatyType): GameState {
        if (type == TreatyType.PEACE) return state
        val rival = state.diplomacy.rivalById(targetCountryId) ?: return state
        val updatedRival = when (type) {
            TreatyType.TRADE -> {
                if (!rival.hasTradeTreaty) return state
                rival.copy(
                    hasTradeTreaty = false,
                    relationshipScore = (rival.relationshipScore - 12).coerceIn(-100, 100),
                )
            }
            TreatyType.NON_AGGRESSION -> {
                if (!rival.hasNonAggressionPact) return state
                rival.copy(
                    hasNonAggressionPact = false,
                    relationshipScore = (rival.relationshipScore - 18).coerceIn(-100, 100),
                )
            }
            TreatyType.PEACE -> rival
        }
        return state.copy(
            vitals = state.vitals.copy(
                approval = (state.vitals.approval - 1.5f).coerceIn(0f, 100f),
            ),
            diplomacy = state.diplomacy
                .updateRival(targetCountryId) { updatedRival }
                .copy(
                    diplomaticInfluence = (state.diplomacy.diplomaticInfluence - 5).coerceAtLeast(0),
                ),
        )
    }

    fun setDefcon(state: GameState, level: Int): GameState {
        val next = level.coerceIn(1, 5)
        if (next == state.military.defcon) return state
        val delta = next - state.military.defcon
        val approvalHit = if (delta < 0) delta * 1.5f else delta * 0.5f
        return state.copy(
            military = state.military.copy(defcon = next),
            vitals = state.vitals.copy(
                approval = (state.vitals.approval + approvalHit).coerceIn(0f, 100f),
            ),
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (
                    state.internalSecurity.instabilityScore + if (delta < 0) 1.5f * -delta else -0.5f * delta
                    ).coerceIn(0f, 100f),
            ),
        )
    }

    /** Soft-power grant: spend budget to improve relations. */
    fun sendForeignAid(state: GameState, targetCountryId: String): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.diplomacy.rivalById(targetCountryId) == null) return state
        if (state.diplomacy.activeWar?.targetCountryId == targetCountryId) return state
        if (state.vitals.budget < FOREIGN_AID_COST) return state
        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - FOREIGN_AID_COST,
                approval = (state.vitals.approval + 0.5f).coerceIn(0f, 100f),
            ),
            diplomacy = state.diplomacy
                .updateRival(targetCountryId) {
                    it.copy(relationshipScore = (it.relationshipScore + FOREIGN_AID_REL_BONUS).coerceIn(-100, 100))
                }
                .copy(
                    diplomaticInfluence = (state.diplomacy.diplomaticInfluence + 2).coerceAtMost(100),
                ),
        )
    }

    /** High-profile visit: spend influence for a larger relationship swing. */
    fun conductStateVisit(state: GameState, targetCountryId: String): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.diplomacy.rivalById(targetCountryId) == null) return state
        if (state.diplomacy.activeWar?.targetCountryId == targetCountryId) return state
        if (state.diplomacy.diplomaticInfluence < STATE_VISIT_INFLUENCE_COST) return state
        if (state.vitals.budget < STATE_VISIT_BUDGET_COST) return state
        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - STATE_VISIT_BUDGET_COST,
                approval = (state.vitals.approval + 1f).coerceIn(0f, 100f),
            ),
            diplomacy = state.diplomacy
                .updateRival(targetCountryId) {
                    it.copy(relationshipScore = (it.relationshipScore + STATE_VISIT_REL_BONUS).coerceIn(-100, 100))
                }
                .copy(
                    diplomaticInfluence = (state.diplomacy.diplomaticInfluence - STATE_VISIT_INFLUENCE_COST)
                        .coerceAtLeast(0),
                ),
        )
    }

    fun setDeployment(state: GameState, status: DeploymentStatus): GameState {
        return state.copy(military = state.military.copy(deployment = status))
    }

    /**
     * Salary funding multiplier (0.5–1.5). Raises morale and personnel upkeep.
     */
    fun setSalaryFunding(state: GameState, funding: Float): GameState {
        val clamped = funding.coerceIn(MIN_SALARY_FUNDING, MAX_SALARY_FUNDING)
        return state.copy(
            military = state.military.copy(salaryFunding = clamped),
        )
    }

    fun recruitPersonnel(state: GameState, amount: Long): GameState {
        if (amount <= 0L) return state
        val perk = NationalPerkEffects.forNationId(state.playerNation.id)
        val cost = (amount * RECRUIT_COST_PER_SOLDIER * NationalPerkEffects.recruitCostMultiplier(perk)).toLong()
        if (state.vitals.budget < cost) return state
        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - cost),
            military = state.military.copy(
                personnel = state.military.personnel + amount,
            ),
        )
    }

    fun purchaseTanks(state: GameState, amount: Int): GameState =
        purchaseHardware(state, amount, MilitaryHardware.TANKS)

    fun purchaseJets(state: GameState, amount: Int): GameState =
        purchaseHardware(state, amount, MilitaryHardware.FIGHTER_JETS)

    fun purchaseShips(state: GameState, amount: Int): GameState =
        purchaseHardware(state, amount, MilitaryHardware.NAVAL_SHIPS)

    fun purchaseNukes(state: GameState, amount: Int): GameState =
        purchaseHardware(state, amount, MilitaryHardware.NUCLEAR_ARSENAL)

    fun purchaseHardware(state: GameState, amount: Int, hardware: MilitaryHardware): GameState {
        if (amount <= 0) return state
        if (hardware == MilitaryHardware.NUCLEAR_ARSENAL &&
            state.governance.nuclearEmbargoActive
        ) {
            return state
        }
        if (hardware != MilitaryHardware.NUCLEAR_ARSENAL &&
            state.governance.weaponsBanActive
        ) {
            return state
        }
        val cost = hardware.unitCost * amount
        if (state.vitals.budget < cost) return state

        val military = state.military
        val updated = when (hardware) {
            MilitaryHardware.TANKS -> military.copy(tanks = military.tanks + amount)
            MilitaryHardware.FIGHTER_JETS -> military.copy(jets = military.jets + amount)
            MilitaryHardware.NAVAL_SHIPS -> military.copy(ships = military.ships + amount)
            MilitaryHardware.NUCLEAR_ARSENAL ->
                military.copy(nuclearArsenal = military.nuclearArsenal + amount)
        }
        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - cost),
            military = updated,
        )
    }

    /** Press attack: increases casualties but increases chance of taking ground. */
    fun launchOffensive(state: GameState): GameState {
        val war = state.diplomacy.activeWar ?: return state
        return state.copy(
            military = state.military.copy(deployment = DeploymentStatus.MOBILIZED),
            diplomacy = state.diplomacy.copy(
                activeWar = war.copy(
                    currentTactic = com.presidentsimulator.game.data.WarTactic.OFFENSIVE,
                ),
            ),
        )
    }

    /** Dig in: defensive posture reduces casualties and limits ground lost. */
    fun holdDefensiveLine(state: GameState): GameState {
        val war = state.diplomacy.activeWar ?: return state
        return state.copy(
            military = state.military.copy(deployment = DeploymentStatus.DEFENSIVE),
            diplomacy = state.diplomacy.copy(
                activeWar = war.copy(
                    currentTactic = com.presidentsimulator.game.data.WarTactic.DEFENSIVE,
                ),
            ),
        )
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private fun militaryPressureScore(state: GameState): Float {
        val spendingRatio = state.military.monthlyUpkeep.toFloat() /
            (state.vitals.budget + state.netIncome).coerceAtLeast(1L).toFloat()
        var pressure = 0f
        if (spendingRatio > 0.08f) pressure += 1.2f
        if (spendingRatio > 0.15f) pressure += 1.5f
        if (state.military.deployment == DeploymentStatus.MOBILIZED) pressure += 2.0f
        if (state.military.defcon <= 2) pressure += 1.5f
        return pressure
    }

    private fun endWar(
        state: GameState,
        victory: Boolean,
        rewardScale: Float = 1f,
        settlementNote: String = "",
    ): GameState {
        val war = state.diplomacy.activeWar ?: return state
        val targetId = war.targetCountryId
        val targetName = state.diplomacy.rivalById(targetId)?.name ?: targetId
        val goal = war.warGoal
        val scale = rewardScale.coerceIn(0.5f, 1f)

        val diplomacy: DiplomacyState = if (victory) {
            state.diplomacy
                .updateRival(targetId) { rival ->
                    val strengthFactor = when (goal) {
                        WarGoal.DEMILITARIZE -> 0.45
                        WarGoal.BUFFER_ZONE -> 0.58
                        WarGoal.REPARATIONS -> 0.65
                    }
                    rival.copy(
                        relationshipScore = when (goal) {
                            WarGoal.BUFFER_ZONE -> -35
                            else -> -60
                        },
                        militaryStrength = (rival.militaryStrength * strengthFactor).coerceAtLeast(80.0),
                        hasTradeTreaty = false,
                        hasNonAggressionPact = goal == WarGoal.BUFFER_ZONE,
                    )
                }
                .copy(activeWar = null)
        } else {
            state.diplomacy
                .updateRival(targetId) { rival ->
                    rival.copy(
                        relationshipScore = -80,
                        militaryStrength = rival.militaryStrength * 1.05,
                    )
                }
                .copy(activeWar = null)
        }

        val (budgetDelta, approvalDelta) = if (victory) {
            when (goal) {
                WarGoal.REPARATIONS -> Pair(
                    (VICTORY_REPARATIONS * 1.25f * scale).toLong(),
                    12f * scale,
                )
                WarGoal.DEMILITARIZE -> Pair(
                    (VICTORY_REPARATIONS * 0.85f * scale).toLong(),
                    10f * scale,
                )
                WarGoal.BUFFER_ZONE -> Pair(
                    (VICTORY_REPARATIONS * 0.70f * scale).toLong(),
                    (12f + 8f) * scale,
                )
            }
        } else {
            Pair(-DEFEAT_PENALTY, -18f)
        }

        val note = settlementNote.ifBlank {
            if (victory) "War goal achieved: ${goal.displayName}" else "War lost against $targetName"
        }

        lastResolvedWar = WarOutcome(
            victory = victory,
            targetCountryId = targetId,
            targetName = targetName,
            monthsActive = war.monthsActive,
            playerCasualties = war.playerCasualties,
            enemyCasualties = war.enemyCasualties,
            budgetDelta = budgetDelta,
            approvalDelta = approvalDelta,
            finalProgress = war.warProgress,
            warGoalLabel = goal.displayName,
            settlementNote = note,
        )

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget + budgetDelta,
                approval = (state.vitals.approval + approvalDelta).coerceIn(0f, 100f),
            ),
            military = state.military.copy(
                deployment = DeploymentStatus.DEFENSIVE,
                defcon = if (victory) 4 else 3,
            ),
            diplomacy = diplomacy,
        )
    }

    companion object {
        const val MAX_INFLUENCE = 100
        const val WAR_MONTHLY_COST = 4_000_000_000L
        const val ARMISTICE_BASE_COST = 5_000_000_000L
        const val FOREIGN_AID_COST = 3_000_000_000L
        const val FOREIGN_AID_REL_BONUS = 8
        const val STATE_VISIT_BUDGET_COST = 1_500_000_000L
        const val STATE_VISIT_INFLUENCE_COST = 12
        const val STATE_VISIT_REL_BONUS = 14
        const val VICTORY_REPARATIONS = 20_000_000_000L
        const val DEFEAT_PENALTY = 15_000_000_000L
        const val EARLY_SETTLEMENT_MIN_PROGRESS = 60f
        const val RECRUIT_COST_PER_SOLDIER = 50_000L
        const val RECRUIT_BATCH_SIZE = 10_000L
        const val TANK_UNIT_COST = 15_000_000L
        const val JET_UNIT_COST = 80_000_000L
        const val MIN_SALARY_FUNDING = 0.5f
        const val MAX_SALARY_FUNDING = 1.5f

        fun maxRecruitable(state: GameState): Int {
            if (RECRUIT_COST_PER_SOLDIER <= 0L) return 0
            return (state.vitals.budget / RECRUIT_COST_PER_SOLDIER).toInt().coerceAtLeast(0)
        }

        fun maxAffordableHardware(state: GameState, hardware: MilitaryHardware): Int {
            if (hardware.unitCost <= 0L) return 0
            if (hardware == MilitaryHardware.NUCLEAR_ARSENAL &&
                state.governance.nuclearEmbargoActive
            ) {
                return 0
            }
            if (hardware != MilitaryHardware.NUCLEAR_ARSENAL &&
                state.governance.weaponsBanActive
            ) {
                return 0
            }
            return (state.vitals.budget / hardware.unitCost).toInt().coerceAtLeast(0)
        }

        fun treatyAffordable(state: GameState, type: TreatyType): Boolean =
            state.vitals.budget >= type.budgetCost &&
                state.diplomacy.diplomaticInfluence >= type.influenceCost

        fun armisticeCost(state: GameState): Long {
            val war = state.diplomacy.activeWar ?: return ARMISTICE_BASE_COST
            val progressPenalty = ((-war.warProgress).coerceAtLeast(0f) / 100f)
            return (ARMISTICE_BASE_COST * (1f + progressPenalty)).toLong()
        }

        fun canDeclareWar(state: GameState, targetCountryId: String): Boolean {
            if (state.diplomacy.activeWar != null) return false
            val rival = state.diplomacy.rivalById(targetCountryId) ?: return false
            return !rival.hasNonAggressionPact
        }
    }
}

/** Maps war progress (-100…100) to a 0…1 fraction for progress bars. */
fun WarState.progressFraction(): Float = ((warProgress + 100f) / 200f).coerceIn(0f, 1f)

fun WarState.progressLabel(): String {
    val value = warProgress.roundToInt()
    val sign = if (value > 0) "+" else ""
    return "$sign$value%"
}

fun Long.toCasualtyString(): String {
    val abs = abs(this)
    return when {
        abs >= 1_000_000L -> "%.2fM".format(abs / 1_000_000.0)
        abs >= 1_000L -> "%.1fK".format(abs / 1_000.0)
        else -> abs.toString()
    }
}
