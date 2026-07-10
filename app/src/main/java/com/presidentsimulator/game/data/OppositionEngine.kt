package com.presidentsimulator.game.data

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Named parties, chamber seats, and opposition tactics that pressure laws,
 * approval, press, and election challengers.
 */
object OppositionEngine {

    const val NEGOTIATE_COST = 2_500_000_000L
    const val SMEAR_COST = 1_800_000_000L
    const val CONCESSION_COST = 1_200_000_000L

    fun seedInitial(ideology: Ideology, random: Random = Random.Default): OppositionState {
        val rulingName = when (ideology) {
            Ideology.DEMOCRACY -> "Governing Alliance"
            Ideology.AUTOCRACY -> "National Order Party"
            Ideology.COMMUNISM -> "Workers' Vanguard"
        }
        val rulingLean = when (ideology) {
            Ideology.COMMUNISM -> PartyLean.LEFT
            Ideology.AUTOCRACY -> PartyLean.NATIONALIST
            Ideology.DEMOCRACY -> PartyLean.CENTER
        }
        val rulingSeats = when (ideology) {
            Ideology.AUTOCRACY -> random.nextInt(58, 72)
            Ideology.DEMOCRACY -> random.nextInt(48, 56)
            Ideology.COMMUNISM -> random.nextInt(50, 62)
        }
        val remaining = 100 - rulingSeats
        val opp1Seats = (remaining * random.nextDouble(0.45, 0.62)).roundToInt().coerceIn(18, remaining - 10)
        val opp2Seats = (remaining - opp1Seats - random.nextInt(6, 12)).coerceAtLeast(8)
        val opp3Seats = (remaining - opp1Seats - opp2Seats).coerceAtLeast(5)

        val parties = listOf(
            PoliticalParty(
                id = "ruling",
                name = rulingName,
                lean = rulingLean,
                seats = rulingSeats,
                popularity = random.nextInt(42, 58).toFloat(),
                leaderName = "You",
                hostility = 0f,
                isRuling = true,
                platformTags = listOf("stability", "continuity"),
            ),
            PoliticalParty(
                id = "opp_main",
                name = listOf("National Renewal", "People's Front", "Civic Mandate", "Reform Alliance").random(random),
                lean = when (rulingLean) {
                    PartyLean.LEFT -> PartyLean.RIGHT
                    PartyLean.RIGHT, PartyLean.NATIONALIST -> PartyLean.LEFT
                    else -> PartyLean.RIGHT
                },
                seats = opp1Seats,
                popularity = random.nextInt(28, 42).toFloat(),
                leaderName = randomLeader(random),
                hostility = random.nextInt(35, 55).toFloat(),
                platformTags = listOf("change", "accountability"),
            ),
            PoliticalParty(
                id = "opp_second",
                name = listOf("Unity Coalition", "Liberty Bloc", "Green Horizon", "Labor Voice").random(random),
                lean = listOf(PartyLean.GREEN, PartyLean.CENTER, PartyLean.LEFT).random(random),
                seats = opp2Seats,
                popularity = random.nextInt(12, 24).toFloat(),
                leaderName = randomLeader(random),
                hostility = random.nextInt(20, 40).toFloat(),
                platformTags = listOf("reform", "rights"),
            ),
            PoliticalParty(
                id = "opp_fringe",
                name = listOf("Homeland First", "Radical Forum", "New Dawn").random(random),
                lean = PartyLean.NATIONALIST,
                seats = opp3Seats,
                popularity = random.nextInt(6, 16).toFloat(),
                leaderName = randomLeader(random),
                hostility = random.nextInt(45, 70).toFloat(),
                platformTags = listOf("hardline"),
            ),
        )
        return OppositionState(
            parties = normalizeSeats(parties),
            lastOppositionAction = "Chamber seated for the new term.",
        ).appendLog("Parliament opened · ruling ${rulingSeats} seats")
    }

