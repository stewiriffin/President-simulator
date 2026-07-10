package com.presidentsimulator.game.data

import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Pure cabinet simulation: appointments, firings, tenure, scandals, resignations,
 * and monthly effect application (skim, cohesion, diplomacy drift).
 */
object CabinetEngine {

    const val FIRE_COST = 1_500_000_000L
    const val FIRE_COOLDOWN = 2
    const val APPOINT_COOLDOWN = 1
    const val RESHUFFLE_COOLDOWN = 3
    const val CANDIDATE_POOL = 3

    private val FIRST_NAMES = listOf(
        "Elena", "Marcus", "Sofia", "Viktor", "Amara", "Jonas", "Priya", "Dmitri",
        "Helena", "Omar", "Ingrid", "Kai", "Nadia", "Felix", "Yara", "Tomas",
        "Clara", "Ravi", "Anika", "Leon", "Mira", "Stefan", "Zara", "Hugo",
    )
    private val LAST_NAMES = listOf(
        "Varga", "Okoye", "Lindqvist", "Chen", "Moreau", "Petrov", "Al-Rashid",
        "Nakamura", "Silva", "Kowalski", "Berg", "Duarte", "Ibrahim", "Novak",
        "Reyes", "Hassan", "Volkov", "Andersen", "Patel", "Costa",
    )

    fun seedInitial(random: Random = Random.Default): CabinetState {
        val ministers = CabinetPortfolio.entries.map { portfolio ->
            generateMinister(portfolio, random, starter = true)
        }
        return CabinetState(
            ministers = ministers,
            candidates = emptyList(),
            cohesion = 58f,
            lastCabinetNote = "Opening cabinet sworn in.",
        ).appendLog("Opening cabinet seated (${ministers.size} portfolios).")
            .let { refreshCandidates(it, random) }
    }

