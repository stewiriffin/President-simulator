package com.presidentsimulator.game.viewmodel

/**
 * Controls how quickly the in-game calendar advances each simulation month.
 */
enum class TimeSpeedMode(
    val label: String,
    val speedLabel: String,
    val intervalMs: Long?,
) {
    PAUSED("Pause", "—", null),
    NORMAL("Normal", "1×", 1_000L),
    FAST("Fast", "3×", 333L),
}
