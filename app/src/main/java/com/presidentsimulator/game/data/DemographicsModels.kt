package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/**
 * Persistent approval cohorts that drive blended national approval.
 */
@Serializable
data class DemographicsState(
    val workingClass: Float = 55f,
    val businessElite: Float = 55f,
    val military: Float = 55f,
    val academics: Float = 55f,
    val recentReasons: List<String> = emptyList(),
    /** Months until each campaign action can be used again (action name → months left). */
    val campaignCooldownMonths: Map<String, Int> = emptyMap(),
    /** Opposition momentum during election season — drags blended approval. */
    val oppositionMomentum: Float = 0f,
    /** Election-season theater: polls, challenger, pending night result. */
    val election: ElectionSeasonState = ElectionSeasonState(),
) {
    fun blendedApproval(): Float =
        (workingClass * SHARE_WORKING) +
            (businessElite * SHARE_BUSINESS) +
            (military * SHARE_MILITARY) +
            (academics * SHARE_ACADEMICS)

    fun withClamp(
        working: Float = workingClass,
        business: Float = businessElite,
        mil: Float = military,
        academic: Float = academics,
        reasons: List<String> = recentReasons,
    ): DemographicsState = copy(
        workingClass = working.coerceIn(0f, 100f),
        businessElite = business.coerceIn(0f, 100f),
        military = mil.coerceIn(0f, 100f),
        academics = academic.coerceIn(0f, 100f),
        recentReasons = reasons.takeLast(8),
    )

    companion object {
        const val SHARE_WORKING = 0.50f
        const val SHARE_BUSINESS = 0.10f
        const val SHARE_MILITARY = 0.15f
        const val SHARE_ACADEMICS = 0.25f
    }
}

@Serializable
data class ElectionPollSnapshot(
    val year: Int,
    val month: Int,
    val playerShare: Float,
    val challengerShare: Float,
    val undecided: Float,
)

@Serializable
data class ElectionNightResult(
    val victory: Boolean,
    val playerShare: Float,
    val challengerShare: Float,
    val workingShare: Float,
    val businessShare: Float,
    val militaryShare: Float,
    val academicsShare: Float,
    val economyOk: Boolean,
    val stabilityOk: Boolean,
    val oppositionPenalty: Float,
    val challengerName: String,
    val narrative: String,
)

@Serializable
data class ElectionSeasonState(
    val challengerName: String = "",
    val challengerParty: String = "",
    val pollHistory: List<ElectionPollSnapshot> = emptyList(),
    val debatesHeld: Int = 0,
    val attackAdsRun: Int = 0,
    val oppositionAttackAds: Int = 0,
    val pendingNight: ElectionNightResult? = null,
) {
    val hasPendingNight: Boolean get() = pendingNight != null

    val latestPoll: ElectionPollSnapshot?
        get() = pollHistory.lastOrNull()
}