    fun processMonth(state: GameState, random: Random = Random.Default): GameState {
        if (state.gameOver.isGameOver) return state
        var cabinet = state.cabinet
        if (cabinet.ministers.isEmpty() && cabinet.candidates.isEmpty()) {
            cabinet = seedInitial(random)
        }

        cabinet = cabinet.copy(
            fireCooldownMonths = (cabinet.fireCooldownMonths - 1).coerceAtLeast(0),
            appointCooldownMonths = (cabinet.appointCooldownMonths - 1).coerceAtLeast(0),
            reshuffleCooldownMonths = (cabinet.reshuffleCooldownMonths - 1).coerceAtLeast(0),
        )

        // Age tenure, drift competence/loyalty, grow scandal heat.
        cabinet = cabinet.copy(
            ministers = cabinet.ministers.map { m ->
                var heat = m.scandalHeat
                var loyalty = m.loyalty
                var competence = m.competence
                heat += when {
                    m.isCorrupt -> 2.2f
                    MinisterTrait.IDEALIST in m.traits -> 0.4f
                    else -> 0.6f
                }
                if (state.press.openScandalCount > 0) heat += 1.2f
                if (state.vitals.approval < 35f) loyalty -= 1.5f
                if (state.vitals.approval >= 65f) loyalty += 0.6f
                if (m.isLoyal) {
                    loyalty += 0.8f
                    heat -= 0.8f
                }
                if (MinisterTrait.COMPETENT in m.traits) competence += 0.15f
                if (m.monthsInOffice > 24) competence = (competence - 0.2f).coerceAtLeast(25f)
                m.copy(
                    monthsInOffice = m.monthsInOffice + 1,
                    scandalHeat = heat.coerceIn(0f, 100f),
                    loyalty = loyalty.coerceIn(5f, 100f),
                    competence = competence.coerceIn(15f, 98f),
                )
            },
        )

        // Cohesion from loyalty average and vacancies.
        val avgLoyalty = cabinet.ministers.map { it.loyalty }.average().toFloat()
            .takeIf { cabinet.ministers.isNotEmpty() } ?: 40f
        var cohesion = cabinet.cohesion
        cohesion += (avgLoyalty - 50f) * 0.04f
        cohesion -= cabinet.vacancyCount * 1.5f
        cohesion -= cabinet.ministers.count { it.scandalHeat >= 50f } * 2f
        cohesion = cohesion.coerceIn(0f, 100f)
        cabinet = cabinet.copy(cohesion = cohesion)

        val effects = cabinet.combinedEffects()
        var next = state.copy(cabinet = cabinet)

        // Apply skim, approval, instability.
        next = next.copy(
            vitals = next.vitals.copy(
                budget = (next.vitals.budget - effects.budgetSkimPerMonth).coerceAtLeast(0L),
                approval = (next.vitals.approval + effects.approvalDelta).coerceIn(0f, 100f),
            ),
            internalSecurity = next.internalSecurity.copy(
                instabilityScore = (
                    next.internalSecurity.instabilityScore + effects.instabilityDelta
                    ).coerceIn(0f, 100f),
            ),
        )

        // Soft diplomacy drift from Foreign minister.
        if (effects.diplomacyDrift != 0f && next.diplomacy.rivals.isNotEmpty()) {
            val drift = effects.diplomacyDrift
            next = next.copy(
                diplomacy = next.diplomacy.copy(
                    rivals = next.diplomacy.rivals.map { rival ->
                        if (rival.hasEmbargo) rival
                        else rival.copy(
                            relationshipScore = (rival.relationshipScore + drift)
                                .roundToInt()
                                .coerceIn(-100, 100),
                        )
                    },
                ),
            )
        }

        // Forced resignations / scandals.
        next = resolveResignationsAndScandals(next, random)

        // Keep candidate pool fresh when vacancies exist.
        if (next.cabinet.vacancyCount > 0 && next.cabinet.candidates.isEmpty()) {
            next = next.copy(cabinet = refreshCandidates(next.cabinet, random))
        } else if (random.nextFloat() < 0.25f) {
            next = next.copy(cabinet = refreshCandidates(next.cabinet, random))
        }

        val note = when {
            next.cabinet.vacancyCount >= 3 -> "Cabinet hollowed out — ${next.cabinet.vacancyCount} seats empty."
            next.cabinet.hottestScandal != null ->
                "Heat on ${next.cabinet.hottestScandal!!.name} (${next.cabinet.hottestScandal!!.portfolio.shortName})."
            next.cabinet.cohesion < 35f -> "Cabinet cohesion collapsing."
            effects.budgetSkimPerMonth > 0L -> "Quiet skimming continues in the ministries."
            else -> "Cabinet holding steady · ${next.cabinet.cohesionLabel}."
        }
        return next.copy(
            cabinet = next.cabinet.copy(lastCabinetNote = note),
        )
    }

