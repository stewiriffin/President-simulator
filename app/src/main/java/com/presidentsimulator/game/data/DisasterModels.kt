package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
enum class DisasterType(
    val displayName: String,
    val blurb: String,
) {
    FLOOD("Flood", "Rivers breach — farms and housing take the hit."),
    EARTHQUAKE("Earthquake", "Infrastructure cracks; rescue clocks are ticking."),
    EPIDEMIC("Epidemic", "Hospitals fill; population and approval bleed."),
    INDUSTRIAL("Industrial Accident", "Factories offline; toxic fallout spreads."),
    DROUGHT("Drought", "Crops fail; food stocks and unrest collide."),
    WILDFIRE("Wildfire", "Smoke and displacement dominate the cycle."),
}

@Serializable
enum class DisasterStage {
    ALERT,
    PEAK,
    CONTAINMENT,
    RECOVERY,
}

@Serializable
enum class ResponseFocus(val displayName: String, val cost: Long) {
    EMERGENCY_FUNDS("Emergency Funds", 4_000_000_000L),
    MILITARY_AID("Military Aid", 3_000_000_000L),
    MEDICAL_SURGE("Medical Surge", 3_500_000_000L),
    REBUILD_CONTRACTS("Rebuild Contracts", 5_000_000_000L),
    PUBLIC_ADDRESS("Public Address", 1_000_000_000L),
}

@Serializable
data class ActiveDisaster(
    val id: String,
    val type: DisasterType,
    val stage: DisasterStage = DisasterStage.ALERT,
    /** 0–100 how bad it is right now. */
    val severity: Float = 40f,
    val monthsActive: Int = 1,
    val responsePoints: Float = 0f,
    val fundsSpent: Long = 0L,
    val livesSavedEstimate: Long = 0L,
    val mismanaged: Boolean = false,
    val label: String = "",
) {
    val stageLabel: String
        get() = when (stage) {
            DisasterStage.ALERT -> "Alert"
            DisasterStage.PEAK -> "Peak crisis"
            DisasterStage.CONTAINMENT -> "Containment"
            DisasterStage.RECOVERY -> "Recovery"
        }

    val needsCommand: Boolean
        get() = stage != DisasterStage.RECOVERY || severity >= 25f
}

@Serializable
data class DisasterState(
    val active: ActiveDisaster? = null,
    val responseCooldownMonths: Int = 0,
    val disastersHandled: Int = 0,
    val disastersMismanaged: Int = 0,
    val readiness: Float = 45f,
    val lastCommandNote: String = "",
    val commandLog: List<String> = emptyList(),
) {
    val hasActive: Boolean get() = active != null

    fun appendLog(line: String): DisasterState =
        copy(commandLog = (commandLog + line).takeLast(14))

    fun summaryLine(): String {
        val d = active ?: return "No active disaster · readiness ${readiness.roundToInt()}%"
        return "${d.type.displayName} · ${d.stageLabel} · severity ${d.severity.roundToInt()}"
    }
}
