package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

/**
 * Social ministry vitals and funding allocations (0–1 funding sliders).
 * Levels and culture score are 0–100 scales.
 */
@Serializable
data class SocietyState(
    val healthLevel: Float = 52f,
    val educationLevel: Float = 48f,
    val cultureScore: Float = 44f,
    val stateReligion: StateReligion = StateReligion.SECULAR,
    val healthFunding: Float = 0.45f,
    val educationFunding: Float = 0.50f,
    val cultureFunding: Float = 0.40f,
    val universities: Int = 6,
    /** Science points generated during the last society tick. */
    val lastScienceGenerated: Long = 0L,
) {
    val healthUpkeep: Long
        get() = (HEALTH_BASE_UPKEEP * healthFunding).toLong()

    val educationUpkeep: Long
        get() = (EDUCATION_BASE_UPKEEP * educationFunding).toLong()

    val cultureUpkeep: Long
        get() = (CULTURE_BASE_UPKEEP * cultureFunding).toLong()

    val totalMinistryUpkeep: Long
        get() = healthUpkeep + educationUpkeep + cultureUpkeep

    /** Tourism income driven by culture score. */
    val tourismIncome: Long
        get() = (cultureScore * 40_000_000L).toLong()

    companion object {
        const val HEALTH_BASE_UPKEEP = 4_000_000_000L
        const val EDUCATION_BASE_UPKEEP = 3_500_000_000L
        const val CULTURE_BASE_UPKEEP = 2_500_000_000L
    }
}

@Serializable
enum class StateReligion(
    val displayName: String,
    val approvalBonus: Float,
    val instabilityModifier: Float,
    val scienceMultiplier: Float,
    val militaryMultiplier: Float,
    val productionMultiplier: Float,
) {
    SECULAR(
        displayName = "Secular Republic",
        approvalBonus = 1f,
        instabilityModifier = -0.5f,
        scienceMultiplier = 1.05f,
        militaryMultiplier = 1f,
        productionMultiplier = 1.02f,
    ),
    TRADITIONAL(
        displayName = "Traditional Faith",
        approvalBonus = 3f,
        instabilityModifier = -1.5f,
        scienceMultiplier = 0.95f,
        militaryMultiplier = 1.03f,
        productionMultiplier = 1f,
    ),
    STATE_CULT(
        displayName = "State Ideology Cult",
        approvalBonus = -2f,
        instabilityModifier = 1.5f,
        scienceMultiplier = 1f,
        militaryMultiplier = 1.08f,
        productionMultiplier = 1.04f,
    ),
    PLURALIST(
        displayName = "Religious Pluralism",
        approvalBonus = 2f,
        instabilityModifier = -1f,
        scienceMultiplier = 1.03f,
        militaryMultiplier = 0.98f,
        productionMultiplier = 1.01f,
    ),
}

/**
 * Stackable modifiers granted by unlocked technologies.
 * Multipliers default to 1 (neutral); bonuses default to 0.
 */
@Serializable
data class TechEffectModifier(
    val productionMultiplier: Float = 1f,
    val farmOutputMultiplier: Float = 1f,
    val factoryOutputMultiplier: Float = 1f,
    val militaryStrengthMultiplier: Float = 1f,
    val scienceMultiplier: Float = 1f,
    val populationGrowthBonus: Float = 0f,
    val approvalBonus: Float = 0f,
    val description: String = "",
) {
    fun combine(other: TechEffectModifier): TechEffectModifier = TechEffectModifier(
        productionMultiplier = productionMultiplier * other.productionMultiplier,
        farmOutputMultiplier = farmOutputMultiplier * other.farmOutputMultiplier,
        factoryOutputMultiplier = factoryOutputMultiplier * other.factoryOutputMultiplier,
        militaryStrengthMultiplier = militaryStrengthMultiplier * other.militaryStrengthMultiplier,
        scienceMultiplier = scienceMultiplier * other.scienceMultiplier,
        populationGrowthBonus = populationGrowthBonus + other.populationGrowthBonus,
        approvalBonus = approvalBonus + other.approvalBonus,
        description = "",
    )

    companion object {
        val NONE = TechEffectModifier()
    }
}

