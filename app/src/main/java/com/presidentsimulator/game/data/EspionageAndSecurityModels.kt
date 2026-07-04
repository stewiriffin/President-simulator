package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/**
 * Domestic stability metrics and active security protocols.
 * Instability and coup risk are percentages in the 0–100 range.
 */
@Serializable
data class InternalSecurityState(
    val instabilityScore: Float = 18f,
    val coupRisk: Float = 5f,
    val activeProtocols: List<SecurityProtocol> = emptyList(),
) {
    val monthlyUpkeep: Long
        get() = activeProtocols.sumOf { it.monthlyUpkeep }

    val approvalPenalty: Float
        get() = activeProtocols.sumOf { it.approvalPenalty.toDouble() }.toFloat()

    val instabilitySuppression: Float
        get() = activeProtocols.sumOf { it.instabilityReduction.toDouble() }.toFloat()

    fun isProtocolActive(protocol: SecurityProtocol): Boolean = protocol in activeProtocols
}

@Serializable
enum class SecurityProtocol(
    val displayName: String,
    val description: String,
    val monthlyUpkeep: Long,
    /** Ongoing approval cost while active (negative). */
    val approvalPenalty: Float,
    /** Flat monthly reduction applied to instability while active. */
    val instabilityReduction: Float,
) {
    RIOT_POLICE(
        displayName = "Deploy Riot Police",
        description = "Heavy police presence in urban centers.",
        monthlyUpkeep = 1_200_000_000L,
        approvalPenalty = -1.2f,
        instabilityReduction = 4f,
    ),
    POLICE_STATE(
        displayName = "Declare Martial Law",
        description = "Expanded surveillance and detention powers.",
        monthlyUpkeep = 2_500_000_000L,
        approvalPenalty = -2.5f,
        instabilityReduction = 7f,
    ),
    CURFEW(
        displayName = "National Curfew",
        description = "Nighttime movement restrictions.",
        monthlyUpkeep = 800_000_000L,
        approvalPenalty = -1.8f,
        instabilityReduction = 3.5f,
    ),
    BORDER_CONTROL(
        displayName = "Border Control",
        description = "Tightened entry screening and checkpoints.",
        monthlyUpkeep = 1_000_000_000L,
        approvalPenalty = -0.6f,
        instabilityReduction = 2f,
    ),
    COUNTER_INTEL(
        displayName = "Fund Counter-Intelligence",
        description = "Domestic counter-espionage operations.",
        monthlyUpkeep = 1_500_000_000L,
        approvalPenalty = -0.4f,
        instabilityReduction = 2.5f,
    ),
}

/**
 * Foreign intelligence agency network and active covert missions.
 */
@Serializable
data class EspionageState(
    val spyCount: Int = 8,
    val intelligencePoints: Int = 20,
    val activeMissions: List<CovertMission> = emptyList(),
) {
    val availableSpies: Int
        get() = (spyCount - activeMissions.count { it.status == MissionStatus.ACTIVE })
            .coerceAtLeast(0)

    val activeMissionCount: Int
        get() = activeMissions.count { it.status == MissionStatus.ACTIVE }
}

@Serializable
enum class MissionType(
    val displayName: String,
    val description: String,
    val budgetCost: Long,
    val intelCost: Int,
    val durationTicks: Int,
    val baseSuccessChance: Float,
) {
    STEAL_TECHNOLOGY(
        displayName = "Steal Technology",
        description = "Exfiltrate industrial and research data.",
        budgetCost = 3_000_000_000L,
        intelCost = 8,
        durationTicks = 4,
        baseSuccessChance = 0.55f,
    ),
    SABOTAGE_ECONOMY(
        displayName = "Sabotage Economy",
        description = "Disrupt rival factories and trade.",
        budgetCost = 4_000_000_000L,
        intelCost = 10,
        durationTicks = 5,
        baseSuccessChance = 0.48f,
    ),
    FUND_REBELS(
        displayName = "Fund Rebels",
        description = "Arm and finance opposition cells.",
        budgetCost = 5_000_000_000L,
        intelCost = 12,
        durationTicks = 6,
        baseSuccessChance = 0.42f,
    ),
    ASSASSINATE_LEADER(
        displayName = "Assassinate Leader",
        description = "High-risk strike against rival leadership.",
        budgetCost = 8_000_000_000L,
        intelCost = 18,
        durationTicks = 7,
        baseSuccessChance = 0.30f,
    ),
}

@Serializable
enum class MissionStatus {
    ACTIVE,
    SUCCESS,
    FAILED,
}

@Serializable
data class CovertMission(
    val id: String,
    val missionType: MissionType,
    val targetCountryId: String,
    val successProbability: Float,
    val progressTicks: Int = 0,
    val requiredTicks: Int,
    val status: MissionStatus = MissionStatus.ACTIVE,
) {
    val progressFraction: Float
        get() = if (requiredTicks <= 0) {
            1f
        } else {
            (progressTicks.toFloat() / requiredTicks.toFloat()).coerceIn(0f, 1f)
        }
}

/**
 * Terminal outcome when domestic control collapses.
 */
@Serializable
data class GameOverState(
    val isGameOver: Boolean = false,
    val reason: String = "",
)
