package com.presidentsimulator.game.data

import java.util.UUID
import kotlin.math.roundToInt

/**
 * Pure legacy ledger: monthly score drift + chapter entries for major beats.
 */
object LegacyLedger {

    fun processMonth(before: GameState, after: GameState): GameState {
        if (after.gameOver.isGameOver && before.gameOver.isGameOver) return after
        var legacy = after.legacy
        var scores = legacy.scores

        // Track approval extremes
        val approval = after.vitals.approval
        legacy = legacy.copy(
            peakApproval = maxOf(legacy.peakApproval, approval),
            lowestApproval = minOf(legacy.lowestApproval, approval),
        )

        // Soft monthly drift from conditions
        if (after.netIncome > 2_000_000_000L) scores = scores.adjust(LegacyPillar.PROSPERITY, 1)
        if (after.netIncome < -2_000_000_000L) scores = scores.adjust(LegacyPillar.PROSPERITY, -1)
        if (after.internalSecurity.coupRisk < 25f) scores = scores.adjust(LegacyPillar.SECURITY, 1)
        if (after.internalSecurity.coupRisk >= 60f) scores = scores.adjust(LegacyPillar.SECURITY, -2)
        if (after.vitals.approval >= 60f) scores = scores.adjust(LegacyPillar.MANDATE, 1)
        if (after.vitals.approval < 35f) scores = scores.adjust(LegacyPillar.MANDATE, -2)
        if (after.society.educationLevel >= 60f && after.society.healthLevel >= 55f) {
            scores = scores.adjust(LegacyPillar.SOCIETY, 1)
        }

        val entries = mutableListOf<LegacyEntry>()

        // War outcomes via war progress collapse detection is handled elsewhere;
        // detect war start / end via before/after.
        val warBefore = before.diplomacy.activeWar
        val warAfter = after.diplomacy.activeWar
        if (warBefore == null && warAfter != null) {
            entries += entry(
                after, "War declared",
                "Opened a front — history will judge the goal.",
                LegacyPillar.SECURITY, LegacyTone.TURNING_POINT, -2,
            )
            scores = scores.adjust(LegacyPillar.DIPLOMACY, -3)
        }

        // Election win
        if (after.demographics.election.pendingNight == null &&
            before.demographics.election.pendingNight?.victory == true
        ) {
            // handled in confirm — also catch via electionsWon bump below
        }

        // Disaster resolved
        if (before.disaster.active != null && after.disaster.active == null) {
            val success = after.disaster.disastersHandled > before.disaster.disastersHandled
            entries += entry(
                after,
                if (success) "Disaster contained" else "Disaster scar",
                after.disaster.lastCommandNote.ifBlank { "Emergency closed." },
                LegacyPillar.SOCIETY,
                if (success) LegacyTone.MILESTONE else LegacyTone.STAIN,
                if (success) 4 else -5,
            )
            scores = scores.adjust(LegacyPillar.SOCIETY, if (success) 3 else -4)
            legacy = legacy.copy(
                disastersHandled = after.disaster.disastersHandled,
            )
        }

        // Law enacted
        val newLaws = after.legal.activeLawIds.toSet() - before.legal.activeLawIds.toSet()
        if (newLaws.isNotEmpty()) {
            val name = LawCatalog.byId(newLaws.first())?.name ?: newLaws.first()
            entries += entry(
                after, "Law enacted: $name",
                "Parliament put your signature on the books.",
                LegacyPillar.MANDATE, LegacyTone.MILESTONE, 2,
            )
            scores = scores.adjust(LegacyPillar.MANDATE, 2)
            legacy = legacy.copy(lawsEnacted = legacy.lawsEnacted + newLaws.size)
        }

        // Tech unlock
        val newTechs = after.research.unlockedTechIds.toSet() - before.research.unlockedTechIds.toSet()
        if (newTechs.isNotEmpty()) {
            val name = TechCatalog.byId(newTechs.first())?.name ?: newTechs.first()
            entries += entry(
                after, "Breakthrough: $name",
                "Labs delivered a lasting edge.",
                LegacyPillar.PROSPERITY, LegacyTone.TRIUMPH, 3,
            )
            scores = scores.adjust(LegacyPillar.PROSPERITY, 2)
            scores = scores.adjust(LegacyPillar.SOCIETY, 1)
        }

        // Cabinet resignation wave
        if (after.cabinet.resignationsThisTerm > before.cabinet.resignationsThisTerm) {
            entries += entry(
                after, "Cabinet fracture",
                after.cabinet.lastCabinetNote.ifBlank { "Ministers walked." },
                LegacyPillar.MANDATE, LegacyTone.STAIN, -3,
            )
            scores = scores.adjust(LegacyPillar.MANDATE, -2)
        }

        // Hostile press scandal arc peak
        if (after.press.openScandalCount >= 2 && before.press.openScandalCount < 2) {
            entries += entry(
                after, "Media firestorm",
                after.press.lastDeskNote.ifBlank { "Scandals owned the cycle." },
                LegacyPillar.MANDATE, LegacyTone.STAIN, -2,
            )
        }

        // UN resolution passed (active cleared with success is hard; track peacekeepers / embargo flips)
        if (!before.governance.nuclearEmbargoActive && after.governance.nuclearEmbargoActive) {
            entries += entry(
                after, "Nuclear embargo imposed",
                "The assembly constrained the arsenal.",
                LegacyPillar.DIPLOMACY, LegacyTone.TURNING_POINT, -1,
            )
            scores = scores.adjust(LegacyPillar.DIPLOMACY, -2)
        }

        // Food crisis chapter
        if (after.production.foodShortage && !before.production.foodShortage) {
            entries += entry(
                after, "Hunger on the streets",
                "Empty shelves become a legacy stain if left to fester.",
                LegacyPillar.PROSPERITY, LegacyTone.TRAGEDY, -4,
            )
            scores = scores.adjust(LegacyPillar.PROSPERITY, -3)
            scores = scores.adjust(LegacyPillar.SOCIETY, -2)
        }

        // Annual chapter each January
        if (after.month == 1 && before.month == 12) {
            entries += entry(
                after, "Year ${before.year} closed",
                "Approval ${approval.roundToInt()}% · treasury ${if (after.netIncome >= 0) "in surplus" else "in deficit"} · legacy ${scores.grade}.",
                LegacyPillar.MANDATE, LegacyTone.MILESTONE, 0,
            )
        }

        if (entries.isEmpty() && after.month % 6 == 0) {
            // Quiet mid-year pulse so the ledger never feels dead
            entries += entry(
                after, "Quiet stewardship",
                "No single headline — the ledger still records the calm.",
                LegacyPillar.SOCIETY, LegacyTone.MILESTONE, 1,
            )
            scores = scores.adjust(LegacyPillar.SOCIETY, 1)
        }

        val merged = (legacy.entries + entries).takeLast(40)
        val note = entries.lastOrNull()?.title ?: legacy.lastLegacyNote

        return after.copy(
            legacy = legacy.copy(
                scores = scores,
                entries = merged,
                lastLegacyNote = note,
            ),
        )
    }

