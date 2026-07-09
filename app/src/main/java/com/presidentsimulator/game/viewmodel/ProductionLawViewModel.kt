package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.Ideology
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.data.Law
import com.presidentsimulator.game.data.LawCatalog
import com.presidentsimulator.game.data.ProductionState
import com.presidentsimulator.game.data.NationalPerkEffects
import com.presidentsimulator.game.data.ParliamentarySupport
import com.presidentsimulator.game.data.PendingLaw
import com.presidentsimulator.game.data.SectorInvestment
import com.presidentsimulator.game.data.awardSectorXp
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
        val nationPerk = NationalPerkEffects.forNationId(state.playerNation.id)
        val productionMod = state.effectiveProductionMultiplier
        val energyDemandMod = legal.combinedEnergyDemandModifier
        val foodDemandMod = legal.combinedFoodDemandModifier
        val farmMod = techEffects.farmOutputMultiplier * NationalPerkEffects.farmOutputMultiplier(nationPerk)
        val factoryMod = techEffects.factoryOutputMultiplier
        val materialsMod = NationalPerkEffects.materialsOutputMultiplier(nationPerk)

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

        val materialsProduced = (production.mines * MATERIALS_PER_MINE * effectiveMod * materialsMod)
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
        val exportQuota = state.trade.goodsExportQuota.coerceIn(0f, 1f)
        val goodsSold = (goodsAvailable * exportQuota).roundToLong().coerceIn(0L, goodsAvailable)
        val goodsStockpiled = goodsAvailable - goodsSold
        val stockpileDecay = if (exportQuota < 1f && goodsStockpiled > 0L) {
            (goodsStockpiled * STOCKPILE_DECAY_RATE).roundToLong().coerceAtLeast(0L)
        } else {
            0L
        }
        val goodsRevenue = goodsSold * GOODS_SALE_PRICE

        val foodBuffer = NationalPerkEffects.foodSecurityBuffer(nationPerk)
        val nextEnergy = (production.energy + energyProduced - energyConsumed).coerceAtLeast(0L)
        val nextFood = (production.food + foodProduced - foodConsumed).coerceAtLeast(0L)
        val nextMaterials = (materialsAvailable - materialsConsumed).coerceAtLeast(0L)
        val nextGoods = (goodsStockpiled - stockpileDecay).coerceAtLeast(0L)

        val foodShortage = foodProduced + production.food + foodBuffer < foodConsumed
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
     * Enacts [lawId] if the treasury can pay. If approval clears the parliamentary threshold,
     * it passes instantly. Otherwise, it is delayed by 3 months.
     */
    fun setIdeology(state: GameState, ideology: Ideology): GameState {
        if (state.legal.ideology == ideology) return state
        val cost = IDEOLOGY_SHIFT_COST
        if (state.vitals.budget < cost) return state
        val approvalHit = when (ideology) {
            Ideology.DEMOCRACY -> 1.5f
            Ideology.AUTOCRACY -> -6f
            Ideology.COMMUNISM -> -3f
        }
        return state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - cost,
                approval = (state.vitals.approval + approvalHit).coerceIn(0f, 100f),
            ),
            legal = state.legal.copy(ideology = ideology),
            demographics = state.demographics.withClamp(
                working = state.demographics.workingClass + when (ideology) {
                    Ideology.COMMUNISM -> 4f
                    Ideology.DEMOCRACY -> 1f
                    Ideology.AUTOCRACY -> -2f
                },
                business = state.demographics.businessElite + when (ideology) {
                    Ideology.AUTOCRACY -> 2f
                    Ideology.COMMUNISM -> -5f
                    Ideology.DEMOCRACY -> 1f
                },
                mil = state.demographics.military + when (ideology) {
                    Ideology.AUTOCRACY -> 3f
                    else -> 0f
                },
                academic = state.demographics.academics + when (ideology) {
                    Ideology.DEMOCRACY -> 2f
                    Ideology.AUTOCRACY -> -4f
                    Ideology.COMMUNISM -> 1f
                },
                reasons = state.demographics.recentReasons +
                    "National ideology shifted to ${ideology.displayName}",
            ),
        )
    }

    fun enactLaw(state: GameState, lawId: String): GameState {
        val law = LawCatalog.byId(lawId) ?: return state
        if (state.legal.isActive(lawId)) return state
        if (state.legal.pendingLaws.any { it.lawId == lawId }) return state
        if (state.vitals.budget < law.activationCost) return state

        // Pay activation cost upfront
        var nextState = state.copy(
            vitals = state.vitals.copy(
                budget = state.vitals.budget - law.activationCost
            )
        )

        if (!ParliamentarySupport.passesImmediately(state, law)) {
            val months = ParliamentarySupport.pendingMonths(state, law)
            val legal = nextState.legal.copy(
                pendingLaws = nextState.legal.pendingLaws + PendingLaw(lawId, true, months),
            )
            return nextState.copy(legal = legal)
        }

        val legal = nextState.legal.copy(
            activeLawIds = nextState.legal.activeLawIds + lawId,
        )

        return nextState.copy(
            vitals = nextState.vitals.copy(
                approval = (nextState.vitals.approval + law.approvalModifier * 0.25f)
                    .coerceIn(0f, 100f),
            ),
            legal = legal,
        )
    }

    /**
     * Removes [lawId] from the active set. If approval is low, it is delayed by 3 months.
     */
    fun repealLaw(state: GameState, lawId: String): GameState {
        val law = LawCatalog.byId(lawId) ?: return state
        if (!state.legal.isActive(lawId)) return state
        if (state.legal.pendingLaws.any { it.lawId == lawId }) return state

        if (!ParliamentarySupport.passesImmediately(state, law)) {
            val months = ParliamentarySupport.pendingMonths(state, law)
            val legal = state.legal.copy(
                pendingLaws = state.legal.pendingLaws + PendingLaw(lawId, false, months),
            )
            return state.copy(legal = legal)
        }

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

    /** Drop a queued bill. Partial refund only for pending enactments. */
    fun cancelPendingLaw(state: GameState, lawId: String): GameState {
        val pending = state.legal.pendingLaws.find { it.lawId == lawId } ?: return state
        val law = LawCatalog.byId(lawId)
        val refund = if (pending.enabling && law != null) law.activationCost / 2 else 0L
        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget + refund),
            legal = state.legal.copy(
                pendingLaws = state.legal.pendingLaws.filterNot { it.lawId == lawId },
            ),
        )
    }

    /** Spend budget to force a pending bill to resolve this month. */
    fun rushPendingLaw(state: GameState, lawId: String): GameState {
        val pending = state.legal.pendingLaws.find { it.lawId == lawId } ?: return state
        if (state.vitals.budget < RUSH_LAW_COST) return state
        val paid = state.copy(vitals = state.vitals.copy(budget = state.vitals.budget - RUSH_LAW_COST))
        return resolvePendingLawNow(paid, pending.copy(ticksRemaining = 0))
    }

    private fun resolvePendingLawNow(state: GameState, pending: PendingLaw): GameState {
        val law = LawCatalog.byId(pending.lawId) ?: return state.copy(
            legal = state.legal.copy(pendingLaws = state.legal.pendingLaws.filterNot { it.lawId == pending.lawId }),
        )
        val without = state.legal.pendingLaws.filterNot { it.lawId == pending.lawId }
        return if (pending.enabling) {
            state.copy(
                vitals = state.vitals.copy(
                    approval = (state.vitals.approval + law.approvalModifier * 0.25f).coerceIn(0f, 100f),
                ),
                legal = state.legal.copy(
                    pendingLaws = without,
                    activeLawIds = state.legal.activeLawIds + pending.lawId,
                ),
            )
        } else {
            state.copy(
                vitals = state.vitals.copy(
                    approval = (state.vitals.approval - law.approvalModifier * 0.15f).coerceIn(0f, 100f),
                ),
                legal = state.legal.copy(
                    pendingLaws = without,
                    activeLawIds = state.legal.activeLawIds.filterNot { it == pending.lawId },
                ),
            )
        }
    }

    /**
     * Processes pending laws, decrementing timers and enacting/repealing when 0.
     */
    fun processLawsTick(state: GameState): GameState {
        if (state.legal.pendingLaws.isEmpty()) return state

        var nextState = state
        val remainingPending = mutableListOf<PendingLaw>()

        state.legal.pendingLaws.forEach { pending ->
            val law = LawCatalog.byId(pending.lawId)
            val nextTicks = pending.ticksRemaining - 1
            if (nextTicks <= 0 && law != null && !ParliamentarySupport.passesImmediately(nextState, law)) {
                remainingPending.add(pending.copy(ticksRemaining = 2))
                return@forEach
            }
            if (nextTicks <= 0) {
                // Enact or repeal
                val law = LawCatalog.byId(pending.lawId) ?: return@forEach
                if (pending.enabling) {
                    val legal = nextState.legal.copy(
                        activeLawIds = nextState.legal.activeLawIds + pending.lawId
                    )
                    nextState = nextState.copy(
                        vitals = nextState.vitals.copy(
                            approval = (nextState.vitals.approval + law.approvalModifier * 0.25f)
                                .coerceIn(0f, 100f),
                        ),
                        legal = legal
                    )
                } else {
                    val legal = nextState.legal.copy(
                        activeLawIds = nextState.legal.activeLawIds.filterNot { it == pending.lawId }
                    )
                    nextState = nextState.copy(
                        vitals = nextState.vitals.copy(
                            approval = (nextState.vitals.approval - law.approvalModifier * 0.15f)
                                .coerceIn(0f, 100f),
                        ),
                        legal = legal
                    )
                }
            } else {
                remainingPending += pending.copy(ticksRemaining = nextTicks)
            }
        }

        return nextState.copy(
            legal = nextState.legal.copy(pendingLaws = remainingPending)
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

        val sector = SectorInvestment.sectorForInfrastructure(type) ?: return state
        return state.awardSectorXp(sector, SectorInvestment.XP_PER_BUILD * amount).copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - cost),
            production = production,
        )
    }

    companion object {
        const val IDEOLOGY_SHIFT_COST = 8_000_000_000L
        const val RUSH_LAW_COST = 4_000_000_000L
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
        const val STOCKPILE_DECAY_RATE = 0.03f

        fun canEnact(state: GameState, law: Law): Boolean =
            !state.legal.isActive(law.id) &&
                state.legal.pendingLaws.none { it.lawId == law.id } &&
                state.vitals.budget >= law.activationCost
    }
}

fun Long.toResourceString(): String = when {
    this >= 1_000_000L -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000L -> "%.1fK".format(this / 1_000.0)
    else -> toString()
}
