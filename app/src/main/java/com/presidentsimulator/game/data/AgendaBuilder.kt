package com.presidentsimulator.game.data

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pure builder: turns [GameState] into a ranked presidential agenda.
 * Shared by morning briefing and dashboard situations.
 */
object AgendaBuilder {

    const val MAX_BRIEFING_ITEMS = 6
    const val MAX_DASHBOARD_ITEMS = 4
    private const val ELECTION_SEASON_MONTHS = 6

    fun monthKey(state: GameState): String = "${state.year}-${state.month}"

    fun monthsUntilElection(state: GameState): Int {
        val monthsLeft = (state.nextElectionYear - state.year) * 12 + (12 - state.month)
        return monthsLeft.coerceAtLeast(0)
    }

    fun build(state: GameState): List<AgendaItem> {
        val items = mutableListOf<AgendaItem>()

        state.diplomacy.activeWar?.let { war ->
            val name = state.diplomacy.rivalById(war.targetCountryId)?.name ?: "enemy forces"
            val progress = war.warProgress.roundToInt()
            items += AgendaItem(
                id = "war_${war.targetCountryId}",
                priority = AgendaPriority.CRITICAL,
                category = AgendaCategory.WAR,
                title = "War with $name",
                detail = "Front at ${progress}% · ${war.warGoal.displayName}. Casualties rising — set tactics or seek settlement.",
                recommendedAction = "Open Defense",
                targetRoute = "military",
                severityScore = 100 + abs(progress),
            )
        }

        if (state.crisis.lingeringMonths > 0) {
            val label = state.crisis.label.ifBlank { "National crisis" }
            items += AgendaItem(
                id = "crisis_linger",
                priority = AgendaPriority.CRITICAL,
                category = AgendaCategory.CRISIS,
                title = "Aftermath: $label",
                detail = "${state.crisis.lingeringMonths} month(s) of lingering fallout still hitting approval and stability.",
                recommendedAction = "Review Domestic",
                targetRoute = "laws_society",
                severityScore = 90 + state.crisis.lingeringMonths * 5,
            )
        }

        if (state.crisis.pendingEventId != null) {
            items += AgendaItem(
                id = "crisis_pending",
                priority = AgendaPriority.CRITICAL,
                category = AgendaCategory.CRISIS,
                title = "Crisis awaiting decision",
                detail = "An unresolved national crisis is on your desk. Time is paused until you choose.",
                recommendedAction = "Decide Now",
                targetRoute = "dashboard",
                severityScore = 110,
            )
        }

        if (state.internalSecurity.coupRisk >= 60f) {
            items += AgendaItem(
                id = "coup_risk",
                priority = if (state.internalSecurity.coupRisk >= 80f) {
                    AgendaPriority.CRITICAL
                } else {
                    AgendaPriority.HIGH
                },
                category = AgendaCategory.SECURITY,
                title = "Coup risk elevated",
                detail = "Coup meter at ${state.internalSecurity.coupRisk.roundToInt()}%. Fund security or calm the streets before officers move.",
                recommendedAction = "Open Intelligence",
                targetRoute = "secret_service",
                severityScore = state.internalSecurity.coupRisk.roundToInt(),
            )
        } else if (state.internalSecurity.instabilityScore >= 55f) {
            items += AgendaItem(
                id = "instability",
                priority = AgendaPriority.HIGH,
                category = AgendaCategory.SECURITY,
                title = "Domestic unrest",
                detail = "Instability at ${state.internalSecurity.instabilityScore.roundToInt()}%. Protocols and funding can still turn this around.",
                recommendedAction = "Stabilize",
                targetRoute = "secret_service",
                severityScore = state.internalSecurity.instabilityScore.roundToInt(),
            )
        }

        if (state.production.foodShortage) {
            items += AgendaItem(
                id = "food_shortage",
                priority = AgendaPriority.CRITICAL,
                category = AgendaCategory.FOOD,
                title = "Food shortage",
                detail = "Granaries are empty. Build farms, import grain, or expect approval and unrest to worsen.",
                recommendedAction = "Fix Supply",
                targetRoute = "economy",
                severityScore = 85,
            )
        }

        if (state.production.energyShortage) {
            items += AgendaItem(
                id = "energy_shortage",
                priority = AgendaPriority.HIGH,
                category = AgendaCategory.ENERGY,
                title = "Energy shortage",
                detail = "Grid strain is cutting production. Power plants or oil imports are overdue.",
                recommendedAction = "Power Grid",
                targetRoute = "economy",
                severityScore = 80,
            )
        }

        if (state.vitals.approval < 35f) {
            items += AgendaItem(
                id = "approval_crash",
                priority = if (state.vitals.approval < 25f) {
                    AgendaPriority.CRITICAL
                } else {
                    AgendaPriority.HIGH
                },
                category = AgendaCategory.APPROVAL,
                title = "Approval in freefall",
                detail = "Public support at ${state.vitals.approval.roundToInt()}%. Campaign hard or cut taxes before election math turns fatal.",
                recommendedAction = "Campaign",
                targetRoute = "demographics",
                severityScore = (100 - state.vitals.approval).roundToInt(),
            )
        }

        val monthsToElection = monthsUntilElection(state)
        if (monthsToElection in 1..ELECTION_SEASON_MONTHS) {
            items += AgendaItem(
                id = "election_season",
                priority = if (monthsToElection <= 2) AgendaPriority.CRITICAL else AgendaPriority.HIGH,
                category = AgendaCategory.ELECTION,
                title = "Election in $monthsToElection month(s)",
                detail = "Opposition momentum ${state.demographics.oppositionMomentum.roundToInt()}" +
                    if (state.demographics.election.challengerName.isNotBlank()) {
                        " · vs ${state.demographics.election.challengerName}"
                    } else {
                        ""
                    } + ". Run campaigns and shore up weak cohorts now.",
                recommendedAction = "Election HQ",
                targetRoute = "demographics",
                severityScore = 70 + (7 - monthsToElection) * 5,
            )
        }

        state.governance.activeResolution?.let { res ->
            items += AgendaItem(
                id = "un_vote_${res.resolutionId}",
                priority = AgendaPriority.HIGH,
                category = AgendaCategory.GOVERNANCE,
                title = "UN vote: ${res.type.displayName}",
                detail = "${res.votingTimeRemaining} tick(s) left · ${res.votesForCount} for / ${res.votesAgainstCount} against. Bribe or lobby before the gavel.",
                recommendedAction = "UN Floor",
                targetRoute = "governance",
                severityScore = 65 + (5 - res.votingTimeRemaining).coerceAtLeast(0) * 4,
            )
        }

        if (state.legal.pendingLaws.isNotEmpty()) {
            val top = state.legal.pendingLaws.first()
            val lawName = LawCatalog.byId(top.lawId)?.name ?: top.lawId
            items += AgendaItem(
                id = "pending_law",
                priority = AgendaPriority.MEDIUM,
                category = AgendaCategory.LAW,
                title = "Parliament: $lawName",
                detail = "${state.legal.pendingLaws.size} bill(s) pending. Rush or wait — delay costs political capital.",
                recommendedAction = "Domestic Policy",
                targetRoute = "laws_society",
                severityScore = 40 + state.legal.pendingLaws.size * 5,
            )
        }

        val embargoed = state.diplomacy.rivals.filter { it.hasEmbargo }
        if (embargoed.isNotEmpty()) {
            val names = embargoed.take(2).joinToString { it.name }
            items += AgendaItem(
                id = "embargo",
                priority = AgendaPriority.HIGH,
                category = AgendaCategory.DIPLOMACY,
                title = "Under embargo",
                detail = "$names cut trade. Aid, visits, or concessions can reopen lanes.",
                recommendedAction = "Foreign Affairs",
                targetRoute = "diplomacy",
                severityScore = 55 + embargoed.size * 8,
            )
        }

        val riskyDeals = state.trade.activeDeals.filter { it.missedDeliveries > 0 }
        if (riskyDeals.isNotEmpty()) {
            items += AgendaItem(
                id = "trade_breach",
                priority = AgendaPriority.MEDIUM,
                category = AgendaCategory.TRADE,
                title = "Trade contracts at risk",
                detail = "${riskyDeals.size} deal(s) missing deliveries. Stock up or partners will void and hold a grudge.",
                recommendedAction = "Trade Desk",
                targetRoute = "economy",
                severityScore = 45 + riskyDeals.sumOf { it.missedDeliveries } * 10,
            )
        }

        if (state.espionage.exposureLevel >= 45f) {
            items += AgendaItem(
                id = "spy_exposure",
                priority = if (state.espionage.exposureLevel >= 70f) {
                    AgendaPriority.HIGH
                } else {
                    AgendaPriority.MEDIUM
                },
                category = AgendaCategory.SECURITY,
                title = "Intelligence exposure",
                detail = "Exposure at ${state.espionage.exposureLevel.roundToInt()}%. Pause aggressive ops or fund counter-intel.",
                recommendedAction = "Intel Desk",
                targetRoute = "secret_service",
                severityScore = state.espionage.exposureLevel.roundToInt(),
            )
        }

        if (state.research.activeTechId == null && state.research.sciencePoints >= 40L) {
            items += AgendaItem(
                id = "research_idle",
                priority = AgendaPriority.OPPORTUNITY,
                category = AgendaCategory.SCIENCE,
                title = "Labs idle",
                detail = "${state.research.sciencePoints} science in reserve and no active project. Queue a tech.",
                recommendedAction = "Science",
                targetRoute = "science",
                severityScore = 25,
            )
        } else {
            state.research.activeTechnology?.let { tech ->
                val pct = state.research.progressPercent()
                if (pct >= 70f) {
                    items += AgendaItem(
                        id = "research_near",
                        priority = AgendaPriority.OPPORTUNITY,
                        category = AgendaCategory.SCIENCE,
                        title = "${tech.name} nearly done",
                        detail = "${pct.roundToInt()}% complete. Extra funding could finish it this quarter.",
                        recommendedAction = "Accelerate",
                        targetRoute = "science",
                        severityScore = 30 + pct.roundToInt() / 5,
                    )
                }
            }
        }

        if (state.netIncome < -2_000_000_000L) {
            items += AgendaItem(
                id = "fiscal_bleed",
                priority = AgendaPriority.HIGH,
                category = AgendaCategory.FISCAL,
                title = "Budget bleeding",
                detail = "Net income ${formatShort(state.netIncome)}/mo. Cut upkeep, raise tariffs, or grow exports.",
                recommendedAction = "Economy",
                targetRoute = "economy",
                severityScore = 60,
            )
        } else if (state.netIncome > 3_000_000_000L && state.vitals.budget > 15_000_000_000L) {
            items += AgendaItem(
                id = "surplus",
                priority = AgendaPriority.OPPORTUNITY,
                category = AgendaCategory.FISCAL,
                title = "Treasury surplus",
                detail = "Strong cash flow — invest in industry, universities, or soft power before rivals catch up.",
                recommendedAction = "Invest",
                targetRoute = "economy",
                severityScore = 20,
            )
        }

        val hostile = state.diplomacy.rivals.count { it.relationshipScore <= -40 }
        if (hostile >= 2 && state.diplomacy.activeWar == null) {
            items += AgendaItem(
                id = "hostile_bloc",
                priority = AgendaPriority.MEDIUM,
                category = AgendaCategory.DIPLOMACY,
                title = "Hostile neighborhood",
                detail = "$hostile rivals at hostile stance. Embargoes and sanctions may follow — rebuild ties or harden defenses.",
                recommendedAction = "Diplomacy",
                targetRoute = "diplomacy",
                severityScore = 35 + hostile * 5,
            )
        }

        if (state.press.openScandalCount > 0 || state.press.hostileCount >= 2) {
            items += AgendaItem(
                id = "hostile_press",
                priority = if (state.press.openScandalCount > 0) {
                    AgendaPriority.HIGH
                } else {
                    AgendaPriority.MEDIUM
                },
                category = AgendaCategory.PRESS,
                title = if (state.press.openScandalCount > 0) "Scandal on the wires" else "Hostile press cycle",
                detail = state.press.summaryLine() +
                    ". Spin softens coverage; suppress buries it — and raises leak risk.",
                recommendedAction = "Press Desk",
                targetRoute = "dashboard",
                severityScore = 50 + state.press.hostileCount * 8 + state.press.openScandalCount * 15,
            )
        } else if (state.press.leakRisk >= 40f) {
            items += AgendaItem(
                id = "press_leak_risk",
                priority = AgendaPriority.MEDIUM,
                category = AgendaCategory.PRESS,
                title = "Leak risk elevated",
                detail = "Suppressed stories may resurface (${state.press.leakRisk.roundToInt()}% risk). Credibility ${state.press.credibility.roundToInt()}.",
                recommendedAction = "Press Desk",
                targetRoute = "dashboard",
                severityScore = 42 + state.press.leakRisk.roundToInt() / 2,
            )
        } else if (state.press.hottestArc != null && state.press.hottestArc!!.intensity >= 55f) {
            val arc = state.press.hottestArc!!
            items += AgendaItem(
                id = "press_arc",
                priority = AgendaPriority.MEDIUM,
                category = AgendaCategory.PRESS,
                title = "Story won't die: ${arc.title.take(28)}",
                detail = "Narrative intensity ${arc.intensity.roundToInt()} after ${arc.monthsActive} month(s). Spin it down or watch it escalate.",
                recommendedAction = "Press Desk",
                targetRoute = "dashboard",
                severityScore = 38 + arc.intensity.roundToInt() / 2,
            )
        }

        if (state.cabinet.vacancyCount >= 2) {
            items += AgendaItem(
                id = "cabinet_vacancies",
                priority = if (state.cabinet.vacancyCount >= 4) {
                    AgendaPriority.HIGH
                } else {
                    AgendaPriority.MEDIUM
                },
                category = AgendaCategory.CABINET,
                title = "${state.cabinet.vacancyCount} cabinet seats empty",
                detail = "Vacancies drag production, science, and cohesion. Appoint from the shortlist.",
                recommendedAction = "Cabinet",
                targetRoute = "cabinet",
                severityScore = 48 + state.cabinet.vacancyCount * 8,
            )
        }
        state.cabinet.hottestScandal?.let { hot ->
            items += AgendaItem(
                id = "cabinet_scandal_${hot.portfolio.name}",
                priority = if (hot.scandalHeat >= 70f) AgendaPriority.HIGH else AgendaPriority.MEDIUM,
                category = AgendaCategory.CABINET,
                title = "Minister under fire: ${hot.name}",
                detail = "${hot.portfolio.displayName} · scandal heat ${hot.scandalHeat.roundToInt()} · loyalty ${hot.loyalty.roundToInt()}. Fire or ride it out.",
                recommendedAction = "Cabinet",
                targetRoute = "cabinet",
                severityScore = 44 + hot.scandalHeat.roundToInt() / 2,
            )
        }
        if (state.cabinet.cohesion < 35f && state.cabinet.ministers.isNotEmpty()) {
            items += AgendaItem(
                id = "cabinet_cohesion",
                priority = AgendaPriority.HIGH,
                category = AgendaCategory.CABINET,
                title = "Cabinet cohesion collapsing",
                detail = "Cohesion ${state.cabinet.cohesion.roundToInt()}% (${state.cabinet.cohesionLabel}). Expect resignations.",
                recommendedAction = "Cabinet",
                targetRoute = "cabinet",
                severityScore = 55 + (35f - state.cabinet.cohesion).roundToInt(),
            )
        }

        state.opposition.mainOpposition?.let { main ->
            if (main.hostility >= 60f || state.opposition.noConfidenceHeat >= 45f || state.opposition.filibusterActive) {
                items += AgendaItem(
                    id = "opposition_pressure",
                    priority = if (state.opposition.noConfidenceHeat >= 70f || main.hostility >= 75f) {
                        AgendaPriority.HIGH
                    } else {
                        AgendaPriority.MEDIUM
                    },
                    category = AgendaCategory.OPPOSITION,
                    title = "${main.leaderName} presses the attack",
                    detail = state.opposition.summaryLine() +
                        ". Negotiate, smear, or concede — or watch bills stall.",
                    recommendedAction = "Parliament",
                    targetRoute = "laws_society",
                    severityScore = 46 + main.hostility.roundToInt() / 2 +
                        state.opposition.noConfidenceHeat.roundToInt() / 3,
                )
            }
            if (!state.opposition.hasMajority) {
                items += AgendaItem(
                    id = "minority_government",
                    priority = AgendaPriority.HIGH,
                    category = AgendaCategory.OPPOSITION,
                    title = "Minority government",
                    detail = "Ruling bloc holds ${state.opposition.rulingSeats}/100 seats. Every bill is a fight.",
                    recommendedAction = "Parliament",
                    targetRoute = "laws_society",
                    severityScore = 58,
                )
            }
        }

        state.disaster.active?.let { d ->
            items += AgendaItem(
                id = "disaster_${d.type.name}",
                priority = if (d.stage == DisasterStage.PEAK || d.severity >= 60f) {
                    AgendaPriority.CRITICAL
                } else {
                    AgendaPriority.HIGH
                },
                category = AgendaCategory.DISASTER,
                title = "${d.type.displayName}: ${d.stageLabel}",
                detail = "Severity ${d.severity.roundToInt()} · ${d.monthsActive} mo active · " +
                    "response pts ${d.responsePoints.roundToInt()}. Open Response Command.",
                recommendedAction = "Respond",
                targetRoute = "dashboard",
                severityScore = 70 + d.severity.roundToInt() / 2,
            )
        }

        return items
            .distinctBy { it.id }
            .sortedWith(
                compareBy<AgendaItem> { it.priority.rank }
                    .thenByDescending { it.severityScore },
            )
    }