@Serializable
enum class TechCategory(val displayName: String) {
    ECONOMY("Economy"),
    MILITARY("Military"),
    SOCIETY("Society"),
}

@Serializable
data class Technology(
    val id: String,
    val name: String,
    val scienceCost: Long,
    val category: TechCategory,
    val effect: TechEffectModifier,
    val prerequisiteIds: List<String> = emptyList(),
)

/**
 * National research progress and unlocked tech nodes.
 */
@Serializable
data class ResearchState(
    val sciencePoints: Long = 120L,
    val unlockedTechIds: List<String> = emptyList(),
    val activeTechId: String? = null,
    val researchProgress: Long = 0L,
    val extraFundingTier: Int = 0,
) {
    val unlockedTechs: List<Technology>
        get() = unlockedTechIds.mapNotNull { TechCatalog.byId(it) }

    val combinedEffects: TechEffectModifier
        get() = unlockedTechs.fold(TechEffectModifier.NONE) { acc, tech ->
            acc.combine(tech.effect)
        }

    val activeTechnology: Technology?
        get() = activeTechId?.let { TechCatalog.byId(it) }

    val fundingMultiplier: Float
        get() = 1f + extraFundingTier * EXTRA_FUNDING_BONUS_PER_TIER

    fun isUnlocked(techId: String): Boolean = techId in unlockedTechIds

    fun prerequisitesMet(tech: Technology): Boolean =
        tech.prerequisiteIds.all { it in unlockedTechIds }

    fun progressPercent(): Float {
        val tech = activeTechnology ?: return 0f
        if (tech.scienceCost <= 0L) return 100f
        return (researchProgress.toFloat() / tech.scienceCost.toFloat() * 100f).coerceIn(0f, 100f)
    }

    fun daysRemaining(sciencePerMonth: Long): Int {
        val tech = activeTechnology ?: return 0
        val remaining = (tech.scienceCost - researchProgress).coerceAtLeast(0L)
        val monthlyRate = (sciencePerMonth * fundingMultiplier).roundToLong().coerceAtLeast(1L)
        return ((remaining * 30.0) / monthlyRate).toInt().coerceAtLeast(0)
    }

    companion object {
        const val EXTRA_FUNDING_BONUS_PER_TIER = 0.25f
        const val MAX_EXTRA_FUNDING_TIER = 3
    }
}

/**
 * Master catalog of unlockable technologies.
 */
object TechCatalog {

