package com.presidentsimulator.game.data

import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Pure press simulation: monthly headlines, narrative arcs, spin/suppress,
 * credibility, leak risk, and approval/election pull.
 */
object PressDesk {

    const val SPIN_COST = 2_000_000_000L
    const val SUPPRESS_COST = 3_500_000_000L
    const val SPIN_COOLDOWN = 2
    const val SUPPRESS_COOLDOWN = 3
    const val MAX_HEADLINES = 14
    const val MAX_ARCS = 4

    fun processMonth(state: GameState, random: Random = Random.Default): GameState {
        if (state.gameOver.isGameOver) return state
        val monthKey = "${state.year}-${state.month}"
        var press = state.press.copy(
            spinCooldownMonths = (state.press.spinCooldownMonths - 1).coerceAtLeast(0),
            suppressCooldownMonths = (state.press.suppressCooldownMonths - 1).coerceAtLeast(0),
        )

        val censorship = state.legal.isActive("censorship")
        var freedom = press.pressFreedom
        freedom += when {
            censorship -> -2.5f
            state.society.educationLevel >= 70f -> 0.6f
            else -> 0.15f
        }
        freedom = freedom.coerceIn(if (censorship) 15f else 35f, if (censorship) 55f else 95f)

        val (afterLeakPress, leakHeadlines, leakInstability) = resolveLeaks(press, state, random)
        press = afterLeakPress.copy(pressFreedom = freedom)
        press = tickNarrativeArcs(press, state, monthKey)

        val fresh = generateHeadlines(state, press, random)
        val arcHeadlines = escalateArcHeadlines(state, press, random)
        val allNew = (leakHeadlines + arcHeadlines + fresh).distinctBy { it.title }.take(4)
        press = ensureArcsForHeadlines(press, allNew, monthKey)
        val merged = (allNew + press.headlines).take(MAX_HEADLINES)

        var sentiment = press.mediaSentiment
        var credibility = press.credibility
        var scandals = press.scandalsSurvived
        allNew.forEach { h ->
            sentiment += when (h.tone) {
                HeadlineTone.POSITIVE -> 1.8f + if (h.outlet == PressOutlet.STATE_BROADCAST) 0.6f else 0f
                HeadlineTone.NEUTRAL -> 0.15f
                HeadlineTone.NEGATIVE -> -2.2f - h.outlet.hostilityBias * 0.05f
                HeadlineTone.SCANDAL -> {
                    scandals += 1
                    -4.2f - (press.leakRisk * 0.02f)
                }
            }
            if (h.tone == HeadlineTone.SCANDAL) credibility -= 1.2f
            if (h.tone == HeadlineTone.POSITIVE) credibility += 0.35f
        }

        if (!censorship && freedom >= 70f) {
            sentiment -= allNew.count {
                it.tone == HeadlineTone.NEGATIVE || it.tone == HeadlineTone.SCANDAL
            } * 0.4f
        }
        if (censorship) {
            sentiment = (sentiment + 1.2f).coerceAtMost(68f)
            credibility = (credibility - 0.4f).coerceAtLeast(20f)
        }

        sentiment += (50f - sentiment) * 0.04f
        sentiment = sentiment.coerceIn(0f, 100f)
        credibility = credibility.coerceIn(10f, 100f)

        val approvalPull = (sentiment - 50f) * 0.035f -
            allNew.count { it.tone == HeadlineTone.SCANDAL } * 0.8f
        val note = composeDeskNote(allNew, press)
        val logLine = "M${state.month}/${state.year}: ${allNew.size} stories · $note"

        var next = state.copy(
            press = press.copy(
                mediaSentiment = sentiment,
                credibility = credibility,
                headlines = merged,
                scandalsSurvived = scandals,
                lastDeskNote = note,
            ).appendLog(logLine),
            vitals = state.vitals.copy(
                approval = (state.vitals.approval + approvalPull).coerceIn(0f, 100f),
            ),
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (
                    state.internalSecurity.instabilityScore + leakInstability +
                        allNew.count { it.tone == HeadlineTone.SCANDAL } * 1.2f
                    ).coerceIn(0f, 100f),
            ),
        )

        if (AgendaBuilder.monthsUntilElection(next) in 1..6 && sentiment < 40f) {
            next = next.copy(
                demographics = next.demographics.copy(
                    oppositionMomentum = (
                        next.demographics.oppositionMomentum + (40f - sentiment) * 0.08f
                        ).coerceIn(0f, 40f),
                ),
            )
        }

