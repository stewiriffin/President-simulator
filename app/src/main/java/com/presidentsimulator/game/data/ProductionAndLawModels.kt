package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/**
 * Industrial resource stocks and last-tick flow accounting.
 * Power plants and mines live here; farms/factories remain on [EconomyState].
 */
@Serializable
data class ProductionState(
    val energy: Long = 2_000L,
    val food: Long = 5_000L,
    val materials: Long = 1_500L,
    val goods: Long = 800L,
    val powerPlants: Int = 20,
    val mines: Int = 15,
    val lastEnergyProduced: Long = 0L,
    val lastEnergyConsumed: Long = 0L,
    val lastFoodProduced: Long = 0L,
    val lastFoodConsumed: Long = 0L,
    val lastMaterialsProduced: Long = 0L,
    val lastMaterialsConsumed: Long = 0L,
    val lastGoodsProduced: Long = 0L,
    val lastGoodsSold: Long = 0L,
    /** Currency earned from goods sold during the last production tick. */
    val lastGoodsRevenue: Long = 0L,
    val energyShortage: Boolean = false,
    val foodShortage: Boolean = false,
) {
    fun flow(resource: ResourceType): ResourceFlow = when (resource) {
        ResourceType.ENERGY -> ResourceFlow(lastEnergyProduced, lastEnergyConsumed)
        ResourceType.FOOD -> ResourceFlow(lastFoodProduced, lastFoodConsumed)
        ResourceType.MATERIALS -> ResourceFlow(lastMaterialsProduced, lastMaterialsConsumed)
        ResourceType.GOODS -> ResourceFlow(lastGoodsProduced, lastGoodsSold)
    }
}

enum class ResourceType(val displayName: String) {
    ENERGY("Energy"),
    FOOD("Food"),
    MATERIALS("Raw Materials"),
    GOODS("Manufactured Goods"),
}

data class ResourceFlow(
    val produced: Long,
    val consumed: Long,
) {
    val surplus: Long get() = produced - consumed
    val isDeficit: Boolean get() = produced < consumed
}

@Serializable
enum class Ideology(val displayName: String) {
    DEMOCRACY("Democracy"),
    AUTOCRACY("Autocracy"),
    COMMUNISM("Communism"),
}

enum class LawCategory(val displayName: String) {
    SOCIAL("Social"),
    ECONOMIC("Economic"),
    MILITARY("Military"),
}

/**
 * An enactable national policy with activation cost, monthly upkeep,
 * and permanent modifiers while active.
 */
data class Law(
    val id: String,
    val name: String,
    val description: String,
    val category: LawCategory,
    val activationCost: Long,
    val upkeepCost: Long,
    /** Minimum approval required for parliament to pass the law. */
    val approvalThreshold: Float,
    val approvalModifier: Float = 0f,
    /** Multiplier applied to industrial output (1.0 = neutral, 1.1 = +10%). */
    val productionModifier: Float = 1f,
    val foodDemandModifier: Float = 1f,
    val energyDemandModifier: Float = 1f,
    val militaryRecruitModifier: Float = 1f,
)

@Serializable
data class LegalState(
    val ideology: Ideology = Ideology.DEMOCRACY,
    val activeLawIds: List<String> = emptyList(),
) {
    val activeLaws: List<Law>
        get() = activeLawIds.mapNotNull { LawCatalog.byId(it) }

    val totalUpkeep: Long
        get() = activeLaws.sumOf { it.upkeepCost }

    val combinedApprovalModifier: Float
        get() = activeLaws.sumOf { it.approvalModifier.toDouble() }.toFloat() +
            ideology.baseApprovalModifier

    val combinedProductionModifier: Float
        get() {
            var modifier = ideology.baseProductionModifier
            activeLaws.forEach { law ->
                modifier *= law.productionModifier
            }
            return modifier
        }

    val combinedFoodDemandModifier: Float
        get() {
            var modifier = 1f
            activeLaws.forEach { law ->
                modifier *= law.foodDemandModifier
            }
            return modifier
        }

    val combinedEnergyDemandModifier: Float
        get() {
            var modifier = 1f
            activeLaws.forEach { law ->
                modifier *= law.energyDemandModifier
            }
            return modifier
        }

    fun isActive(lawId: String): Boolean = lawId in activeLawIds
}

