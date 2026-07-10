package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.CovertMission
import com.presidentsimulator.game.data.EspionageState
import com.presidentsimulator.game.data.GameOverState
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MissionStatus
import com.presidentsimulator.game.data.MissionType
import com.presidentsimulator.game.data.SecurityProtocol
import com.presidentsimulator.game.data.TechCatalog
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Pure internal-security and espionage simulation engine.
 * [GameViewModel] applies results through immutable [GameState.copy] updates.
 */
class EspionageSecurityViewModel(
    private val random: Random = Random.Default,
) {

    /**
     * Monthly security pipeline:
     * 1. Recalculate instability from approval, shortages, and tax pressure
     * 2. Accelerate coup risk when instability is critical
     * 3. Advance covert missions and resolve completed operations
     */
    fun processSecurityTick(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state

        val cooledProtocols = state.internalSecurity.protocolCooldownMonths
            .mapValues { (_, months) -> (months - 1).coerceAtLeast(0) }
            .filterValues { it > 0 }
        var next = state.copy(
            internalSecurity = state.internalSecurity.copy(
                securityFundsThisMonth = 0,
                protocolCooldownMonths = cooledProtocols,
            ),
        )
        next = updateDomesticStability(next)
        next = processExposureEffects(next)
        if (next.internalSecurity.coupRisk >= 100f) {
            return triggerCoup(next)
        }
        next = advanceCovertMissions(next)
        next = regenerateIntelligence(next)
        return next
    }

    /**
     * Deploys a spy mission against [targetCountryId].
     * Costs budget, intelligence points, and one available agent.
     */
    fun deploySpy(
        state: GameState,
        targetCountryId: String,
        missionType: MissionType,
    ): GameState {
        if (state.gameOver.isGameOver) return state
        val rival = state.diplomacy.rivalById(targetCountryId) ?: return state
        if (state.espionage.availableSpies <= 0) return state
        if (state.vitals.budget < missionType.budgetCost) return state
        if (state.espionage.intelligencePoints < missionType.intelCost) return state
        if (state.espionage.activeMissions.any {
                it.targetCountryId == targetCountryId &&
                    it.missionType == missionType &&
                    it.status == MissionStatus.ACTIVE
            }
        ) {
            return state
        }

        val successProbability = estimateSuccessProbability(state, rival.militaryStrength, missionType)
        val mission = CovertMission(
            id = UUID.randomUUID().toString(),
            missionType = missionType,
            targetCountryId = targetCountryId,
            successProbability = successProbability,
            progressTicks = 0,
            requiredTicks = missionType.durationTicks,
            status = MissionStatus.ACTIVE,
        )

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - missionType.budgetCost,
            ),
            espionage = state.espionage.copy(
                intelligencePoints = state.espionage.intelligencePoints - missionType.intelCost,
                activeMissions = state.espionage.activeMissions + mission,
            ),
        )
    }

    /**
     * Spends treasury funds on police and counter-intelligence,
     * immediately reducing domestic instability.
     */
    fun fundInternalSecurity(state: GameState, amount: Int): GameState {
        if (state.gameOver.isGameOver) return state
        if (amount <= 0) return state
        val already = state.internalSecurity.securityFundsThisMonth
        val allowed = (MONTHLY_SECURITY_FUND_CAP - already).coerceAtLeast(0)
        val units = amount.coerceAtMost(allowed)
        if (units <= 0) return state
        val cost = units * SECURITY_FUND_UNIT_COST
        if (state.vitals.budget < cost) return state

        val diminishing = 1f / (1f + already * 0.35f)
        val instabilityDrop = units * SECURITY_FUND_INSTABILITY_PER_UNIT * diminishing
        val coupDrop = units * SECURITY_FUND_COUP_PER_UNIT * diminishing

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - cost,
                approval = (state.vitals.approval + units * 0.15f * diminishing).coerceIn(0f, 100f),
            ),
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (state.internalSecurity.instabilityScore - instabilityDrop)
                    .coerceIn(0f, 100f),
                coupRisk = (state.internalSecurity.coupRisk - coupDrop).coerceIn(0f, 100f),
                securityFundsThisMonth = already + units,
            ),
        )
    }

    fun toggleSecurityProtocol(state: GameState, protocol: SecurityProtocol): GameState {
        if (state.gameOver.isGameOver) return state
        val cooldown = state.internalSecurity.protocolCooldownMonths[protocol.name] ?: 0
        if (cooldown > 0) return state

        val active = state.internalSecurity.activeProtocols.toMutableList()
        val enabling = protocol !in active
        if (enabling) {
            if (state.vitals.budget < protocol.monthlyUpkeep) return state
            active.add(protocol)
            val activationShock = when (protocol) {
                SecurityProtocol.POLICE_STATE -> 4f
                SecurityProtocol.CURFEW -> 2.5f
                else -> 1f
            }
            return state.copy(
                vitals = state.vitals.copy(
                    budget = state.vitals.budget - protocol.monthlyUpkeep,
                    approval = (state.vitals.approval + protocol.approvalPenalty * 0.5f)
                        .coerceIn(0f, 100f),
                ),
                internalSecurity = state.internalSecurity.copy(
                    activeProtocols = active,
                    instabilityScore = (state.internalSecurity.instabilityScore + activationShock)
                        .coerceIn(0f, 100f),
                    protocolCooldownMonths = state.internalSecurity.protocolCooldownMonths +
                        (protocol.name to PROTOCOL_TOGGLE_COOLDOWN_MONTHS),
                ),
            )
        }

        active.remove(protocol)
        return state.copy(
            internalSecurity = state.internalSecurity.copy(
                activeProtocols = active,
                instabilityScore = (state.internalSecurity.instabilityScore + 2f).coerceIn(0f, 100f),
                protocolCooldownMonths = state.internalSecurity.protocolCooldownMonths +
                    (protocol.name to PROTOCOL_TOGGLE_COOLDOWN_MONTHS),
            ),
        )
    }

    fun recruitSpy(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.vitals.budget < SPY_RECRUIT_COST) return state
        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - SPY_RECRUIT_COST),
            espionage = state.espionage.copy(spyCount = state.espionage.spyCount + 1),
        )
    }

    /**
     * Aborts an active covert mission and frees the assigned agent.
     * Spent budget and intelligence points are not refunded.
     */
    fun cancelCovertMission(state: GameState, missionId: String): GameState {
        if (state.gameOver.isGameOver) return state
        val mission = state.espionage.activeMissions.find {
            it.id == missionId && it.status == MissionStatus.ACTIVE
        } ?: return state

        return state.copy(
            espionage = state.espionage.copy(
                activeMissions = state.espionage.activeMissions.filterNot { it.id == mission.id },
            ),
        )
    }

    fun estimateSuccessProbability(
        state: GameState,
        rivalMilitaryStrength: Double,
        missionType: MissionType,
    ): Float {
        val playerPower = state.effectiveCombatStrength.coerceAtLeast(1.0)
        val rivalSecurity = rivalMilitaryStrength.coerceAtLeast(1.0)
        val powerRatio = (playerPower / (playerPower + rivalSecurity)).toFloat()
        val intelBonus = (state.espionage.intelligencePoints / 200f).coerceIn(0f, 0.15f)
        val counterIntelActive = SecurityProtocol.COUNTER_INTEL in state.internalSecurity.activeProtocols
        val counterIntelBonus = if (counterIntelActive) 0.03f else 0f
        val exposureDrag = (state.espionage.exposureLevel / 200f).coerceIn(0f, 0.12f)
        return (missionType.baseSuccessChance * 0.55f + powerRatio * 0.40f + intelBonus + counterIntelBonus - exposureDrag)
            .coerceIn(0.05f, 0.92f)
    }

    private fun processExposureEffects(state: GameState): GameState {
        var exposure = state.espionage.exposureLevel
        if (SecurityProtocol.COUNTER_INTEL in state.internalSecurity.activeProtocols) {
            exposure = (exposure - 4f).coerceAtLeast(0f)
        } else {
            exposure = (exposure - 1f).coerceAtLeast(0f)
        }

        var next = state.copy(
            espionage = state.espionage.copy(exposureLevel = exposure),
        )

        if (exposure >= EXPOSURE_RETALIATION_THRESHOLD) {
            val penalty = ((exposure - EXPOSURE_RETALIATION_THRESHOLD) / 10f).coerceAtMost(4f)
            val rivals = next.diplomacy.rivals.map { rival ->
                rival.copy(
                    relationshipScore = (rival.relationshipScore - penalty.roundToInt())
                        .coerceIn(-100, 100),
                )
            }
            next = next.copy(
                diplomacy = next.diplomacy.copy(rivals = rivals),
                internalSecurity = next.internalSecurity.copy(
                    instabilityScore = (next.internalSecurity.instabilityScore + penalty * 0.6f)
                        .coerceIn(0f, 100f),
                ),
            )
        }
        return next
    }

    private fun exposureGain(missionType: MissionType, success: Boolean): Float = when {
        success -> when (missionType) {
            MissionType.ASSASSINATE_LEADER -> 8f
            MissionType.FUND_REBELS -> 5f
            MissionType.SABOTAGE_ECONOMY -> 4f
            MissionType.STEAL_TECHNOLOGY -> 3f
        }
        else -> when (missionType) {
            MissionType.ASSASSINATE_LEADER -> 18f
            MissionType.FUND_REBELS -> 12f
            MissionType.SABOTAGE_ECONOMY -> 9f
            MissionType.STEAL_TECHNOLOGY -> 6f
        }
    }

    private fun withExposure(state: GameState, gain: Float): GameState =
        state.copy(
            espionage = state.espionage.copy(
                exposureLevel = (state.espionage.exposureLevel + gain).coerceIn(0f, 100f),
            ),
        )

    // ── Internals ────────────────────────────────────────────────────────────

    private fun updateDomesticStability(state: GameState): GameState {
        val approval = state.vitals.approval
        val taxRate = state.economy.taxRate
        val security = state.internalSecurity

        var instability = security.instabilityScore

        // Low approval fuels unrest.
        instability += when {
            approval < 25f -> 6f
            approval < 40f -> 3.5f
            approval < 55f -> 1.2f
            approval > 70f -> -1.5f
            else -> 0f
        }

        // Resource shortages and high taxes amplify instability.
        if (state.production.foodShortage) instability += 5f
        if (state.production.energyShortage) instability += 3f
        if (taxRate >= 0.40f) instability += 4f
        else if (taxRate >= 0.30f) instability += 2f

        // Active protocols suppress unrest but cost approval.
        instability -= security.instabilitySuppression

        // Martial law framework (legal) slightly stabilizes.
        if (state.legal.isActive("martial_law")) {
            instability -= 2f
        }

        instability = instability.coerceIn(0f, 100f)

        var coupRisk = security.coupRisk
        if (instability >= INSTABILITY_COUP_THRESHOLD) {
            val overage = instability - INSTABILITY_COUP_THRESHOLD
            coupRisk += 4f + overage * 0.35f
        } else if (instability < 50f) {
            coupRisk -= 1.5f
        } else {
            coupRisk -= 0.4f
        }

        // Security funding protocols also bleed coup momentum.
        coupRisk -= security.instabilitySuppression * 0.15f
        coupRisk = coupRisk.coerceIn(0f, 100f)

        val approvalDelta = security.approvalPenalty
        return state.copy(
            vitals = state.vitals.copy(
                approval = (state.vitals.approval + approvalDelta).coerceIn(0f, 100f),
            ),
            internalSecurity = security.copy(
                instabilityScore = instability,
                coupRisk = coupRisk,
            ),
        )
    }

    private fun advanceCovertMissions(state: GameState): GameState {
        if (state.espionage.activeMissions.isEmpty()) return state

        var next = state
        val resolvedMissions = mutableListOf<CovertMission>()

        state.espionage.activeMissions.forEach { mission ->
            if (mission.status != MissionStatus.ACTIVE) {
                resolvedMissions += mission
                return@forEach
            }

            val advanced = mission.copy(progressTicks = mission.progressTicks + 1)
            if (advanced.progressTicks < advanced.requiredTicks) {
                resolvedMissions += advanced
            } else {
                val outcome = resolveMission(next, advanced)
                next = outcome.first
                resolvedMissions += outcome.second
            }
        }

        // Keep recent outcomes for the UI, but cap history.
        val trimmed = resolvedMissions
            .sortedByDescending { it.status == MissionStatus.ACTIVE }
            .take(MAX_MISSION_LOG)

        return next.copy(
            espionage = next.espionage.copy(activeMissions = trimmed),
        )
    }

    private fun resolveMission(
        state: GameState,
        mission: CovertMission,
    ): Pair<GameState, CovertMission> {
        val success = random.nextFloat() < mission.successProbability
        val gain = exposureGain(mission.missionType, success)
        return if (success) {
            val (next, summary) = applyMissionSuccess(state, mission)
            withExposure(next, gain) to mission.copy(
                progressTicks = mission.requiredTicks,
                status = MissionStatus.SUCCESS,
                outcomeSummary = summary,
            )
        } else {
            val (next, summary) = applyMissionFailure(state, mission)
            withExposure(next, gain) to mission.copy(
                progressTicks = mission.requiredTicks,
                status = MissionStatus.FAILED,
                outcomeSummary = summary,
            )
        }
    }

    private fun applyMissionSuccess(
        state: GameState,
        mission: CovertMission,
    ): Pair<GameState, String> {
        val rival = state.diplomacy.rivalById(mission.targetCountryId)
            ?: return state to "Target nation not found."
        return when (mission.missionType) {
            MissionType.STEAL_TECHNOLOGY -> {
                val stealable = TechCatalog.all.filter { tech ->
                    !state.research.isUnlocked(tech.id) &&
                        state.research.prerequisitesMet(tech) &&
                        !(tech.id == "nuclear_fission" && state.governance.nuclearEmbargoActive)
                }
                val stolen = stealable.randomOrNull(random)
                if (stolen != null) {
                    state.copy(
                        research = state.research.copy(
                            unlockedTechIds = state.research.unlockedTechIds + stolen.id,
                            sciencePoints = state.research.sciencePoints + 25L,
                        ),
                        espionage = state.espionage.copy(
                            intelligencePoints = state.espionage.intelligencePoints + 12,
                        ),
                    ) to "Stolen technology: ${stolen.name} (+25 science, +12 intel)."
                } else {
                    state.copy(
                        research = state.research.copy(
                            sciencePoints = state.research.sciencePoints + 80L,
                        ),
                        espionage = state.espionage.copy(
                            intelligencePoints = state.espionage.intelligencePoints + 16,
                        ),
                    ) to "No unlockable tech available — recovered +80 science and +16 intel."
                }
            }
            MissionType.SABOTAGE_ECONOMY -> state.copy(
                diplomacy = state.diplomacy.updateRival(rival.id) {
                    it.copy(
                        militaryStrength = (it.militaryStrength * 0.92).coerceAtLeast(80.0),
                        relationshipScore = (it.relationshipScore - 12).coerceIn(-100, 100),
                    )
                },
                vitals = state.vitals.copy(
                    budget = state.vitals.budget + 2_000_000_000L,
                ),
            ) to "Diverted industrial assets (+$2.0B) · rival strength −8% · relations −12."
            MissionType.FUND_REBELS -> state.copy(
                diplomacy = state.diplomacy.updateRival(rival.id) {
                    it.copy(
                        militaryStrength = (it.militaryStrength * 0.88).coerceAtLeast(80.0),
                        relationshipScore = (it.relationshipScore - 20).coerceIn(-100, 100),
                    )
                },
                espionage = state.espionage.copy(
                    intelligencePoints = state.espionage.intelligencePoints + 6,
                ),
            ) to "Insurgency funded · rival strength −12% · relations −20 · +6 intel."
            MissionType.ASSASSINATE_LEADER -> state.copy(
                diplomacy = state.diplomacy.updateRival(rival.id) {
                    it.copy(
                        militaryStrength = (it.militaryStrength * 0.80).coerceAtLeast(80.0),
                        relationshipScore = (it.relationshipScore - 35).coerceIn(-100, 100),
                    )
                },
                vitals = state.vitals.copy(
                    approval = (state.vitals.approval + 3f).coerceIn(0f, 100f),
                ),
                espionage = state.espionage.copy(
                    intelligencePoints = state.espionage.intelligencePoints + 10,
                ),
            ) to "Leadership shock · rival strength −20% · relations −35 · approval +3 · +10 intel."
        }
    }

    private fun applyMissionFailure(
        state: GameState,
        mission: CovertMission,
    ): Pair<GameState, String> {
        val rival = state.diplomacy.rivalById(mission.targetCountryId) ?: return state to "Mission failed."
        val loseSpy = random.nextFloat() < 0.35f
        val next = state.copy(
            vitals = state.vitals.copy(
                approval = (state.vitals.approval - 2f).coerceIn(0f, 100f),
            ),
            diplomacy = state.diplomacy.updateRival(rival.id) {
                it.copy(
                    relationshipScore = (it.relationshipScore - 18).coerceIn(-100, 100),
                )
            },
            espionage = state.espionage.copy(
                spyCount = if (loseSpy) {
                    (state.espionage.spyCount - 1).coerceAtLeast(0)
                } else {
                    state.espionage.spyCount
                },
                intelligencePoints = (state.espionage.intelligencePoints - 4).coerceAtLeast(0),
            ),
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (state.internalSecurity.instabilityScore + 2f)
                    .coerceIn(0f, 100f),
            ),
        )
        val summary = buildString {
            append("Exposed · relations −18 · approval −2 · intel −4")
            if (loseSpy) append(" · spy lost")
            append(" · instability +2")
        }
        return next to summary
    }

    private fun regenerateIntelligence(state: GameState): GameState {
        val passiveIntel = 2 + state.espionage.spyCount / 4
        return state.copy(
            espionage = state.espionage.copy(
                intelligencePoints = (state.espionage.intelligencePoints + passiveIntel)
                    .coerceIn(0, MAX_INTEL_POINTS),
            ),
        )
    }

    private fun triggerCoup(state: GameState): GameState = state.copy(
        gameOver = GameOverState(
            isGameOver = true,
            reason = "Game Over: Coup d'État — the government has been overthrown.",
        ),
        internalSecurity = state.internalSecurity.copy(
            instabilityScore = 100f,
            coupRisk = 100f,
        ),
        vitals = state.vitals.copy(approval = 0f),
    )

    companion object {
        const val INSTABILITY_COUP_THRESHOLD = 75f
        const val SECURITY_FUND_UNIT_COST = 500_000_000L
        const val SECURITY_FUND_INSTABILITY_PER_UNIT = 3.5f
        const val SECURITY_FUND_COUP_PER_UNIT = 2f
        const val MONTHLY_SECURITY_FUND_CAP = 8
        const val PROTOCOL_TOGGLE_COOLDOWN_MONTHS = 3
        const val SPY_RECRUIT_COST = 2_000_000_000L
        const val MAX_INTEL_POINTS = 100
        const val MAX_MISSION_LOG = 12
        const val EXPOSURE_RETALIATION_THRESHOLD = 55f

        fun canDeploy(
            state: GameState,
            targetCountryId: String,
            missionType: MissionType,
        ): Boolean {
            if (state.gameOver.isGameOver) return false
            if (state.diplomacy.rivalById(targetCountryId) == null) return false
            if (state.espionage.availableSpies <= 0) return false
            if (state.vitals.budget < missionType.budgetCost) return false
            if (state.espionage.intelligencePoints < missionType.intelCost) return false
            return state.espionage.activeMissions.none {
                it.targetCountryId == targetCountryId &&
                    it.missionType == missionType &&
                    it.status == MissionStatus.ACTIVE
            }
        }

        fun maxAffordableSecurityUnits(state: GameState): Int {
            if (SECURITY_FUND_UNIT_COST <= 0L) return 0
            val byBudget = (state.vitals.budget / SECURITY_FUND_UNIT_COST).toInt().coerceAtLeast(0)
            val remainingCap = (
                MONTHLY_SECURITY_FUND_CAP - state.internalSecurity.securityFundsThisMonth
                ).coerceAtLeast(0)
            return minOf(byBudget, remainingCap)
        }

        fun protocolCooldownRemaining(state: GameState, protocol: SecurityProtocol): Int =
            state.internalSecurity.protocolCooldownMonths[protocol.name] ?: 0
    }
}

fun Float.toRiskString(): String = "${roundToInt()}%"