    fun processMonth(state: GameState, random: Random = Random.Default): GameState {
        if (state.gameOver.isGameOver) return state
        var opp = state.opposition
        if (opp.parties.isEmpty()) {
            opp = seedInitial(state.legal.ideology, random)
        }

        opp = opp.copy(
            filibusterMonths = (opp.filibusterMonths - 1).coerceAtLeast(0),
            negotiateCooldownMonths = (opp.negotiateCooldownMonths - 1).coerceAtLeast(0),
            smearCooldownMonths = (opp.smearCooldownMonths - 1).coerceAtLeast(0),
            concessionCooldownMonths = (opp.concessionCooldownMonths - 1).coerceAtLeast(0),
            noConfidenceHeat = (opp.noConfidenceHeat - 1.5f).coerceAtLeast(0f),
        )

        // Popularity drift from national conditions.
        opp = opp.copy(
            parties = opp.parties.map { party ->
                var pop = party.popularity
                var hostility = party.hostility
                var seats = party.seats
                if (party.isRuling) {
                    pop += (state.vitals.approval - 50f) * 0.08f
                    if (state.netIncome < 0) pop -= 0.8f
                    if (state.production.foodShortage) pop -= 1.5f
                } else {
                    pop += (50f - state.vitals.approval) * 0.06f
                    hostility += when {
                        state.vitals.approval < 35f -> 1.8f
                        state.cabinet.scandalsThisTerm > 0 && state.month % 3 == 0 -> 1.2f
                        state.press.openScandalCount > 0 -> 1.0f
                        else -> 0.3f
                    }
                    if (state.vitals.approval >= 65f) hostility -= 1.2f
                }
                party.copy(
                    popularity = pop.coerceIn(5f, 85f),
                    hostility = hostility.coerceIn(0f, 100f),
                    seats = seats,
                    monthsAsLeader = party.monthsAsLeader + 1,
                )
            },
        )

        // Soft seat drift toward popularity (slow).
        if (state.month % 4 == 0) {
            opp = applySeatDrift(opp)
        }

        var next = state.copy(opposition = opp)
        next = runOppositionTactic(next, random)

        // Sync election challenger with main opposition leader when season starts.
        val main = next.opposition.mainOpposition
        if (main != null &&
            AgendaBuilder.monthsUntilElection(next) in 1..6 &&
            next.demographics.election.challengerName.isBlank()
        ) {
            next = next.copy(
                demographics = next.demographics.copy(
                    election = next.demographics.election.copy(
                        challengerName = main.leaderName,
                        challengerParty = main.name,
                    ),
                    oppositionMomentum = (
                        next.demographics.oppositionMomentum + main.hostility * 0.05f
                        ).coerceIn(0f, 40f),
                ),
            )
        } else if (main != null && next.demographics.election.challengerName.isNotBlank()) {
            // Keep party label aligned if challenger already spawned under old random name mid-season.
            next = next.copy(
                demographics = next.demographics.copy(
                    oppositionMomentum = (
                        next.demographics.oppositionMomentum +
                            (main.hostility - 40f) * 0.02f
                        ).coerceIn(0f, 40f),
                ),
            )
        }

        return next
    }

    /** Player: buy down hostility with the main opposition. */
    fun negotiate(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.opposition.negotiateCooldownMonths > 0) return state
        if (state.vitals.budget < NEGOTIATE_COST) return state
        val main = state.opposition.mainOpposition ?: return state

