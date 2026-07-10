package com.presidentsimulator.game.data

import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Disaster Response Command: spawn, stage progression, player allocations,
 * and fallout into production, press, approval, and lingering crisis.
 */
object DisasterEngine {

    const val SPAWN_CHANCE = 0.12f
    const val RESPONSE_COOLDOWN = 1

    fun processMonth(state: GameState, random: Random = Random.Default): GameState {
        if (state.gameOver.isGameOver) return state
        var next = state
        var disaster = next.disaster.copy(
            responseCooldownMonths = (next.disaster.responseCooldownMonths - 1).coerceAtLeast(0),
            readiness = (next.disaster.readiness + readinessDrift(next)).coerceIn(10f, 95f),
        )
        next = next.copy(disaster = disaster)

        if (disaster.active == null) {
            if (canSpawn(next) && random.nextFloat() < SPAWN_CHANCE) {
                next = spawnDisaster(next, random)
            }
            return next
        }

        return tickActiveDisaster(next, random)
    }

    fun allocateResponse(
        state: GameState,
        focus: ResponseFocus,
        random: Random = Random.Default,
    ): GameState {
        if (state.gameOver.isGameOver) return state
        val active = state.disaster.active ?: return state
        if (state.disaster.responseCooldownMonths > 0) return state
        if (state.vitals.budget < focus.cost) return state

        val healthMinister = state.cabinet.ministerFor(CabinetPortfolio.HEALTH)
        val interior = state.cabinet.ministerFor(CabinetPortfolio.INTERIOR)
        val healthBonus = ((healthMinister?.competence ?: 45f) - 50f) / 100f
        val interiorBonus = ((interior?.competence ?: 45f) - 50f) / 100f
        val readinessBonus = (state.disaster.readiness - 50f) / 200f

        var points = when (focus) {
            ResponseFocus.EMERGENCY_FUNDS -> 14f + readinessBonus * 10f
            ResponseFocus.MILITARY_AID -> 12f + interiorBonus * 18f
            ResponseFocus.MEDICAL_SURGE -> 13f + healthBonus * 22f
            ResponseFocus.REBUILD_CONTRACTS -> 16f + readinessBonus * 8f
            ResponseFocus.PUBLIC_ADDRESS -> 6f
        }
        // Type match bonuses
        points += when {
            focus == ResponseFocus.MEDICAL_SURGE && active.type == DisasterType.EPIDEMIC -> 6f
            focus == ResponseFocus.MILITARY_AID &&
                active.type in listOf(DisasterType.FLOOD, DisasterType.EARTHQUAKE, DisasterType.WILDFIRE) -> 5f
            focus == ResponseFocus.REBUILD_CONTRACTS &&
                active.type in listOf(DisasterType.EARTHQUAKE, DisasterType.INDUSTRIAL) -> 5f
            focus == ResponseFocus.EMERGENCY_FUNDS && active.type == DisasterType.DROUGHT -> 4f
            else -> 0f
        }

        val bungled = random.nextFloat() < (0.08f + if (state.disaster.readiness < 30f) 0.12f else 0f)
        if (bungled) points *= 0.45f

        val newSeverity = (active.severity - points * 0.55f).coerceIn(0f, 100f)
        val lives = (points * 2_500L * (1f + healthBonus)).toLong().coerceAtLeast(0L)
        val approvalGain = when (focus) {
            ResponseFocus.PUBLIC_ADDRESS -> 2.2f + if (bungled) -3f else 0f
            else -> points * 0.08f + if (bungled) -1.5f else 0.4f
        }

        var stage = active.stage
        if (newSeverity < 35f && stage == DisasterStage.PEAK) stage = DisasterStage.CONTAINMENT
        if (newSeverity < 20f && stage == DisasterStage.CONTAINMENT) stage = DisasterStage.RECOVERY

        val updated = active.copy(
            severity = newSeverity,
            stage = stage,
            responsePoints = active.responsePoints + points,
            fundsSpent = active.fundsSpent + focus.cost,
            livesSavedEstimate = active.livesSavedEstimate + lives,
            mismanaged = active.mismanaged || bungled,
            label = active.label,
        )

        val note = if (bungled) {
            "${focus.displayName} deployment bungled — severity still ${newSeverity.roundToInt()}."
        } else {
            "${focus.displayName} deployed · severity ${newSeverity.roundToInt()} · ~${lives / 1000}k aided."
        }

        var press = state.press
        if (focus == ResponseFocus.PUBLIC_ADDRESS || points >= 15f) {
            val tone = if (bungled) HeadlineTone.NEGATIVE else HeadlineTone.POSITIVE
            press = press.copy(
                headlines = (
                    listOf(
                        PressHeadline(
                            id = UUID.randomUUID().toString(),
                            title = if (bungled) {
                                "Disaster response draws fire"
                            } else {
                                "Palace mobilizes for ${active.type.displayName.lowercase()}"
                            },
                            lede = note,
                            tone = tone,
                            year = state.year,
                            month = state.month,
                            outlet = PressOutlet.STATE_BROADCAST,
                            topic = PressTopic.SOCIETY,
                        ),
                    ) + press.headlines
                    ).take(14),
                mediaSentiment = (press.mediaSentiment + if (bungled) -3f else 2.5f).coerceIn(0f, 100f),
            )
        }

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - focus.cost,
                approval = (state.vitals.approval + approvalGain).coerceIn(0f, 100f),
            ),
            disaster = state.disaster.copy(
                active = updated,
                responseCooldownMonths = RESPONSE_COOLDOWN,
                readiness = (state.disaster.readiness + if (bungled) -2f else 1.5f).coerceIn(10f, 95f),
                lastCommandNote = note,
            ).appendLog(note),
            press = press,
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (
                    state.internalSecurity.instabilityScore - points * 0.06f + if (bungled) 1.5f else 0f
                    ).coerceIn(0f, 100f),
            ),
        )
    }

    private fun canSpawn(state: GameState): Boolean {
        if (state.disaster.active != null) return false
        if (state.crisis.pendingEventId != null) return false
        // Don't stack on top of peak war chaos every time — still allow sometimes
        return true
    }

    private fun spawnDisaster(state: GameState, random: Random): GameState {
        val type = weightedType(state, random)
        val severity = when {
            state.disaster.readiness < 30f -> random.nextInt(55, 78).toFloat()
            state.society.healthLevel < 40f && type == DisasterType.EPIDEMIC ->
                random.nextInt(60, 82).toFloat()
            else -> random.nextInt(38, 65).toFloat()
        }
        val disaster = ActiveDisaster(
            id = UUID.randomUUID().toString(),
            type = type,
            stage = DisasterStage.ALERT,
            severity = severity,
            label = "${type.displayName} emergency",
        )
        val headline = PressHeadline(
            id = UUID.randomUUID().toString(),
            title = "BREAKING: ${type.displayName} emergency declared",
            lede = type.blurb,
            tone = HeadlineTone.SCANDAL,
            year = state.year,
            month = state.month,
            outlet = PressOutlet.NATIONAL_BROADSHEET,
            topic = PressTopic.SOCIETY,
        )
        return state.copy(
            disaster = state.disaster.copy(
                active = disaster,
                lastCommandNote = "${type.displayName} declared — open Response Command.",
            ).appendLog("Spawned ${type.displayName} (severity ${severity.roundToInt()})"),
            press = state.press.copy(
                headlines = (listOf(headline) + state.press.headlines).take(14),
                mediaSentiment = (state.press.mediaSentiment - 4f).coerceIn(0f, 100f),
                lastDeskNote = "${type.displayName} owns the cycle.",
            ),
            vitals = state.vitals.copy(
                approval = (state.vitals.approval - 1.5f).coerceIn(0f, 100f),
            ),
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (state.internalSecurity.instabilityScore + 2f).coerceIn(0f, 100f),
            ),
        )
    }

    private fun tickActiveDisaster(state: GameState, random: Random): GameState {
        val active = state.disaster.active ?: return state
        var severity = active.severity
        var stage = active.stage
        val months = active.monthsActive + 1

        // Natural escalation / decay
        when (stage) {
            DisasterStage.ALERT -> {
                severity = (severity + 6f + random.nextFloat() * 4f).coerceAtMost(95f)
                if (months >= 2 || severity >= 55f) stage = DisasterStage.PEAK
            }
            DisasterStage.PEAK -> {
                severity = (severity + 2f - active.responsePoints * 0.05f).coerceIn(15f, 100f)
                if (active.responsePoints >= 25f || months >= 4) stage = DisasterStage.CONTAINMENT
            }
            DisasterStage.CONTAINMENT -> {
                severity = (severity - 4f - active.responsePoints * 0.08f).coerceAtLeast(5f)
                if (severity <= 22f || months >= 6) stage = DisasterStage.RECOVERY
            }
            DisasterStage.RECOVERY -> {
                severity = (severity - 8f).coerceAtLeast(0f)
            }
        }

        // Neglect penalty
        if (active.responsePoints < months * 4f) {
            severity = (severity + 3f).coerceAtMost(100f)
        }

        var next = applyDisasterPressure(
            state.copy(
                disaster = state.disaster.copy(
                    active = active.copy(
                        severity = severity,
                        stage = stage,
                        monthsActive = months,
                    ),
                    lastCommandNote = "${active.type.displayName}: ${stage.name.lowercase()} · severity ${severity.roundToInt()}",
                ),
            ),
            severity,
            active.type,
        )

        // Resolve when recovered and mild
        if (stage == DisasterStage.RECOVERY && severity <= 12f) {
            next = resolveDisaster(next, success = !active.mismanaged && active.responsePoints >= 18f)
        } else if (months >= 9 && severity >= 70f) {
            // Catastrophic mismanagement — force resolve as failure into lingering crisis
            next = resolveDisaster(next, success = false)
        }

        return next
    }

    private fun applyDisasterPressure(
        state: GameState,
        severity: Float,
        type: DisasterType,
    ): GameState {
        val scale = severity / 100f
        var production = state.production
        var vitals = state.vitals
        var security = state.internalSecurity
        var society = state.society

        when (type) {
            DisasterType.FLOOD, DisasterType.DROUGHT, DisasterType.WILDFIRE -> {
                if (severity >= 45f && type == DisasterType.DROUGHT) {
                    production = production.copy(
                        food = (production.food - (400 * scale).toLong()).coerceAtLeast(0L),
                        foodShortage = true,
                    )
                } else if (severity >= 55f) {
                    production = production.copy(
                        food = (production.food - (200 * scale).toLong()).coerceAtLeast(0L),
                        foodShortage = production.foodShortage || severity >= 70f,
                    )
                }
                vitals = vitals.copy(
                    approval = (vitals.approval - 0.4f * scale).coerceIn(0f, 100f),
                    population = (vitals.population - (8_000 * scale).toLong()).coerceAtLeast(1_000_000L),
                )
            }
            DisasterType.EARTHQUAKE, DisasterType.INDUSTRIAL -> {
                vitals = vitals.copy(
                    approval = (vitals.approval - 0.5f * scale).coerceIn(0f, 100f),
                )
                security = security.copy(
                    instabilityScore = (security.instabilityScore + 0.6f * scale).coerceIn(0f, 100f),
                )
                if (severity >= 50f) {
                    production = production.copy(
                        materials = (production.materials - (150 * scale).toLong()).coerceAtLeast(0L),
                        energyShortage = production.energyShortage || type == DisasterType.INDUSTRIAL,
                    )
                }
            }
            DisasterType.EPIDEMIC -> {
                society = society.copy(
                    healthLevel = (society.healthLevel - 0.7f * scale).coerceIn(0f, 100f),
                )
                vitals = vitals.copy(
                    approval = (vitals.approval - 0.55f * scale).coerceIn(0f, 100f),
                    population = (vitals.population - (15_000 * scale).toLong()).coerceAtLeast(1_000_000L),
                )
            }
        }

        return state.copy(
            vitals = vitals,
            production = production,
            internalSecurity = security,
            society = society,
        )
    }

    private fun resolveDisaster(state: GameState, success: Boolean): GameState {
        val active = state.disaster.active ?: return state
        val note = if (success) {
            "${active.type.displayName} contained. ~${active.livesSavedEstimate / 1000}k aided."
        } else {
            "${active.type.displayName} left lasting scars. Inquiry opens."
        }

        val lingering = if (!success || active.severity > 30f) {
            ActiveCrisisState(
                pendingEventId = null,
                lingeringMonths = if (success) 2 else 4,
                monthlyApprovalDelta = if (success) -0.4f else -1.2f,
                monthlyInstabilityDelta = if (success) 0.3f else 1.1f,
                monthlyBudgetDelta = if (success) -200_000_000L else -600_000_000L,
                label = "${active.type.displayName} aftermath",
            )
        } else {
            state.crisis
        }

        val headline = PressHeadline(
            id = UUID.randomUUID().toString(),
            title = if (success) {
                "${active.type.displayName} emergency winding down"
            } else {
                "Inquiry: ${active.type.displayName} response failed"
            },
            lede = note,
            tone = if (success) HeadlineTone.NEUTRAL else HeadlineTone.SCANDAL,
            year = state.year,
            month = state.month,
            outlet = PressOutlet.NATIONAL_BROADSHEET,
            topic = PressTopic.SOCIETY,
        )

        return state.copy(
            disaster = state.disaster.copy(
                active = null,
                disastersHandled = state.disaster.disastersHandled + if (success) 1 else 0,
                disastersMismanaged = state.disaster.disastersMismanaged + if (success) 0 else 1,
                readiness = (state.disaster.readiness + if (success) 4f else -6f).coerceIn(10f, 95f),
                lastCommandNote = note,
            ).appendLog(note),
            crisis = if (state.crisis.lingeringMonths > 0 && lingering.lingeringMonths > 0) {
                state.crisis.copy(
                    lingeringMonths = maxOf(state.crisis.lingeringMonths, lingering.lingeringMonths),
                    label = lingering.label.ifBlank { state.crisis.label },
                )
            } else if (lingering.lingeringMonths > 0) {
                lingering
            } else {
                state.crisis
            },
            press = state.press.copy(
                headlines = (listOf(headline) + state.press.headlines).take(14),
                mediaSentiment = (state.press.mediaSentiment + if (success) 3f else -5f)
                    .coerceIn(0f, 100f),
            ),
            vitals = state.vitals.copy(
                approval = (state.vitals.approval + if (success) 2f else -4f).coerceIn(0f, 100f),
            ),
            demographics = state.demographics.withClamp(
                working = state.demographics.workingClass + if (success) 1.5f else -2.5f,
                reasons = state.demographics.recentReasons + note,
            ),
        )
    }

    private fun weightedType(state: GameState, random: Random): DisasterType {
        val weights = mutableListOf<Pair<DisasterType, Int>>()
        weights += DisasterType.FLOOD to 3
        weights += DisasterType.EARTHQUAKE to 2
        weights += DisasterType.WILDFIRE to 2
        weights += DisasterType.INDUSTRIAL to if (state.economy.factories >= 20) 3 else 1
        weights += DisasterType.DROUGHT to if (state.production.foodShortage) 4 else 2
        weights += DisasterType.EPIDEMIC to if (state.society.healthLevel < 50f) 4 else 2
        val total = weights.sumOf { it.second }
        var roll = random.nextInt(total)
        weights.forEach { (type, w) ->
            roll -= w
            if (roll < 0) return type
        }
        return DisasterType.FLOOD
    }

    private fun readinessDrift(state: GameState): Float {
        var d = 0.15f
        val health = state.cabinet.ministerFor(CabinetPortfolio.HEALTH)
        if (health != null && health.competence >= 65f) d += 0.25f
        if (state.society.healthLevel >= 60f) d += 0.2f
        if (state.disaster.disastersMismanaged > state.disaster.disastersHandled) d -= 0.4f
        return d
    }
}