        return next
    }

    fun spinHeadline(state: GameState, headlineId: String, random: Random = Random.Default): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.press.spinCooldownMonths > 0) return state
        if (state.vitals.budget < SPIN_COST) return state
        val target = state.press.headlines.find { it.id == headlineId && !it.handled } ?: return state
        if (target.tone == HeadlineTone.POSITIVE) return state

        val backfireChance = when {
            state.press.credibility < 30f -> 0.55f
            state.press.credibility < 45f -> 0.35f
            target.tone == HeadlineTone.SCANDAL -> 0.28f
            target.outlet == PressOutlet.FOREIGN_DESK -> 0.32f
            else -> 0.12f
        }
        val backfired = random.nextFloat() < backfireChance

        val updated = state.press.headlines.map { h ->
            if (h.id != headlineId) h
            else if (backfired) {
                h.copy(
                    tone = HeadlineTone.SCANDAL,
                    handled = true,
                    backfired = true,
                    title = "Spin collapses: ${h.title.removePrefix("BREAKING: ").take(40)}",
                    lede = "Clarifications contradicted on air. Credibility takes a hit.",
                )
            } else {
                h.copy(
                    tone = HeadlineTone.NEUTRAL,
                    handled = true,
                    title = "Clarified: ${h.title.removePrefix("BREAKING: ").take(42)}",
                    lede = "The administration pushed back; coverage softens overnight.",
                )
            }
        }

        val arcs = if (!backfired && target.arcId != null) {
            state.press.narrativeArcs.map { arc ->
                if (arc.id == target.arcId) arc.copy(intensity = (arc.intensity - 12f).coerceAtLeast(0f))
                else arc
            }.filter { it.intensity > 5f }
        } else {
            state.press.narrativeArcs.map { arc ->
                if (arc.id == target.arcId) arc.copy(intensity = (arc.intensity + 8f).coerceAtMost(100f))
                else arc
            }
        }

        val note = if (backfired) {
            "Spin desk backfired — story metastasized."
        } else {
            "Spin desk worked the phones on tonight's lead."
        }
        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - SPIN_COST,
                approval = (state.vitals.approval + if (backfired) -2.5f else 1.4f).coerceIn(0f, 100f),
            ),
            press = state.press.copy(
                headlines = updated,
                narrativeArcs = arcs,
                mediaSentiment = (state.press.mediaSentiment + if (backfired) -5f else 4.5f)
                    .coerceIn(0f, 100f),
                credibility = (state.press.credibility + if (backfired) -6f else 1.5f)
                    .coerceIn(10f, 100f),
                spinCooldownMonths = SPIN_COOLDOWN,
                storiesSpun = state.press.storiesSpun + 1,
                spinBackfires = state.press.spinBackfires + if (backfired) 1 else 0,
                lastDeskNote = note,
            ).appendLog(note),
            demographics = if (backfired) {
                state.demographics.withClamp(
                    academic = state.demographics.academics - 1.5f,
                    reasons = state.demographics.recentReasons + "Botched press spin",
                )
            } else {
                state.demographics
            },
        )
    }

    fun suppressHeadline(state: GameState, headlineId: String): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.press.suppressCooldownMonths > 0) return state
        if (state.vitals.budget < SUPPRESS_COST) return state
        val target = state.press.headlines.find { it.id == headlineId && !it.handled } ?: return state

        val censorship = state.legal.isActive("censorship")
        val leakBump = when {
            target.tone == HeadlineTone.SCANDAL -> 18f
            target.outlet == PressOutlet.FOREIGN_DESK -> 14f
            censorship -> 6f
            else -> 11f
        }
        val newLeak = (state.press.leakRisk + leakBump).coerceIn(0f, 100f)

        val updated = state.press.headlines.map { h ->
            if (h.id != headlineId) h
            else h.copy(
                handled = true,
                tone = HeadlineTone.NEUTRAL,
                title = "Story withheld",
                lede = if (censorship) {
                    "State media blackout holds — academics seethe."
                } else {
                    "Quiet pressure keeps the piece off the front page — for now."
                },
            )
        }

        val arcs = if (target.arcId != null) {
            state.press.narrativeArcs.map { arc ->
                if (arc.id == target.arcId) {
                    arc.copy(intensity = (arc.intensity + 5f).coerceAtMost(100f))
                } else arc
            }
        } else {
            state.press.narrativeArcs
        }

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - SUPPRESS_COST,
                approval = (state.vitals.approval - if (censorship) 0.5f else 1.5f).coerceIn(0f, 100f),
            ),
            press = state.press.copy(
                headlines = updated,
                narrativeArcs = arcs,
                mediaSentiment = (state.press.mediaSentiment + if (censorship) 6f else 2f)
                    .coerceIn(0f, 100f),
                credibility = (state.press.credibility - if (censorship) 1f else 2.5f)
                    .coerceIn(10f, 100f),
                leakRisk = newLeak,
                suppressCooldownMonths = SUPPRESS_COOLDOWN,
                storiesSuppressed = state.press.storiesSuppressed + 1,
                lastDeskNote = "A story never made print. Leak risk now ${newLeak.roundToInt()}%.",
            ).appendLog("Suppressed: ${target.title.take(48)}"),
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (
                    state.internalSecurity.instabilityScore + if (censorship) 0.5f else 2f
                    ).coerceIn(0f, 100f),
            ),
            demographics = state.demographics.withClamp(
                academic = state.demographics.academics - if (censorship) 2.5f else 1.2f,
                reasons = state.demographics.recentReasons + "Press suppression angered academics",
            ),
        )
    }

    private fun resolveLeaks(
        press: PressState,
        state: GameState,
        random: Random,
    ): Triple<PressState, List<PressHeadline>, Float> {
        if (press.leakRisk < 12f) {
            return Triple(
                press.copy(leakRisk = (press.leakRisk - 1.5f).coerceAtLeast(0f)),
                emptyList(),
                0f,
            )
        }
        val chance = (press.leakRisk / 100f).coerceIn(0.05f, 0.55f)
        if (random.nextFloat() >= chance) {
            return Triple(
                press.copy(leakRisk = (press.leakRisk - 2f).coerceAtLeast(0f)),
                emptyList(),
                0f,
            )
        }
        val monthKey = "${state.year}-${state.month}"
        val leak = PressHeadline(
            id = UUID.randomUUID().toString(),
            title = "LEAK: Withheld story resurfaces",
            lede = "Anonymous sources dump documents the palace tried to bury. Cycle ignites.",
            tone = HeadlineTone.SCANDAL,
            year = state.year,
            month = state.month,
            outlet = PressOutlet.DIGITAL_WIRE,
            topic = PressTopic.SCANDAL,
            leaked = true,
        )
        val arc = PressNarrativeArc(
            id = "leak_$monthKey",
            topic = PressTopic.SCANDAL,
            title = "Palace leak scandal",
            intensity = 45f + press.leakRisk * 0.2f,
            monthsActive = 1,
            lastTouchedMonthKey = monthKey,
        )
        val nextPress = press.copy(
            leakRisk = (press.leakRisk * 0.45f).coerceAtLeast(8f),
            suppressLeaks = press.suppressLeaks + 1,
            credibility = (press.credibility - 8f).coerceIn(10f, 100f),
            narrativeArcs = (listOf(arc) + press.narrativeArcs).take(MAX_ARCS),
            lastDeskNote = "A suppressed story leaked.",
        ).appendLog("LEAK: suppressed story returned")
        return Triple(nextPress, listOf(leak), 3.5f)
    }

    private fun tickNarrativeArcs(
        press: PressState,
        state: GameState,
        monthKey: String,
    ): PressState {
        val updated = press.narrativeArcs.map { arc ->
            val drift = when (arc.topic) {
                PressTopic.WAR -> if (state.diplomacy.activeWar != null) 4f else -8f
                PressTopic.ECONOMY -> if (state.netIncome < 0) 3f else -5f
                PressTopic.SECURITY -> if (state.internalSecurity.coupRisk >= 50f) 5f else -6f
                PressTopic.ELECTION -> if (AgendaBuilder.monthsUntilElection(state) in 1..6) 4f else -10f
                PressTopic.SCANDAL -> 2f
                else -> -3f
            }
            arc.copy(
                intensity = (arc.intensity + drift).coerceIn(0f, 100f),
                monthsActive = arc.monthsActive + 1,
                lastTouchedMonthKey = monthKey,
            )
        }.filter { it.intensity > 8f || it.monthsActive <= 2 }
            .take(MAX_ARCS)
        return press.copy(narrativeArcs = updated)
    }

    private fun escalateArcHeadlines(
        state: GameState,
        press: PressState,
        random: Random,
    ): List<PressHeadline> {
        val hot = press.hottestArc ?: return emptyList()
        if (hot.intensity < 40f || random.nextFloat() > 0.55f) return emptyList()
        val tone = when {
            hot.intensity >= 70f -> HeadlineTone.SCANDAL
            hot.intensity >= 50f -> HeadlineTone.NEGATIVE
            else -> HeadlineTone.NEUTRAL
        }
        return listOf(
            PressHeadline(
                id = UUID.randomUUID().toString(),
                title = "Follow-up: ${hot.title}",
                lede = "The ${hot.topic.name.lowercase()} story refuses to die — intensity ${hot.intensity.roundToInt()}.",
                tone = tone,
                year = state.year,
                month = state.month,
                outlet = if (tone == HeadlineTone.SCANDAL) PressOutlet.TABLOID else PressOutlet.NATIONAL_BROADSHEET,
                topic = hot.topic,
                arcId = hot.id,
            ),
        )
    }

    private fun generateHeadlines(
        state: GameState,
        press: PressState,
        random: Random,
    ): List<PressHeadline> {
        val pool = mutableListOf<PressHeadline>()
        val y = state.year
        val m = state.month
        val monthKey = "$y-$m"

        fun h(
            title: String,
            lede: String,
            tone: HeadlineTone,
            outlet: PressOutlet = PressOutlet.NATIONAL_BROADSHEET,
            topic: PressTopic = PressTopic.GENERAL,
            arcId: String? = null,
        ) = PressHeadline(
            id = UUID.randomUUID().toString(),
            title = title,
            lede = lede,
            tone = tone,
            year = y,
            month = m,
            outlet = outlet,
            topic = topic,
            arcId = arcId,
        )

        state.diplomacy.activeWar?.let { war ->
            val name = state.diplomacy.rivalById(war.targetCountryId)?.name ?: "foe"
            pool += if (war.warProgress >= 20f) {
                h(
                    "Front advances against $name",
                    "Defense ministry briefers sound cautiously upbeat.",
                    HeadlineTone.POSITIVE,
                    PressOutlet.STATE_BROADCAST,
                    PressTopic.WAR,
                )
            } else {
                h(
                    "War costs mount in $name campaign",
                    "Casualty lists and budget overruns dominate evening news.",
                    HeadlineTone.NEGATIVE,
                    PressOutlet.NATIONAL_BROADSHEET,
                    PressTopic.WAR,
                )
            }
        }

        if (state.production.foodShortage) {
            pool += h(
                "Empty shelves spark grocery protests",
                "Families queue for staples as farms fall short.",
                HeadlineTone.SCANDAL,
                PressOutlet.TABLOID,
                PressTopic.ECONOMY,
            )
        }
        if (state.production.energyShortage) {
            pool += h(
                "Blackouts hit industrial belt",
                "Factories idle; opposition blames energy policy.",
                HeadlineTone.NEGATIVE,
                PressOutlet.DIGITAL_WIRE,
                PressTopic.ECONOMY,
            )
        }
        if (state.internalSecurity.coupRisk >= 55f) {
            pool += h(
                "Rumors of restless officers",
                "Anonymous sources claim loyalty fraying in the ranks.",
                HeadlineTone.SCANDAL,
                PressOutlet.FOREIGN_DESK,
                PressTopic.SECURITY,
            )
        }
        if (state.vitals.approval >= 65f && state.netIncome > 0) {
            pool += h(
                "Polls buoy palace mood",
                "Approval and surplus give the president room to maneuver.",
                HeadlineTone.POSITIVE,
                PressOutlet.STATE_BROADCAST,
                PressTopic.SOCIETY,
            )
        }
        if (state.vitals.approval < 35f) {
            pool += h(
                "Approval freefall continues",
                "Columnists ask how long the coalition can hold.",
                HeadlineTone.NEGATIVE,
                PressOutlet.NATIONAL_BROADSHEET,
                PressTopic.SOCIETY,
            )
        }
        state.demographics.election.challengerName
            .takeIf { it.isNotBlank() && AgendaBuilder.monthsUntilElection(state) in 1..6 }
            ?.let { name ->
                pool += h(
                    "$name surges in swing districts",
                    "Opposition ads flood the airwaves ahead of ${state.nextElectionYear}.",
                    HeadlineTone.NEGATIVE,
                    PressOutlet.DIGITAL_WIRE,
                    PressTopic.ELECTION,
                )
            }
        if (state.espionage.exposureLevel >= 50f) {
            pool += h(
                "Spy scandal whispers grow louder",
                "Foreign desks smell blood after a botched operation.",
                HeadlineTone.SCANDAL,
                PressOutlet.FOREIGN_DESK,
                PressTopic.SCANDAL,
            )
        }
        if (state.governance.activeResolution != null) {
            pool += h(
                "UN vote under global spotlight",
                "Diplomats jockey as the resolution clock ticks down.",
                HeadlineTone.NEUTRAL,
                PressOutlet.FOREIGN_DESK,
                PressTopic.DIPLOMACY,
            )
        }
        if (state.netIncome > 5_000_000_000L) {
            pool += h(
                "Treasury posts strong month",
                "Markets cheer the fiscal print.",
                HeadlineTone.POSITIVE,
                PressOutlet.NATIONAL_BROADSHEET,
                PressTopic.ECONOMY,
            )
        }
        if (state.crisis.lingeringMonths > 0) {
            val label = state.crisis.label.ifBlank { "national crisis" }
            pool += h(
                "Aftermath of $label still dominates",
                "${state.crisis.lingeringMonths} month(s) of fallout keep editors hungry.",
                HeadlineTone.NEGATIVE,
                PressOutlet.TABLOID,
                PressTopic.SOCIETY,
            )
        }
        if (press.credibility < 35f && random.nextFloat() < 0.6f) {
            pool += h(
                "Palace briefings lose the room",
                "Reporters openly question official numbers.",
                HeadlineTone.NEGATIVE,
                PressOutlet.NATIONAL_BROADSHEET,
                PressTopic.SCANDAL,
            )
        }
        if (press.mediaSentiment < 35f && random.nextFloat() < 0.5f) {
            pool += h(
                "Editorial boards turn on the palace",
                "A coordinated week of critical op-eds lands hard.",
                HeadlineTone.NEGATIVE,
                PressOutlet.NATIONAL_BROADSHEET,
                PressTopic.SOCIETY,
            )
        }
        if (state.legal.isActive("censorship") && random.nextFloat() < 0.4f) {
            pool += h(
                "Foreign press condemns speech curbs",
                "Exile journalists amplify stories state media won't touch.",
                HeadlineTone.NEGATIVE,
                PressOutlet.FOREIGN_DESK,
                PressTopic.SOCIETY,
            )
        }

        if (pool.isEmpty()) {
            pool += h(
                "Quiet week in the capital",
                "No single story owns the cycle — for now.",
                HeadlineTone.NEUTRAL,
            )
        }

        return pool.shuffled(random).take(3).map { headline ->
            if (headline.tone == HeadlineTone.SCANDAL || headline.topic == PressTopic.WAR) {
                val existing = press.narrativeArcs.find { it.topic == headline.topic }
                if (existing != null) {
                    headline.copy(arcId = existing.id)
                } else {
                    headline.copy(arcId = "arc_${headline.topic}_$monthKey")
                }
            } else {
                headline
            }
        }
    }

    fun ensureArcsForHeadlines(
        press: PressState,
        headlines: List<PressHeadline>,
        monthKey: String,
    ): PressState {
        var arcs = press.narrativeArcs.toMutableList()
        headlines.forEach { h ->
            val arcId = h.arcId ?: return@forEach
            if (arcs.none { it.id == arcId }) {
                arcs += PressNarrativeArc(
                    id = arcId,
                    topic = h.topic,
                    title = h.title.take(48),
                    intensity = when (h.tone) {
                        HeadlineTone.SCANDAL -> 48f
                        HeadlineTone.NEGATIVE -> 32f
                        else -> 22f
                    },
                    monthsActive = 1,
                    lastTouchedMonthKey = monthKey,
                )
            }
        }
        return press.copy(narrativeArcs = arcs.take(MAX_ARCS))
    }

    private fun composeDeskNote(fresh: List<PressHeadline>, press: PressState): String = when {
        fresh.any { it.leaked } -> "A buried story leaked onto the wire."
        fresh.any { it.tone == HeadlineTone.SCANDAL } -> "Scandal dominates the cycle."
        fresh.count { it.tone == HeadlineTone.NEGATIVE } >= 2 -> "Hostile coverage across the board."
        fresh.any { it.tone == HeadlineTone.POSITIVE } -> "A few friendly bylines land."
        press.hottestArc != null && press.hottestArc!!.intensity >= 50f ->
            "Follow-up heat on: ${press.hottestArc!!.title}"
        else -> "Quiet news day."
    }
}
