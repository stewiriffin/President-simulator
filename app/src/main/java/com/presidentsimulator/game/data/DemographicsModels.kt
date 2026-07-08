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