    fun briefingItems(state: GameState): List<AgendaItem> =
        build(state).take(MAX_BRIEFING_ITEMS)

    fun dashboardItems(state: GameState): List<AgendaItem> =
        build(state).take(MAX_DASHBOARD_ITEMS)

    fun composeIntro(state: GameState, items: List<AgendaItem>): String {
        val critical = items.count { it.priority == AgendaPriority.CRITICAL }
        val nation = state.playerNation.name
        return when {
            critical >= 3 ->
                "Chief of Staff: $nation faces multiple critical threats this morning. Triage the desk before noon."
            critical >= 1 ->
                "Chief of Staff: One critical file leads the briefing. Everything else can wait."
            items.any { it.priority == AgendaPriority.HIGH } ->
                "Chief of Staff: Manageable pressure today — still, several high-priority files need your signature."
            items.any { it.priority == AgendaPriority.OPPORTUNITY } ->
                "Chief of Staff: Quiet morning. A few opportunities if you want to get ahead of the cycle."
            items.isEmpty() ->
                "Chief of Staff: No urgent files. Enjoy the calm — it rarely lasts."
            else ->
                "Chief of Staff: Standard briefing for ${GameState.monthName(state.month)} ${state.year}."
        }
    }

    /**
     * Rebuilds agenda for a new month, tracking resolved acted items and critical streak.
     */
    fun applyMonthlyAgenda(previous: AgendaState, state: GameState): AgendaState {
        val key = monthKey(state)
        val items = briefingItems(state)
        val newIds = items.map { it.id }.toSet()
        val previousIds = previous.items.map { it.id }.toSet()
        val resolved = previous.actedItemIds.filter { id ->
            id in previousIds && id !in newIds
        }
        val addressedCritical = previous.openCriticalIds.any { it in previous.actedItemIds }
        val streak = when {
            previous.openCriticalIds.isEmpty() -> previous.criticalAddressedStreak
            addressedCritical -> previous.criticalAddressedStreak + 1
            else -> 0
        }
        return AgendaState(
            monthKey = key,
            items = items,
            acknowledgedMonthKey = previous.acknowledgedMonthKey,
            actedItemIds = previous.actedItemIds.takeLast(24),
            resolvedLastMonth = resolved,
            criticalAddressedStreak = streak,
            briefingIntro = composeIntro(state, items),
        )
    }

    private fun formatShort(value: Long): String = when {
        abs(value) >= 1_000_000_000L -> "%+.1fB".format(value / 1_000_000_000.0)
        abs(value) >= 1_000_000L -> "%+.1fM".format(value / 1_000_000.0)
        else -> "%+d".format(value)
    }
}
