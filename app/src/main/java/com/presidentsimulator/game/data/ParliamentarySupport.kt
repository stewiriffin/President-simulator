package com.presidentsimulator.game.data

/**
 * Cohort-weighted parliamentary support for laws.
 * Social bills need workers & academics; economic bills need business & workers; military bills need brass & industry.
 */
object ParliamentarySupport {
    fun score(state: GameState, law: Law): Float {
        val demographics = state.demographics
        val cohortVoice = when (law.category) {
            LawCategory.SOCIAL ->
                demographics.workingClass * 0.55f + demographics.academics * 0.45f
            LawCategory.ECONOMIC ->
                demographics.businessElite * 0.50f + demographics.workingClass * 0.50f
            LawCategory.MILITARY ->
                demographics.military * 0.60f + demographics.businessElite * 0.40f
        }
        return (cohortVoice * 0.65f + state.vitals.approval * 0.35f).coerceIn(0f, 100f)
    }

    fun passesImmediately(state: GameState, law: Law): Boolean =
        score(state, law) >= law.approvalThreshold

    fun pendingMonths(state: GameState, law: Law): Int {
        val gap = (law.approvalThreshold - score(state, law)).coerceAtLeast(0f)
        return when {
            gap <= 8f -> 3
            gap <= 18f -> 5
            else -> 7
        }
    }
}
