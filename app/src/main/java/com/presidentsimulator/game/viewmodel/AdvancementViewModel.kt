package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.ResearchState
import com.presidentsimulator.game.data.SocietyMinistry
import com.presidentsimulator.game.data.SocietyState
import com.presidentsimulator.game.data.StateReligion
import com.presidentsimulator.game.data.TechCatalog
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Pure science, technology, and society simulation engine.
 * [GameViewModel] applies results through immutable [GameState.copy] updates.
 */
class AdvancementViewModel {

    /**
     * Monthly society pipeline:
     * 1. Generate science from education, universities, and tech/religion multipliers
     * 2. Pay ministry upkeep or decay underfunded levels
     * 3. Apply health-driven demographic pressure and passive approval from culture/religion/tech
     */
    fun processSocietyTick(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state

        val society = state.society
        val research = state.research
        val techEffects = research.combinedEffects
        val religion = society.stateReligion

        val scienceGenerated = calculateScienceGenerated(state)
        val researchAfterScience = advanceActiveResearch(
            state = state,
            research = research,
            scienceGenerated = scienceGenerated,
        )

        var budget = state.vitals.budget
        var healthLevel = society.healthLevel
        var educationLevel = society.educationLevel
        var cultureScore = society.cultureScore

        val healthCost = society.healthUpkeep
        val educationCost = society.educationUpkeep
        val cultureCost = society.cultureUpkeep

        if (budget >= healthCost) {
            budget -= healthCost
            healthLevel = (healthLevel + forecastLevelDelta(society.healthFunding)).coerceIn(0f, 100f)
        } else {
            healthLevel = (healthLevel - UNDERFUNDED_DECAY).coerceIn(0f, 100f)
        }

        if (budget >= educationCost) {
            budget -= educationCost
            educationLevel = (educationLevel + forecastLevelDelta(society.educationFunding))
                .coerceIn(0f, 100f)
        } else {
            educationLevel = (educationLevel - UNDERFUNDED_DECAY).coerceIn(0f, 100f)
        }

        if (budget >= cultureCost) {
            budget -= cultureCost
            cultureScore = (cultureScore + forecastLevelDelta(society.cultureFunding))
                .coerceIn(0f, 100f)
        } else {
            cultureScore = (cultureScore - UNDERFUNDED_DECAY).coerceIn(0f, 100f)
        }

        val population = applyHealthDemographics(
            population = state.vitals.population,
            healthLevel = healthLevel,
            techGrowthBonus = techEffects.populationGrowthBonus,
        )

        return state.copy(
            vitals = state.vitals.copy(
                budget = budget,
                population = population,
            ),
            society = society.copy(
                healthLevel = healthLevel,
                educationLevel = educationLevel,
                cultureScore = cultureScore,
                lastScienceGenerated = scienceGenerated,
            ),
            research = researchAfterScience,
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (
                    state.internalSecurity.instabilityScore + religion.instabilityModifier * 0.2f
                    ).coerceIn(0f, 100f),
            ),
        )
    }

    /**
     * Begins researching [techId] when no other project is active.
     */
    fun startResearch(state: GameState, techId: String): GameState {
        if (state.gameOver.isGameOver) return state
        val tech = TechCatalog.byId(techId) ?: return state
        if (state.research.isUnlocked(techId)) return state
        if (!state.research.prerequisitesMet(tech)) return state
        if (state.research.activeTechId != null) return state
        if (techId == "nuclear_fission" && state.governance.nuclearEmbargoActive) return state

        return state.copy(
            research = state.research.copy(
                activeTechId = techId,
                researchProgress = 0L,
                extraFundingTier = 0,
            ),
        )
    }

    /**
     * Spends treasury to accelerate the active research project.
     */
    fun allocateExtraResearchFunding(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state
        val research = state.research
        val activeId = research.activeTechId ?: return state
        if (research.extraFundingTier >= ResearchState.MAX_EXTRA_FUNDING_TIER) return state
        if (state.vitals.budget < EXTRA_RESEARCH_FUNDING_COST) return state

        val tech = TechCatalog.byId(activeId) ?: return state
        val boost = (tech.scienceCost * EXTRA_FUNDING_PROGRESS_SHARE).roundToLong()
        val newProgress = research.researchProgress + boost
        val completed = newProgress >= tech.scienceCost

        return if (completed) {
            val updated = completeResearch(state, activeId, state.research.sciencePoints)
            updated.copy(
                vitals = updated.vitals.copy(
                    budget = updated.vitals.budget - EXTRA_RESEARCH_FUNDING_COST,
                ),
            )
        } else {
            state.copy(
                vitals = state.vitals.copy(
                    budget = state.vitals.budget - EXTRA_RESEARCH_FUNDING_COST,
                ),
                research = research.copy(
                    researchProgress = newProgress,
                    extraFundingTier = research.extraFundingTier + 1,
                ),
            )
        }
    }

    /**
     * Unlocks [techId] instantly when science points and prerequisites are sufficient.
     */
    fun unlockTechnology(state: GameState, techId: String): GameState {
        if (state.gameOver.isGameOver) return state
        val tech = TechCatalog.byId(techId) ?: return state
        if (state.research.isUnlocked(techId)) return state
        if (!state.research.prerequisitesMet(tech)) return state
        if (state.research.sciencePoints < tech.scienceCost) return state
        if (techId == "nuclear_fission" && state.governance.nuclearEmbargoActive) return state

        return completeResearch(
            state = state,
            techId = techId,
            newSciencePoints = (state.research.sciencePoints - tech.scienceCost).coerceAtLeast(0L),
        )
    }

    private fun advanceActiveResearch(
        state: GameState,
        research: ResearchState,
        scienceGenerated: Long,
    ): ResearchState {
        val activeId = research.activeTechId
        if (activeId == null) {
            return research.copy(sciencePoints = research.sciencePoints + scienceGenerated)
        }

        val tech = TechCatalog.byId(activeId) ?: return research.copy(activeTechId = null)
        val gained = (scienceGenerated * research.fundingMultiplier).roundToLong().coerceAtLeast(0L)
        val newProgress = research.researchProgress + gained

        return if (newProgress >= tech.scienceCost) {
            completeResearch(
                state = state,
                techId = activeId,
                newSciencePoints = (newProgress - tech.scienceCost).coerceAtLeast(0L),
            ).research
        } else {
            research.copy(researchProgress = newProgress)
        }
    }

    private fun completeResearch(
        state: GameState,
        techId: String,
        newSciencePoints: Long,
    ): GameState {
        val tech = TechCatalog.byId(techId) ?: return state
        return state.copy(
            research = state.research.copy(
                activeTechId = null,
                researchProgress = 0L,
                extraFundingTier = 0,
                sciencePoints = newSciencePoints,
                unlockedTechIds = state.research.unlockedTechIds + techId,
            ),
            vitals = state.vitals.copy(
                approval = (state.vitals.approval + tech.effect.approvalBonus * 0.25f)
                    .coerceIn(0f, 100f),
            ),
        )
    }

    /**
     * Updates funding allocation (0–1) for a social ministry.
     */
    fun adjustMinistryFunding(
        state: GameState,
        ministry: SocietyMinistry,
        newFundingLevel: Float,
    ): GameState {
        if (state.gameOver.isGameOver) return state
        val funding = newFundingLevel.coerceIn(0f, 1f)
        val society = when (ministry) {
            SocietyMinistry.HEALTH -> state.society.copy(healthFunding = funding)
            SocietyMinistry.EDUCATION -> state.society.copy(educationFunding = funding)
            SocietyMinistry.CULTURE -> state.society.copy(cultureFunding = funding)
        }
        return state.copy(society = society)
    }

    /**
     * Changes the state religion, applying an immediate approval and instability shock.
     */
    fun changeStateReligion(state: GameState, religion: StateReligion): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.society.stateReligion == religion) return state

        return state.copy(
            society = state.society.copy(stateReligion = religion),
            vitals = state.vitals.copy(
                approval = (state.vitals.approval - RELIGION_CONVERSION_APPROVAL_COST)
                    .coerceIn(0f, 100f),
            ),
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (
                    state.internalSecurity.instabilityScore + RELIGION_CONVERSION_INSTABILITY_COST
                    ).coerceIn(0f, 100f),
            ),
        )
    }

    fun buildUniversity(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state
        if (state.vitals.budget < UNIVERSITY_COST) return state
        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - UNIVERSITY_COST),
            society = state.society.copy(universities = state.society.universities + 1),
        )
    }

    fun calculateScienceGenerated(state: GameState): Long {
        val society = state.society
        val base = BASE_SCIENCE_PER_TICK
        val educationBonus = (society.educationLevel / 100f) * EDUCATION_SCIENCE_FACTOR
        val universityBonus = society.universities * SCIENCE_PER_UNIVERSITY
        val techMult = state.research.combinedEffects.scienceMultiplier
        val religionMult = society.stateReligion.scienceMultiplier
        return ((base + educationBonus + universityBonus) * techMult * religionMult)
            .roundToLong()
            .coerceAtLeast(0L)
    }

    fun forecastLevelDelta(funding: Float): Float = when {
        funding >= 0.75f -> 1.2f
        funding >= 0.55f -> 0.7f
        funding >= 0.40f -> 0.3f
        funding >= 0.25f -> 0.0f
        else -> -0.8f
    }

    fun forecastMinistryText(society: SocietyState, ministry: SocietyMinistry): String {
        val funding = when (ministry) {
            SocietyMinistry.HEALTH -> society.healthFunding
            SocietyMinistry.EDUCATION -> society.educationFunding
            SocietyMinistry.CULTURE -> society.cultureFunding
        }
        val upkeep = when (ministry) {
            SocietyMinistry.HEALTH -> society.healthUpkeep
            SocietyMinistry.EDUCATION -> society.educationUpkeep
            SocietyMinistry.CULTURE -> society.cultureUpkeep
        }
        val delta = forecastLevelDelta(funding)
        val deltaText = when {
            delta > 0f -> "will increase by ${"%.1f".format(delta)}% next month"
            delta < 0f -> "will decrease by ${"%.1f".format(-delta)}% next month"
            else -> "will remain stable next month"
        }
        return "Current Funding: ${upkeep.toBudgetString()}. Forecast: " +
            "${ministry.displayName} level $deltaText."
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private fun applyHealthDemographics(
        population: Long,
        healthLevel: Float,
        techGrowthBonus: Float,
    ): Long {
        val healthFactor = when {
            healthLevel >= 75f -> 1.003
            healthLevel >= 55f -> 1.001
            healthLevel >= 40f -> 1.0
            healthLevel >= 25f -> 0.997
            else -> 0.993
        }
        val techFactor = 1.0 + techGrowthBonus
        return (population * healthFactor * techFactor).toLong().coerceAtLeast(1_000_000L)
    }

    companion object {
        const val BASE_SCIENCE_PER_TICK = 18.0
        const val EDUCATION_SCIENCE_FACTOR = 40.0
        const val SCIENCE_PER_UNIVERSITY = 6.0
        const val UNDERFUNDED_DECAY = 1.5f
        const val RELIGION_CONVERSION_APPROVAL_COST = 8f
        const val RELIGION_CONVERSION_INSTABILITY_COST = 10f
        const val UNIVERSITY_COST = 5_000_000_000L

        fun canUnlock(state: GameState, techId: String): Boolean {
            val tech = TechCatalog.byId(techId) ?: return false
            if (state.research.isUnlocked(techId)) return false
            if (!state.research.prerequisitesMet(tech)) return false
            if (techId == "nuclear_fission" && state.governance.nuclearEmbargoActive) return false
            return state.research.sciencePoints >= tech.scienceCost
        }

        fun canStartResearch(state: GameState, techId: String): Boolean {
            val tech = TechCatalog.byId(techId) ?: return false
            if (state.research.isUnlocked(techId)) return false
            if (!state.research.prerequisitesMet(tech)) return false
            if (state.research.activeTechId != null) return false
            if (techId == "nuclear_fission" && state.governance.nuclearEmbargoActive) return false
            return true
        }

        const val EXTRA_RESEARCH_FUNDING_COST = 3_000_000_000L
        const val EXTRA_FUNDING_PROGRESS_SHARE = 0.15f
    }
}