    fun recordElectionVictory(state: GameState, challenger: String): GameState {
        val entry = entry(
            state, "Re-elected",
            "Defeated $challenger — the mandate renews.",
            LegacyPillar.MANDATE, LegacyTone.TRIUMPH, 8,
        )
        return state.copy(
            legacy = state.legacy.copy(
                scores = state.legacy.scores.adjust(LegacyPillar.MANDATE, 6),
                entries = (state.legacy.entries + entry).takeLast(40),
                electionsWon = state.legacy.electionsWon + 1,
                lastLegacyNote = entry.title,
            ),
        )
    }

    fun recordElectionDefeat(state: GameState, challenger: String): GameState {
        val entry = entry(
            state, "Defeated at the ballot",
            "$challenger ends the presidency.",
            LegacyPillar.MANDATE, LegacyTone.TRAGEDY, -12,
        )
        return state.copy(
            legacy = state.legacy.copy(
                scores = state.legacy.scores.adjust(LegacyPillar.MANDATE, -10),
                entries = (state.legacy.entries + entry).takeLast(40),
                lastLegacyNote = entry.title,
            ),
        )
    }

    fun recordWarOutcome(state: GameState, victory: Boolean, foe: String): GameState {
        val entry = entry(
            state,
            if (victory) "Victory over $foe" else "Defeat to $foe",
            if (victory) "The front held — a martial chapter." else "The map turned against you.",
            LegacyPillar.SECURITY,
            if (victory) LegacyTone.TRIUMPH else LegacyTone.TRAGEDY,
            if (victory) 7 else -8,
        )
        return state.copy(
            legacy = state.legacy.copy(
                scores = state.legacy.scores
                    .adjust(LegacyPillar.SECURITY, if (victory) 5 else -6)
                    .adjust(LegacyPillar.DIPLOMACY, if (victory) -1 else -3),
                entries = (state.legacy.entries + entry).takeLast(40),
                warsWon = state.legacy.warsWon + if (victory) 1 else 0,
                warsLost = state.legacy.warsLost + if (victory) 0 else 1,
                lastLegacyNote = entry.title,
            ),
        )
    }

    private fun entry(
        state: GameState,
        title: String,
        detail: String,
        pillar: LegacyPillar,
        tone: LegacyTone,
        scoreDelta: Int,
    ) = LegacyEntry(
        id = UUID.randomUUID().toString(),
        year = state.year,
        month = state.month,
        title = title,
        detail = detail,
        pillar = pillar,
        tone = tone,
        scoreDelta = scoreDelta,
    )
}