    val all: List<Technology> = listOf(
        Technology(
            id = "advanced_robotics",
            name = "Advanced Robotics",
            scienceCost = 260L,
            category = TechCategory.ECONOMY,
            effect = TechEffectModifier(
                factoryOutputMultiplier = 1.18f,
                productionMultiplier = 1.06f,
                description = "Increases factory output by 18% and general production by 6%.",
            ),
            prerequisiteIds = listOf("advanced_automation"),
        ),
        Technology(
            id = "advanced_automation",
            name = "Advanced Automation",
            scienceCost = 200L,
            category = TechCategory.ECONOMY,
            effect = TechEffectModifier(
                factoryOutputMultiplier = 1.15f,
                productionMultiplier = 1.05f,
                description = "Increases factory output by 15% and general production by 5%.",
            ),
        ),
        Technology(
            id = "precision_agriculture",
            name = "Precision Agriculture",
            scienceCost = 160L,
            category = TechCategory.ECONOMY,
            effect = TechEffectModifier(
                farmOutputMultiplier = 1.20f,
                description = "Increases farm output by 20%.",
            ),
        ),
        Technology(
            id = "green_grid",
            name = "Smart Energy Grid",
            scienceCost = 220L,
            category = TechCategory.ECONOMY,
            effect = TechEffectModifier(
                productionMultiplier = 1.08f,
                description = "Increases general production by 8%.",
            ),
            prerequisiteIds = listOf("advanced_automation"),
        ),
        Technology(
            id = "nuclear_fission",
            name = "Nuclear Fission",
            scienceCost = 350L,
            category = TechCategory.MILITARY,
            effect = TechEffectModifier(
                militaryStrengthMultiplier = 1.12f,
                productionMultiplier = 1.04f,
                description = "Increases military strength by 12% and production by 4%.",
            ),
        ),
        Technology(
            id = "drone_warfare",
            name = "Drone Warfare",
            scienceCost = 280L,
            category = TechCategory.MILITARY,
            effect = TechEffectModifier(
                militaryStrengthMultiplier = 1.10f,
                description = "Increases military strength by 10%.",
            ),
        ),
        Technology(
            id = "cyber_command",
            name = "Cyber Command",
            scienceCost = 300L,
            category = TechCategory.MILITARY,
            effect = TechEffectModifier(
                militaryStrengthMultiplier = 1.08f,
                scienceMultiplier = 1.05f,
                description = "Increases military strength by 8% and science output by 5%.",
            ),
            prerequisiteIds = listOf("drone_warfare"),
        ),
        Technology(
            id = "genomics",
            name = "Genomics",
            scienceCost = 240L,
            category = TechCategory.SOCIETY,
            effect = TechEffectModifier(
                populationGrowthBonus = 0.004f,
                approvalBonus = 2f,
                description = "Boosts population growth and grants +2 approval.",
            ),
        ),
        Technology(
            id = "mass_education",
            name = "Mass Education Systems",
            scienceCost = 180L,
            category = TechCategory.SOCIETY,
            effect = TechEffectModifier(
                scienceMultiplier = 1.15f,
                approvalBonus = 1.5f,
                description = "Increases science generation by 15% and approval by 1.5.",
            ),
        ),
        Technology(
            id = "cultural_broadcast",
            name = "Cultural Broadcast Network",
            scienceCost = 150L,
            category = TechCategory.SOCIETY,
            effect = TechEffectModifier(
                approvalBonus = 3f,
                productionMultiplier = 1.02f,
                description = "Grants +3 approval and +2% production.",
            ),
        ),
        Technology(
            id = "fusion_pilot",
            name = "Fusion Pilot Plant",
            scienceCost = 400L,
            category = TechCategory.ECONOMY,
            effect = TechEffectModifier(
                productionMultiplier = 1.12f,
                factoryOutputMultiplier = 1.08f,
                description = "Increases general production by 12% and factory output by 8%.",
            ),
            prerequisiteIds = listOf("green_grid", "nuclear_fission"),
        ),
        Technology(
            id = "stealth_composites",
            name = "Stealth Composites",
            scienceCost = 320L,
            category = TechCategory.MILITARY,
            effect = TechEffectModifier(
                militaryStrengthMultiplier = 1.14f,
                description = "Increases military strength by 14%.",
            ),
            prerequisiteIds = listOf("drone_warfare"),
        ),
        Technology(
            id = "universal_broadband",
            name = "Universal Broadband",
            scienceCost = 210L,
            category = TechCategory.SOCIETY,
            effect = TechEffectModifier(
                scienceMultiplier = 1.10f,
                approvalBonus = 2f,
                productionMultiplier = 1.03f,
                description = "Increases science by 10%, production by 3%, and grants +2 approval.",
            ),
            prerequisiteIds = listOf("mass_education"),
        ),
        Technology(
            id = "orbital_logistics",
            name = "Orbital Logistics",
            scienceCost = 380L,
            category = TechCategory.ECONOMY,
            effect = TechEffectModifier(
                productionMultiplier = 1.09f,
                factoryOutputMultiplier = 1.06f,
                description = "Increases general production by 9% and factory output by 6%.",
            ),
            prerequisiteIds = listOf("green_grid"),
        ),
        Technology(
            id = "civil_defense_net",
            name = "Civil Defense Network",
            scienceCost = 260L,
            category = TechCategory.MILITARY,
            effect = TechEffectModifier(
                militaryStrengthMultiplier = 1.07f,
                approvalBonus = 1f,
                description = "Increases military strength by 7% and grants +1 approval.",
            ),
            prerequisiteIds = listOf("drone_warfare"),
        ),
    )

    fun byId(id: String): Technology? = all.find { it.id == id }

    fun byCategory(category: TechCategory): List<Technology> =
        all.filter { it.category == category }
}

enum class SocietyMinistry(val displayName: String) {
    HEALTH("Health"),
    EDUCATION("Education"),
    CULTURE("Culture"),
}