private val Ideology.baseApprovalModifier: Float
    get() = when (this) {
        Ideology.DEMOCRACY -> 2f
        Ideology.AUTOCRACY -> -3f
        Ideology.COMMUNISM -> 0f
    }

private val Ideology.baseProductionModifier: Float
    get() = when (this) {
        Ideology.DEMOCRACY -> 1f
        Ideology.AUTOCRACY -> 1.05f
        Ideology.COMMUNISM -> 0.95f
    }

/**
 * Master catalog of enactable laws.
 */
object LawCatalog {

    val all: List<Law> = listOf(
        Law(
            id = "universal_healthcare",
            name = "Universal Healthcare",
            description = "Guarantees medical coverage for every citizen.",
            category = LawCategory.SOCIAL,
            activationCost = 8_000_000_000L,
            upkeepCost = 1_500_000_000L,
            approvalThreshold = 45f,
            approvalModifier = 8f,
            productionModifier = 0.98f,
            foodDemandModifier = 1.02f,
        ),
        Law(
            id = "free_education",
            name = "Free Education",
            description = "State-funded schooling from primary through university.",
            category = LawCategory.SOCIAL,
            activationCost = 6_000_000_000L,
            upkeepCost = 1_200_000_000L,
            approvalThreshold = 40f,
            approvalModifier = 6f,
            productionModifier = 1.04f,
        ),
        Law(
            id = "censorship",
            name = "State Censorship",
            description = "Restricts press freedom and online speech.",
            category = LawCategory.SOCIAL,
            activationCost = 1_000_000_000L,
            upkeepCost = 200_000_000L,
            approvalThreshold = 30f,
            approvalModifier = -10f,
            productionModifier = 1.02f,
            energyDemandModifier = 0.97f,
        ),
        Law(
            id = "minimum_wage",
            name = "National Minimum Wage",
            description = "Raises baseline wages across all industries.",
            category = LawCategory.ECONOMIC,
            activationCost = 3_000_000_000L,
            upkeepCost = 800_000_000L,
            approvalThreshold = 50f,
            approvalModifier = 5f,
            productionModifier = 0.96f,
        ),
        Law(
            id = "industrial_subsidies",
            name = "Industrial Subsidies",
            description = "Direct subsidies to factories, mines, and power plants.",
            category = LawCategory.ECONOMIC,
            activationCost = 5_000_000_000L,
            upkeepCost = 2_000_000_000L,
            approvalThreshold = 35f,
            approvalModifier = -2f,
            productionModifier = 1.15f,
            energyDemandModifier = 1.05f,
        ),
        Law(
            id = "green_energy_mandate",
            name = "Green Energy Mandate",
            description = "Forces cleaner generation and efficiency standards.",
            category = LawCategory.ECONOMIC,
            activationCost = 4_000_000_000L,
            upkeepCost = 900_000_000L,
            approvalThreshold = 48f,
            approvalModifier = 4f,
            productionModifier = 0.97f,
            energyDemandModifier = 0.90f,
        ),
        Law(
            id = "conscription",
            name = "Conscription",
            description = "Mandatory military service for eligible adults.",
            category = LawCategory.MILITARY,
            activationCost = 2_000_000_000L,
            upkeepCost = 1_000_000_000L,
            approvalThreshold = 40f,
            approvalModifier = -6f,
            productionModifier = 0.94f,
            militaryRecruitModifier = 1.25f,
        ),
        Law(
            id = "defense_spending_act",
            name = "Defense Spending Act",
            description = "Locks in elevated military procurement budgets.",
            category = LawCategory.MILITARY,
            activationCost = 3_500_000_000L,
            upkeepCost = 1_800_000_000L,
            approvalThreshold = 38f,
            approvalModifier = -3f,
            productionModifier = 1.03f,
            energyDemandModifier = 1.08f,
            militaryRecruitModifier = 1.10f,
        ),
        Law(
            id = "martial_law",
            name = "Martial Law Framework",
            description = "Grants emergency powers to security forces.",
            category = LawCategory.MILITARY,
            activationCost = 1_500_000_000L,
            upkeepCost = 600_000_000L,
            approvalThreshold = 25f,
            approvalModifier = -12f,
            productionModifier = 0.92f,
            foodDemandModifier = 1.05f,
        ),
    )

    fun byId(id: String): Law? = all.find { it.id == id }

    fun byCategory(category: LawCategory): List<Law> =
        all.filter { it.category == category }
}