        val parties = state.opposition.parties.map { p ->
            if (p.id == main.id) {
                p.copy(
                    hostility = (p.hostility - 14f).coerceAtLeast(5f),
                    popularity = (p.popularity - 2f).coerceAtLeast(5f),
                )
            } else if (p.isRuling) {
                p.copy(popularity = (p.popularity - 1.5f).coerceAtLeast(5f))
            } else p
        }
        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - NEGOTIATE_COST,
                approval = (state.vitals.approval - 0.8f).coerceIn(0f, 100f),
            ),
            opposition = state.opposition.copy(
                parties = parties,
                negotiateCooldownMonths = 4,
                noConfidenceHeat = (state.opposition.noConfidenceHeat - 10f).coerceAtLeast(0f),
                lastPlayerCounter = "Backroom deal with ${main.leaderName}.",
            ).appendLog("Negotiated with ${main.name}"),
            demographics = state.demographics.copy(
                oppositionMomentum = (state.demographics.oppositionMomentum - 3f).coerceAtLeast(0f),
            ),
        )
    }

    /** Player: smear the opposition leader — boosts your polls, risks backlash. */
    fun smear(state: GameState, random: Random = Random.Default): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.opposition.smearCooldownMonths > 0) return state
        if (state.vitals.budget < SMEAR_COST) return state
        val main = state.opposition.mainOpposition ?: return state
        val backfire = random.nextFloat() < 0.28f

        val parties = state.opposition.parties.map { p ->
            when {
                p.id == main.id && !backfire -> p.copy(
                    popularity = (p.popularity - 5f).coerceAtLeast(5f),
                    hostility = (p.hostility + 8f).coerceAtMost(100f),
                )
                p.id == main.id && backfire -> p.copy(
                    popularity = (p.popularity + 3f).coerceAtMost(85f),
                    hostility = (p.hostility + 12f).coerceAtMost(100f),
                )
                p.isRuling && !backfire -> p.copy(popularity = (p.popularity + 3f).coerceAtMost(85f))
                p.isRuling && backfire -> p.copy(popularity = (p.popularity - 4f).coerceAtLeast(5f))
                else -> p
            }
        }

        val headline = PressHeadline(
            id = java.util.UUID.randomUUID().toString(),
            title = if (backfire) {
                "Smear against ${main.leaderName} backfires"
            } else {
                "Palace allies hit ${main.leaderName}"
            },
            lede = if (backfire) {
                "Voters punish the dirty tricks — opposition surges in sympathy."
            } else {
                "Opposition offices scramble as attack ads land."
            },
            tone = if (backfire) HeadlineTone.SCANDAL else HeadlineTone.NEGATIVE,
            year = state.year,
            month = state.month,
            outlet = PressOutlet.TABLOID,
            topic = PressTopic.ELECTION,
        )

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - SMEAR_COST,
                approval = (state.vitals.approval + if (backfire) -2.5f else 1.5f).coerceIn(0f, 100f),
            ),
            opposition = state.opposition.copy(
                parties = parties,
                smearCooldownMonths = 3,
                lastPlayerCounter = if (backfire) "Smear backfired." else "Smear campaign landed.",
            ).appendLog(if (backfire) "Smear backfired vs ${main.leaderName}" else "Smeared ${main.leaderName}"),
            press = state.press.copy(
                headlines = (listOf(headline) + state.press.headlines).take(14),
                mediaSentiment = (state.press.mediaSentiment + if (backfire) -5f else -1f)
                    .coerceIn(0f, 100f),
                credibility = (state.press.credibility - if (backfire) 4f else 1.5f)
                    .coerceIn(10f, 100f),
            ),
            demographics = state.demographics.copy(
                oppositionMomentum = (
                    state.demographics.oppositionMomentum + if (backfire) 4f else -2f
                    ).coerceIn(0f, 40f),
            ),
        )
    }

    /** Player: concede a policy point — lowers hostility, costs political capital. */
    fun concedePlatform(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.opposition.concessionCooldownMonths > 0) return state
        if (state.vitals.budget < CONCESSION_COST) return state
        val main = state.opposition.mainOpposition ?: return state

        val parties = state.opposition.parties.map { p ->
            if (p.id == main.id) {
                p.copy(
                    hostility = (p.hostility - 10f).coerceAtLeast(5f),
                    popularity = (p.popularity + 2f).coerceAtMost(85f),
                )
            } else if (p.isRuling) {
                p.copy(popularity = (p.popularity - 2.5f).coerceAtLeast(5f))
            } else p
        }
        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - CONCESSION_COST,
                approval = (state.vitals.approval - 1.2f).coerceIn(0f, 100f),
            ),
            opposition = state.opposition.copy(
                parties = parties,
                concessionCooldownMonths = 3,
                filibusterMonths = (state.opposition.filibusterMonths - 1).coerceAtLeast(0),
                lastPlayerCounter = "Policy concession to ${main.name}.",
            ).appendLog("Conceded platform point to ${main.name}"),
            demographics = state.demographics.withClamp(
                working = state.demographics.workingClass + 1.2f,
                business = state.demographics.businessElite - 1.5f,
                reasons = state.demographics.recentReasons + "Policy concession to opposition",
            ),
        )
    }

    private fun runOppositionTactic(state: GameState, random: Random): GameState {
        val main = state.opposition.mainOpposition ?: return state
        if (main.hostility < 30f && state.vitals.approval >= 55f) {
            return state.copy(
                opposition = state.opposition.copy(
                    lastOppositionAction = "${main.leaderName} keeps powder dry.",
                ),
            )
        }

        val tactic = weightedTactic(main, state, random)
        var opp = state.opposition
        var vitals = state.vitals
        var security = state.internalSecurity
        var press = state.press
        var demographics = state.demographics
        var legal = state.legal
        val actionNote: String

        when (tactic) {
            OppositionTactic.FILIBUSTER -> {
                opp = opp.copy(
                    filibusterMonths = (opp.filibusterMonths + 2).coerceAtMost(4),
                    billsBlockedThisTerm = opp.billsBlockedThisTerm + 1,
                )
                if (legal.pendingLaws.isNotEmpty()) {
                    legal = legal.copy(
                        pendingLaws = legal.pendingLaws.mapIndexed { index, pending ->
                            if (index == 0) pending.copy(ticksRemaining = pending.ticksRemaining + 1)
                            else pending
                        },
                    )
                }
                actionNote = "${main.leaderName} launches a filibuster."
            }
            OppositionTactic.STREET_PROTEST -> {
                security = security.copy(
                    instabilityScore = (security.instabilityScore + 2.5f + main.hostility * 0.02f)
                        .coerceIn(0f, 100f),
                )
                vitals = vitals.copy(
                    approval = (vitals.approval - 1.2f).coerceIn(0f, 100f),
                )
                opp = opp.copy(protestsThisTerm = opp.protestsThisTerm + 1)
                actionNote = "${main.name} organizes street protests."
            }
            OppositionTactic.MEDIA_BLITZ -> {
                press = press.copy(
                    mediaSentiment = (press.mediaSentiment - 3.5f).coerceIn(0f, 100f),
                    headlines = (
                        listOf(
                            PressHeadline(
                                id = java.util.UUID.randomUUID().toString(),
                                title = "${main.leaderName} hammers the palace on air",
                                lede = "${main.name} floods the cycle with attack messaging.",
                                tone = HeadlineTone.NEGATIVE,
                                year = state.year,
                                month = state.month,
                                outlet = PressOutlet.DIGITAL_WIRE,
                                topic = PressTopic.ELECTION,
                            ),
                        ) + press.headlines
                        ).take(14),
                )
                demographics = demographics.copy(
                    oppositionMomentum = (demographics.oppositionMomentum + 2f).coerceIn(0f, 40f),
                )
                actionNote = "${main.leaderName} runs a media blitz."
            }
            OppositionTactic.NO_CONFIDENCE -> {
                opp = opp.copy(
                    noConfidenceHeat = (opp.noConfidenceHeat + 12f + main.hostility * 0.08f)
                        .coerceIn(0f, 100f),
                )
                vitals = vitals.copy(
                    approval = (vitals.approval - 1.8f).coerceIn(0f, 100f),
                )
                actionNote = "${main.name} pushes a no-confidence narrative."
            }
            OppositionTactic.WHIP_VOTE -> {
                opp = opp.copy(
                    parties = opp.parties.map { p ->
                        if (!p.isRuling) p.copy(hostility = (p.hostility + 3f).coerceAtMost(100f))
                        else p
                    },
                    billsBlockedThisTerm = opp.billsBlockedThisTerm + 1,
                )
                actionNote = "Opposition whips votes against the government agenda."
            }
            OppositionTactic.POLICY_OFFER -> {
                opp = opp.copy(
                    parties = opp.parties.map { p ->
                        if (p.id == main.id) p.copy(hostility = (p.hostility - 4f).coerceAtLeast(5f))
                        else p
                    },
                )
                actionNote = "${main.leaderName} floats a conditional deal."
            }
        }

        // Extreme no-confidence: seat shock
        if (opp.noConfidenceHeat >= 85f && random.nextFloat() < 0.25f) {
            opp = applySeatShock(opp, rulingLoss = 3)
            opp = opp.copy(noConfidenceHeat = 55f)
            vitals = vitals.copy(approval = (vitals.approval - 3f).coerceIn(0f, 100f))
        }

        return state.copy(
            opposition = opp.copy(lastOppositionAction = actionNote).appendLog(actionNote),
            vitals = vitals,
            internalSecurity = security,
            press = press,
            demographics = demographics,
            legal = legal,
        )
    }

    private fun weightedTactic(
        main: PoliticalParty,
        state: GameState,
        random: Random,
    ): OppositionTactic {
        val weights = mutableListOf<Pair<OppositionTactic, Int>>()
        weights += OppositionTactic.MEDIA_BLITZ to 3
        weights += OppositionTactic.WHIP_VOTE to 3
        if (state.legal.pendingLaws.isNotEmpty()) weights += OppositionTactic.FILIBUSTER to 4
        if (main.hostility >= 50f) weights += OppositionTactic.STREET_PROTEST to 3
        if (main.hostility >= 60f || state.vitals.approval < 40f) {
            weights += OppositionTactic.NO_CONFIDENCE to 4
        }
        if (main.hostility < 45f) weights += OppositionTactic.POLICY_OFFER to 2
        val total = weights.sumOf { it.second }
        var roll = random.nextInt(total)
        weights.forEach { (tactic, w) ->
            roll -= w
            if (roll < 0) return tactic
        }
        return OppositionTactic.MEDIA_BLITZ
    }

    private fun applySeatDrift(opp: OppositionState): OppositionState {
        val totalPop = opp.parties.sumOf { it.popularity.toDouble() }.toFloat().coerceAtLeast(1f)
        val target = opp.parties.map { p ->
            ((p.popularity / totalPop) * 100f).roundToInt().coerceIn(5, 70)
        }
        val adjusted = opp.parties.mapIndexed { i, p ->
            val delta = (target[i] - p.seats).coerceIn(-2, 2)
            p.copy(seats = (p.seats + delta).coerceAtLeast(5))
        }
        return opp.copy(parties = normalizeSeats(adjusted))
    }

    private fun applySeatShock(opp: OppositionState, rulingLoss: Int): OppositionState {
        val parties = opp.parties.map { p ->
            when {
                p.isRuling -> p.copy(seats = (p.seats - rulingLoss).coerceAtLeast(30))
                p.id == opp.mainOpposition?.id -> p.copy(seats = p.seats + rulingLoss)
                else -> p
            }
        }
        return opp.copy(parties = normalizeSeats(parties))
            .appendLog("Seat shock: ruling bloc loses $rulingLoss seats")
    }

    private fun normalizeSeats(parties: List<PoliticalParty>): List<PoliticalParty> {
        val total = parties.sumOf { it.seats }.coerceAtLeast(1)
        if (total == 100) return parties
        val scale = 100.0 / total
        val scaled = parties.map { it.copy(seats = (it.seats * scale).roundToInt().coerceAtLeast(5)) }
        val drift = 100 - scaled.sumOf { it.seats }
        return scaled.mapIndexed { i, p ->
            if (i == 0) p.copy(seats = (p.seats + drift).coerceAtLeast(5)) else p
        }
    }

    private fun randomLeader(random: Random): String {
        val first = listOf(
            "Helena", "Marcus", "Irena", "David", "Sofia", "Julian", "Amara", "Victor",
            "Clara", "Omar", "Nadia", "Leon", "Priya", "Felix",
        )
        val last = listOf(
            "Voss", "Quill", "Sol", "Renn", "Hart", "Crowe", "Finch", "Lang",
            "Okoye", "Petrov", "Moreau", "Chen", "Silva", "Berg",
        )
        return "${first.random(random)} ${last.random(random)}"
    }
}
