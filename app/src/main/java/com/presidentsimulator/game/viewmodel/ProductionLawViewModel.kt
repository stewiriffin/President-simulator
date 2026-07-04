package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.data.Law
import com.presidentsimulator.game.data.LawCatalog
import com.presidentsimulator.game.data.ProductionState
import kotlin.math.roundToLong

/**
 * Pure industrial and legal simulation engine.
 * [GameViewModel] applies results through immutable [GameState.copy] updates.
 */
class ProductionLawViewModel {

    /**
     * Monthly production pipeline:
     * 1. Energy balance (shortage applies a severe output penalty)
     * 2. Food balance (deficit cuts approval and starves population)
     * 3. Materials → Goods via factories, goods sold for revenue
     */
    fun processProductionTick(state: GameState): GameState {
        val legal = state.legal
        val techEffects = state.research.combinedEffects
        val productionMod = state.effectiveProductionMultiplier
        val energyDemandMod = legal.combinedEnergyDemandModifier
        val foodDemandMod = legal.combinedFoodDemandModifier
        val farmMod = techEffects.farmOutputMultiplier
        val factoryMod = techEffects.factoryOutputMultiplier

        val economy = state.economy
        val production = state.production

        val energyProduced = (production.powerPlants * ENERGY_PER_PLANT * productionMod)
            .roundToLong()
            .coerceAtLeast(0L)

        val energyConsumed = (
            (
                economy.factories * ENERGY_PER_FACTORY +
                    economy.farms * ENERGY_PER_FARM +
                    economy.housing * ENERGY_PER_HOUSING +
                    production.mines * ENERGY_PER_MINE +
                    production.powerPlants * ENERGY_PER_PLANT_IDLE
                ) * energyDemandMod
            ).roundToLong()
            .coerceAtLeast(0L)

        val energyShortage = energyProduced + production.energy < energyConsumed
        val shortagePenalty = if (energyShortage) ENERGY_SHORTAGE_PENALTY else 1f
        val effectiveMod = productionMod * shortagePenalty

        val foodProduced = (economy.farms * FOOD_PER_FARM * effectiveMod * farmMod)
            .roundToLong()
            .coerceAtLeast(0L)
        val foodConsumed = (
            (state.vitals.population / PEOPLE_PER_FOOD_UNIT).toFloat() * foodDemandMod
            ).roundToLong()
            .coerceAtLeast(0L)

        val materialsProduced = (production.mines * MATERIALS_PER_MINE * effectiveMod)
            .roundToLong()
            .coerceAtLeast(0L)

        val factoryDemand = (economy.factories * MATERIALS_PER_FACTORY * effectiveMod * factoryMod)
            .roundToLong()
            .coerceAtLeast(0L)
        val materialsAvailable = production.materials + materialsProduced
        val materialsConsumed = minOf(factoryDemand, materialsAvailable)

        val goodsCapacity = (economy.factories * GOODS_PER_FACTORY * effectiveMod * factoryMod)
            .roundToLong()
            .coerceAtLeast(0L)
        val goodsFromMaterials = if (MATERIALS_PER_FACTORY <= 0) {
            0L
        } else {
            (materialsConsumed.toDouble() / MATERIALS_PER_FACTORY * GOODS_PER_FACTORY).roundToLong()
        }
        val goodsProduced = minOf(goodsCapacity, goodsFromMaterials)

        val goodsAvailable = production.goods + goodsProduced
        val goodsSold = goodsAvailable
        val goodsRevenue = goodsSold * GOODS_SALE_PRICE

        val nextEnergy = (production.energy + energyProduced - energyConsumed).coerceAtLeast(0L)
        val nextFood = (production.food + foodProduced - foodConsumed).coerceAtLeast(0L)
        val nextMaterials = (materialsAvailable - materialsConsumed).coerceAtLeast(0L)
        val nextGoods = 0L

        val foodShortage = foodProduced + production.food < foodConsumed
        var approval = state.vitals.approval + legal.combinedApprovalModifier * 0.05f
        var population = state.vitals.population

        if (foodShortage) {
            val deficitRatio = if (foodConsumed <= 0L) {
                0f
            } else {
                ((foodConsumed - foodProduced - production.food).toFloat() / foodConsumed.toFloat())
                    .coerceIn(0f, 1f)
            }
            approval -= 8f + deficitRatio * 12f
            population = (population * (1.0 - 0.004 - deficitRatio * 0.01))
                .toLong()
                .coerceAtLeast(1_000_000L)
        }

        if (energyShortage) {
            approval -= 3f
        }

        val updatedProduction = production.copy(
            energy = nextEnergy,
            food = nextFood,
            materials = nextMaterials,
            goods = nextGoods,
            lastEnergyProduced = energyProduced,
            lastEnergyConsumed = energyConsumed,
            lastFoodProduced = foodProduced,
            lastFoodConsumed = foodConsumed,
            lastMaterialsProduced = materialsProduced,
            lastMaterialsConsumed = materialsConsumed,
            lastGoodsProduced = goodsProduced,
            lastGoodsSold = goodsSold,
            lastGoodsRevenue = goodsRevenue,
            energyShortage = energyShortage,
            foodShortage = foodShortage,
        )

        return state.copy(
            vitals = state.vitals.copy(
                approval = approval.coerceIn(0f, 100f),
                population = population,
            ),
            production = updatedProduction,
        )
    }