    fun appointCandidate(state: GameState, candidateId: String): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.cabinet.appointCooldownMonths > 0) return state
        val candidate = state.cabinet.candidates.find { it.id == candidateId } ?: return state
        if (state.cabinet.ministerFor(candidate.portfolio) != null) return state
        if (state.vitals.budget < candidate.hireCost) return state

        val minister = Minister(
            id = candidate.id,
            name = candidate.name,
            portfolio = candidate.portfolio,
            competence = candidate.competence,
            loyalty = candidate.loyalty,
            traits = candidate.traits,
            monthsInOffice = 0,
            originNote = candidate.pitch,
        )
        val cabinet = state.cabinet.copy(
            ministers = state.cabinet.ministers + minister,
            candidates = state.cabinet.candidates.filter { it.id != candidateId },
            appointCooldownMonths = APPOINT_COOLDOWN,
            cohesion = (state.cabinet.cohesion + 4f).coerceIn(0f, 100f),
            lastCabinetNote = "${minister.name} sworn in as ${minister.portfolio.displayName}.",
        ).appendLog("Appointed ${minister.name} → ${minister.portfolio.shortName}")

        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - candidate.hireCost),
            cabinet = refreshCandidates(cabinet, Random.Default),
            demographics = state.demographics.withClamp(
                business = state.demographics.businessElite +
                    if (MinisterTrait.REFORMER in minister.traits) -1.5f else 0.8f,
                academic = state.demographics.academics +
                    if (MinisterTrait.TECHNOCRAT in minister.traits) 1.2f else 0f,
                reasons = state.demographics.recentReasons + "Cabinet appointment: ${minister.name}",
            ),
        )
    }

    fun fireMinister(state: GameState, portfolio: CabinetPortfolio): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.cabinet.fireCooldownMonths > 0) return state
        if (state.vitals.budget < FIRE_COST) return state
        val target = state.cabinet.ministerFor(portfolio) ?: return state

        val loyaltyHit = if (target.isLoyal) 3.5f else 1.5f
        val remaining = state.cabinet.ministers.filter { it.portfolio != portfolio }.map { m ->
            m.copy(loyalty = (m.loyalty - loyaltyHit).coerceIn(5f, 100f))
        }
        val cabinet = state.cabinet.copy(
            ministers = remaining,
            fireCooldownMonths = FIRE_COOLDOWN,
            firingsThisTerm = state.cabinet.firingsThisTerm + 1,
            cohesion = (state.cabinet.cohesion - 8f - if (target.isLoyal) 5f else 0f)
                .coerceIn(0f, 100f),
            lastCabinetNote = "${target.name} dismissed from ${target.portfolio.shortName}.",
        ).appendLog("Fired ${target.name} (${target.portfolio.shortName})")

        // Fired ministers sometimes leak to the press.
        val leakPress = if (target.scandalHeat >= 40f || target.loyalty < 35f) {
            val headline = PressHeadline(
                id = UUID.randomUUID().toString(),
                title = "Ex-minister ${target.name} goes public",
                lede = "A bitter exit interview lights up the evening cycle.",
                tone = HeadlineTone.SCANDAL,
                year = state.year,
                month = state.month,
                outlet = PressOutlet.TABLOID,
                topic = PressTopic.SCANDAL,
            )
            state.press.copy(
                headlines = (listOf(headline) + state.press.headlines).take(14),
                mediaSentiment = (state.press.mediaSentiment - 4f).coerceIn(0f, 100f),
                lastDeskNote = "Cabinet firing became a media event.",
            )
        } else {
            state.press
        }

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - FIRE_COST,
                approval = (state.vitals.approval - if (target.competence >= 70f) 2f else 0.8f)
                    .coerceIn(0f, 100f),
            ),
            cabinet = refreshCandidates(cabinet, Random.Default),
            press = leakPress,
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (
                    state.internalSecurity.instabilityScore + if (target.portfolio == CabinetPortfolio.INTERIOR) 2f else 0.8f
                    ).coerceIn(0f, 100f),
            ),
        )
    }

    fun reshuffleCandidates(state: GameState, random: Random = Random.Default): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.cabinet.reshuffleCooldownMonths > 0) return state
        val cost = 800_000_000L
        if (state.vitals.budget < cost) return state
        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - cost),
            cabinet = refreshCandidates(state.cabinet, random).copy(
                reshuffleCooldownMonths = RESHUFFLE_COOLDOWN,
                lastCabinetNote = "Personnel office sent a fresh shortlist.",
            ).appendLog("Candidate pool reshuffled"),
        )
    }

    private fun resolveResignationsAndScandals(
        state: GameState,
        random: Random,
    ): GameState {
        var cabinet = state.cabinet
        var press = state.press
        var approval = state.vitals.approval
        var instability = state.internalSecurity.instabilityScore
        val resigned = mutableListOf<String>()

        val survivors = cabinet.ministers.mapNotNull { m ->
            val resignChance = when {
                m.loyalty < 25f -> 0.35f
                m.scandalHeat >= 75f -> 0.45f
                m.scandalHeat >= 55f && m.loyalty < 45f -> 0.22f
                cabinet.cohesion < 30f && m.loyalty < 40f -> 0.18f
                else -> 0.02f
            }
            if (random.nextFloat() < resignChance) {
                resigned += m.name
                // Scandal splash
                if (m.scandalHeat >= 50f || random.nextFloat() < 0.4f) {
                    val headline = PressHeadline(
                        id = UUID.randomUUID().toString(),
                        title = "${m.portfolio.shortName} minister ${m.name} resigns",
                        lede = if (m.scandalHeat >= 50f) {
                            "Scandal heat forced the exit — opposition smells blood."
                        } else {
                            "A sudden resignation rattles the cabinet corridor."
                        },
                        tone = if (m.scandalHeat >= 50f) HeadlineTone.SCANDAL else HeadlineTone.NEGATIVE,
                        year = state.year,
                        month = state.month,
                        outlet = PressOutlet.NATIONAL_BROADSHEET,
                        topic = PressTopic.SCANDAL,
                    )
                    press = press.copy(
                        headlines = (listOf(headline) + press.headlines).take(14),
                        mediaSentiment = (press.mediaSentiment - 3f).coerceIn(0f, 100f),
                    )
                    cabinet = cabinet.copy(scandalsThisTerm = cabinet.scandalsThisTerm + 1)
                }
                approval -= if (m.competence >= 65f) 1.8f else 0.9f
                instability += 1.2f
                null
            } else {
                // Passive scandal tick without resign
                if (m.scandalHeat >= 60f && random.nextFloat() < 0.2f) {
                    val headline = PressHeadline(
                        id = UUID.randomUUID().toString(),
                        title = "Questions swirl around ${m.name}",
                        lede = "Opposition MPs demand answers on ${m.portfolio.shortName} portfolio.",
                        tone = HeadlineTone.NEGATIVE,
                        year = state.year,
                        month = state.month,
                        outlet = PressOutlet.DIGITAL_WIRE,
                        topic = PressTopic.SCANDAL,
                    )
                    press = press.copy(
                        headlines = (listOf(headline) + press.headlines).take(14),
                        mediaSentiment = (press.mediaSentiment - 1.5f).coerceIn(0f, 100f),
                    )
                    cabinet = cabinet.copy(scandalsThisTerm = cabinet.scandalsThisTerm + 1)
                }
                m
            }
        }

        if (resigned.isEmpty()) {
            return state.copy(cabinet = cabinet, press = press)
        }

        cabinet = cabinet.copy(
            ministers = survivors,
            resignationsThisTerm = cabinet.resignationsThisTerm + resigned.size,
            cohesion = (cabinet.cohesion - resigned.size * 6f).coerceIn(0f, 100f),
            lastCabinetNote = "Resignation(s): ${resigned.joinToString()}",
        ).appendLog("Resigned: ${resigned.joinToString()}")

        return state.copy(
            cabinet = refreshCandidates(cabinet, random),
            press = press.copy(lastDeskNote = "Cabinet resignation dominates the cycle."),
            vitals = state.vitals.copy(approval = approval.coerceIn(0f, 100f)),
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = instability.coerceIn(0f, 100f),
            ),
        )
    }

    fun refreshCandidates(cabinet: CabinetState, random: Random): CabinetState {
        val vacancies = cabinet.vacantPortfolios
        if (vacancies.isEmpty()) {
            return cabinet.copy(candidates = emptyList())
        }
        val pool = vacancies.shuffled(random).take(CANDIDATE_POOL).map { portfolio ->
            generateCandidate(portfolio, random)
        }
        // Keep any existing candidates for still-vacant seats that aren't replaced
        return cabinet.copy(candidates = pool)
    }

    private fun generateMinister(
        portfolio: CabinetPortfolio,
        random: Random,
        starter: Boolean,
    ): Minister {
        val traits = rollTraits(portfolio, random, starter)
        val competence = when {
            starter -> random.nextInt(48, 72).toFloat()
            MinisterTrait.COMPETENT in traits -> random.nextInt(68, 90).toFloat()
            MinisterTrait.CRONY in traits -> random.nextInt(35, 55).toFloat()
            else -> random.nextInt(42, 78).toFloat()
        }
        val loyalty = when {
            MinisterTrait.LOYAL in traits || MinisterTrait.CRONY in traits ->
                random.nextInt(65, 92).toFloat()
            MinisterTrait.IDEALIST in traits -> random.nextInt(40, 70).toFloat()
            else -> random.nextInt(45, 75).toFloat()
        }
        return Minister(
            id = UUID.randomUUID().toString(),
            name = randomName(random),
            portfolio = portfolio,
            competence = competence,
            loyalty = loyalty,
            traits = traits,
            monthsInOffice = if (starter) random.nextInt(0, 8) else 0,
            originNote = if (starter) "Founding cabinet" else "Appointed mid-term",
        )
    }

    private fun generateCandidate(portfolio: CabinetPortfolio, random: Random): MinisterCandidate {
        val traits = rollTraits(portfolio, random, starter = false)
        val competence = when {
            MinisterTrait.COMPETENT in traits -> random.nextInt(70, 92).toFloat()
            MinisterTrait.CRONY in traits -> random.nextInt(32, 52).toFloat()
            MinisterTrait.CORRUPT in traits -> random.nextInt(40, 68).toFloat()
            else -> random.nextInt(45, 80).toFloat()
        }
        val loyalty = when {
            MinisterTrait.LOYAL in traits || MinisterTrait.CRONY in traits ->
                random.nextInt(70, 95).toFloat()
            else -> random.nextInt(40, 78).toFloat()
        }
        val hireCost = (1_200_000_000L + (competence * 40_000_000L).toLong() +
            if (MinisterTrait.COMPETENT in traits) 800_000_000L else 0L)
        val pitch = buildPitch(portfolio, traits, competence)
        return MinisterCandidate(
            id = UUID.randomUUID().toString(),
            name = randomName(random),
            portfolio = portfolio,
            competence = competence,
            loyalty = loyalty,
            traits = traits,
            hireCost = hireCost,
            pitch = pitch,
        )
    }

    private fun rollTraits(
        portfolio: CabinetPortfolio,
        random: Random,
        starter: Boolean,
    ): List<MinisterTrait> {
        val pool = mutableListOf(MinisterTrait.COMPETENT, MinisterTrait.LOYAL, MinisterTrait.CRONY)
        when (portfolio) {
            CabinetPortfolio.DEFENSE -> pool += listOf(MinisterTrait.HAWK, MinisterTrait.LOYAL)
            CabinetPortfolio.FOREIGN -> pool += listOf(MinisterTrait.DIPLOMAT, MinisterTrait.HAWK)
            CabinetPortfolio.SCIENCE, CabinetPortfolio.INDUSTRY ->
                pool += listOf(MinisterTrait.TECHNOCRAT, MinisterTrait.REFORMER)
            CabinetPortfolio.HEALTH, CabinetPortfolio.EDUCATION, CabinetPortfolio.CULTURE ->
                pool += listOf(MinisterTrait.REFORMER, MinisterTrait.IDEALIST, MinisterTrait.POPULIST)
            CabinetPortfolio.ECONOMY ->
                pool += listOf(MinisterTrait.TECHNOCRAT, MinisterTrait.CORRUPT, MinisterTrait.POPULIST)
            CabinetPortfolio.INTERIOR ->
                pool += listOf(MinisterTrait.LOYAL, MinisterTrait.CORRUPT, MinisterTrait.HAWK)
        }
        if (!starter) pool += MinisterTrait.CORRUPT
        val count = if (starter) 1 else random.nextInt(1, 3)
        return pool.shuffled(random).distinct().take(count)
    }

    private fun buildPitch(
        portfolio: CabinetPortfolio,
        traits: List<MinisterTrait>,
        competence: Float,
    ): String {
        val traitText = traits.joinToString { it.displayName.lowercase() }
        return "Shortlisted for ${portfolio.displayName} · grade lean ${
            when {
                competence >= 75f -> "A"
                competence >= 60f -> "B"
                else -> "C"
            }
        } · $traitText"
    }

    private fun randomName(random: Random): String =
        "${FIRST_NAMES.random(random)} ${LAST_NAMES.random(random)}"
}
