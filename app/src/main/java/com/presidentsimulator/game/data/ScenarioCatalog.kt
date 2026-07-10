package com.presidentsimulator.game.data

import kotlin.random.Random

object ScenarioCatalog {
    val ALL = listOf(
        ScenarioPack(
            id = "standard",
            title = "Standard Mandate",
            tagline = "Balanced start — write your own legacy.",
            difficulty = ScenarioDifficulty.STANDARD,
        ),
        ScenarioPack(
            id = "powder_keg",
            title = "Powder Keg",
            tagline = "Hostile neighbors, thin treasury, election in 18 months.",
            difficulty = ScenarioDifficulty.HARD,
        ),
        ScenarioPack(
            id = "empty_granaries",
            title = "Empty Granaries",
            tagline = "Food crisis on day one — feed the nation or fall.",
            difficulty = ScenarioDifficulty.HARD,
        ),
        ScenarioPack(
            id = "palace_intrigue",
            title = "Palace Intrigue",
            tagline = "Corrupt cabinet, hostile press, restless officers.",
            difficulty = ScenarioDifficulty.HARD,
        ),
        ScenarioPack(
            id = "iron_curtain",
            title = "Iron Curtain",
            tagline = "Autocratic grip, embargoed trade, UN spotlight.",
            difficulty = ScenarioDifficulty.NIGHTMARE,
            recommendedNationId = "kryos",
        ),
        ScenarioPack(
            id = "reform_or_die",
            title = "Reform or Die",
            tagline = "Minority government, surging opposition, ticking clock.",
            difficulty = ScenarioDifficulty.NIGHTMARE,
        ),
    )

    fun byId(id: String): ScenarioPack = ALL.find { it.id == id } ?: ALL.first()

    fun apply(state: GameState, scenarioId: String, seed: Int = Random.Default.nextInt()): GameState {
        val pack = byId(scenarioId)
        val rng = Random(seed)
        var next = state.copy(
            scenario = ScenarioState(
                scenarioId = pack.id,
                title = pack.title,
                challengeSeed = seed,
                notes = listOf(pack.tagline),
            ),
        )

        next = when (pack.id) {
            "powder_keg" -> next.copy(
                vitals = next.vitals.copy(
                    budget = (next.vitals.budget * 0.55).toLong(),
                    approval = (next.vitals.approval - 8f).coerceIn(20f, 100f),
                ),
                nextElectionYear = next.year + 1,
                diplomacy = next.diplomacy.copy(
                    rivals = next.diplomacy.rivals.map { r ->
                        r.copy(relationshipScore = (r.relationshipScore - 25).coerceIn(-100, 100))
                    },
                ),
                military = next.military.copy(defcon = 3),
                scenario = next.scenario.copy(
                    victoryYearOverride = next.year + 12,
                    notes = next.scenario.notes + "Election accelerated · rivals hostile",
                ),
            )
            "empty_granaries" -> next.copy(
                production = next.production.copy(
                    food = 400L,
                    foodShortage = true,
                ),
                economy = next.economy.copy(farms = (next.economy.farms * 0.6).toInt().coerceAtLeast(8)),
                vitals = next.vitals.copy(approval = (next.vitals.approval - 10f).coerceIn(15f, 100f)),
                disaster = next.disaster.copy(readiness = 28f),
                scenario = next.scenario.copy(notes = next.scenario.notes + "Food stocks critical"),
            )
            "palace_intrigue" -> {
                val cabinet = next.cabinet.copy(
                    ministers = next.cabinet.ministers.map { m ->
                        if (rng.nextFloat() < 0.45f) {
                            m.copy(
                                traits = (m.traits + MinisterTrait.CORRUPT).distinct(),
                                scandalHeat = rng.nextInt(35, 60).toFloat(),
                                loyalty = (m.loyalty - 15f).coerceAtLeast(20f),
                            )
                        } else {
                            m
                        }
                    },
                    cohesion = 32f,
                )
                next.copy(
                    cabinet = cabinet,
                    press = next.press.copy(
                        mediaSentiment = 28f,
                        credibility = 35f,
                        leakRisk = 40f,
                    ),
                    internalSecurity = next.internalSecurity.copy(
                        coupRisk = 48f,
                        instabilityScore = 42f,
                    ),
                    scenario = next.scenario.copy(notes = next.scenario.notes + "Cabinet compromised"),
                )
            }
            "iron_curtain" -> next.copy(
                legal = next.legal.copy(ideology = Ideology.AUTOCRACY),
                diplomacy = next.diplomacy.copy(
                    rivals = next.diplomacy.rivals.map { r ->
                        r.copy(
                            relationshipScore = (r.relationshipScore - 35).coerceIn(-100, 40),
                            hasEmbargo = rng.nextFloat() < 0.4f,
                        )
                    },
                ),
                press = next.press.copy(pressFreedom = 22f, mediaSentiment = 40f, credibility = 30f),
                opposition = OppositionEngine.seedInitial(Ideology.AUTOCRACY, rng),
                scenario = next.scenario.copy(
                    victoryYearOverride = next.year + 16,
                    notes = next.scenario.notes + "Isolated autocracy",
                ),
            )
            "reform_or_die" -> {
                val base = if (next.opposition.parties.isEmpty()) {
                    OppositionEngine.seedInitial(next.legal.ideology, rng)
                } else {
                    next.opposition
                }
                val ruling = base.rulingParty ?: return next
                val main = base.mainOpposition ?: return next
                val others = base.parties.filter { !it.isRuling && it.id != main.id }
                val parties = buildList {
                    add(ruling.copy(seats = 44, popularity = 36f))
                    add(main.copy(seats = 34, popularity = 46f, hostility = 72f))
                    others.forEachIndexed { i, p ->
                        add(p.copy(seats = if (i == 0) 14 else 8, hostility = 55f))
                    }
                }
                next.copy(
                    opposition = base.copy(parties = parties, noConfidenceHeat = 35f),
                    nextElectionYear = next.year + 2,
                    demographics = next.demographics.copy(oppositionMomentum = 18f),
                    vitals = next.vitals.copy(approval = 42f),
                    scenario = next.scenario.copy(
                        notes = next.scenario.notes + "Minority government · early election",
                    ),
                )
            }
            else -> next
        }

        return next.copy(
            legacy = next.legacy.copy(
                lastLegacyNote = "Scenario: ${pack.title}",
                entries = listOf(
                    LegacyEntry(
                        id = "scenario_$seed",
                        year = next.year,
                        month = next.month,
                        title = "Mandate begins: ${pack.title}",
                        detail = pack.tagline,
                        pillar = LegacyPillar.MANDATE,
                        tone = LegacyTone.TURNING_POINT,
                        scoreDelta = 0,
                    ),
                ),
            ),
        )
    }
}