    /**
     * Enacts [lawId] if the treasury can pay and approval clears the parliamentary threshold.
     */
    fun enactLaw(state: GameState, lawId: String): GameState {
        val law = LawCatalog.byId(lawId) ?: return state
        if (state.legal.isActive(lawId)) return state
        if (state.vitals.budget < law.activationCost) return state
        if (state.vitals.approval < law.approvalThreshold) return state

        val legal = state.legal.copy(
            activeLawIds = state.legal.activeLawIds + lawId,
        )

        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - law.activationCost,
                approval = (state.vitals.approval + law.approvalModifier * 0.25f)
                    .coerceIn(0f, 100f),
            ),
            legal = legal,
        )
    }

    /**
     * Removes [lawId] from the active set, reversing its ongoing influence.
     */
    fun repealLaw(state: GameState, lawId: String): GameState {
        val law = LawCatalog.byId(lawId) ?: return state
        if (!state.legal.isActive(lawId)) return state

        val legal = state.legal.copy(
            activeLawIds = state.legal.activeLawIds.filterNot { it == lawId },
        )

        return state.copy(
            vitals = state.vitals.copy(
                approval = (state.vitals.approval - law.approvalModifier * 0.15f)
                    .coerceIn(0f, 100f),
            ),
            legal = legal,
        )
    }

    fun buildPowerPlant(state: GameState, amount: Int): GameState =
        buildProductionInfrastructure(state, InfrastructureType.POWER_PLANT, amount)

    fun buildMine(state: GameState, amount: Int): GameState =
        buildProductionInfrastructure(state, InfrastructureType.MINE, amount)

    private fun buildProductionInfrastructure(
        state: GameState,
        type: InfrastructureType,
        amount: Int,
    ): GameState {
        if (amount <= 0) return state
        val cost = type.unitCost * amount
        if (state.vitals.budget < cost) return state

        val production = when (type) {
            InfrastructureType.POWER_PLANT -> state.production.copy(
                powerPlants = state.production.powerPlants + amount,
            )
            InfrastructureType.MINE -> state.production.copy(
                mines = state.production.mines + amount,
            )
            else -> return state
        }

        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - cost),
            production = production,
        )
    }

    companion object {
        const val ENERGY_PER_PLANT = 120L
        const val ENERGY_PER_PLANT_IDLE = 5L
        const val ENERGY_PER_FACTORY = 18L
        const val ENERGY_PER_FARM = 6L
        const val ENERGY_PER_HOUSING = 4L
        const val ENERGY_PER_MINE = 10L
        const val ENERGY_SHORTAGE_PENALTY = 0.30f

        const val FOOD_PER_FARM = 180L
        const val PEOPLE_PER_FOOD_UNIT = 8_000L

        const val MATERIALS_PER_MINE = 90L
        const val MATERIALS_PER_FACTORY = 40L
        const val GOODS_PER_FACTORY = 55L
        const val GOODS_SALE_PRICE = 4_000_000L

        fun canEnact(state: GameState, law: Law): Boolean =
            !state.legal.isActive(law.id) &&
                state.vitals.budget >= law.activationCost &&
                state.vitals.approval >= law.approvalThreshold
    }
}

fun Long.toResourceString(): String = when {
    this >= 1_000_000L -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000L -> "%.1fK".format(this / 1_000.0)
    else -> toString()
}
