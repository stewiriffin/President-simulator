package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

@Serializable
enum class AgendaPriority(val rank: Int, val label: String) {
    CRITICAL(0, "CRITICAL"),
    HIGH(1, "HIGH"),
    MEDIUM(2, "MEDIUM"),
    OPPORTUNITY(3, "OPPORTUNITY"),
}

@Serializable
enum class AgendaCategory {
    WAR,
    SECURITY,
    ECONOMY,
    FOOD,
    ENERGY,
    DIPLOMACY,
    GOVERNANCE,
    LAW,
    SCIENCE,
    ELECTION,
    APPROVAL,
    FISCAL,
    CRISIS,
    TRADE,
    PRESS,
    CABINET,
    OPPOSITION,
    DISASTER,
}

/**
 * A single presidential agenda item — actionable briefing entry with deep-link target.
 */
@Serializable
data class AgendaItem(
    val id: String,
    val priority: AgendaPriority,
    val category: AgendaCategory,
    val title: String,
    val detail: String,
    val recommendedAction: String,
    /** Matches [com.presidentsimulator.game.ui.navigation.GameDestination.route]. */
    val targetRoute: String,
    /** Higher = more urgent within the same priority band. */
    val severityScore: Int = 0,
)

/**
 * Monthly presidential agenda: built each tick, acknowledged to stop spam,
 * tracks which items the player jumped to for continuity and scoring.
 */
@Serializable
data class AgendaState(
    /** "year-month" key for the current agenda build. */
    val monthKey: String = "",
    val items: List<AgendaItem> = emptyList(),
    /** When set equal to [monthKey], briefing was dismissed this month. */
    val acknowledgedMonthKey: String? = null,
    /** Agenda item ids the player jumped to (persists across months). */
    val actedItemIds: List<String> = emptyList(),
    /** Items cleared since last month that the player had acted on. */
    val resolvedLastMonth: List<String> = emptyList(),
    /** Consecutive months the player addressed at least one CRITICAL item. */
    val criticalAddressedStreak: Int = 0,
    /** Chief-of-staff intro line for the briefing dialog. */
    val briefingIntro: String = "",
) {
    val needsBriefing: Boolean
        get() = monthKey.isNotEmpty() &&
            acknowledgedMonthKey != monthKey &&
            items.isNotEmpty()

    val criticalCount: Int
        get() = items.count { it.priority == AgendaPriority.CRITICAL }

    val openCriticalIds: List<String>
        get() = items.filter { it.priority == AgendaPriority.CRITICAL }.map { it.id }

    fun isActed(itemId: String): Boolean = itemId in actedItemIds
}
