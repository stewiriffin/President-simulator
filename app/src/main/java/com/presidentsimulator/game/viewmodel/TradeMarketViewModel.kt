package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.NationalPerkEffects
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MarketResource
import com.presidentsimulator.game.data.MarketState
import com.presidentsimulator.game.data.ProductionState
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeDeal
import com.presidentsimulator.game.data.TradeState
import com.presidentsimulator.game.data.TradeType
import java.util.UUID
import kotlin.math.roundToLong
import kotlin.random.Random

/**
 * Pure global trade and tariff simulation engine.
 * [GameViewModel] applies results through immutable [GameState.copy] updates.
 */
class TradeMarketViewModel(
    private val random: Random = Random.Default,
) {

    /**
     * Monthly trade pipeline:
     * 1. Shift commodity prices by volatility
     * 2. Settle active import/export contracts against stocks and treasury
     * 3. Collect tariffs on imports and apply approval pressure when tariffs are high
     */
    fun processTradeTick(state: GameState): GameState {
        if (state.gameOver.isGameOver) return state

        var next = updateMarketPrices(state)
        val settlement = settleActiveDeals(next)
        next = settlement.state

        val importVolume = settlement.importSpend
        val tariffRate = next.trade.tariffRate
        val tariffRevenue = (importVolume * tariffRate).toLong()

        var approval = next.vitals.approval
        if (tariffRate > HIGH_TARIFF_THRESHOLD) {
            val overage = tariffRate - HIGH_TARIFF_THRESHOLD
            approval -= overage * 20f
        }

        val tradeBalance = settlement.exportIncome - settlement.importSpend
        val tradePerk = NationalPerkEffects.tradeIncomeMultiplier(
            NationalPerkEffects.forNationId(next.playerNation.id),
        )
        val exportBonus = ((settlement.exportIncome * tradePerk) - settlement.exportIncome).toLong()
        return next.copy(
            vitals = next.vitals.copy(
                budget = next.vitals.budget + tariffRevenue + exportBonus,
                approval = approval.coerceIn(0f, 100f),
            ),
            trade = next.trade.copy(
                activeDeals = settlement.remainingDeals,
                tradeBalance = tradeBalance,
                lastTariffRevenue = tariffRevenue,
            ),
        )
    }

    /**
     * Proposes a multi-month contract with [partnerCountryId].
     * Hostile partners reject; friendly partners offer better unit prices.
     */
    fun proposeTradeDeal(
        state: GameState,
        partnerCountryId: String,
        commodity: TradeCommodity,
        amount: Long,
        type: TradeType,
    ): GameState {
        if (state.gameOver.isGameOver) return state
        if (amount <= 0L) return state
        val rival = state.diplomacy.rivalById(partnerCountryId) ?: return state
        if (rival.relationshipScore <= HOSTILE_RELATION_THRESHOLD) return state

        val quote = state.market.quote(commodity)
        val pricePerUnit = negotiatedPrice(quote.currentPrice, rival.relationshipScore, type)
        val duration = DEFAULT_DEAL_DURATION

        val deal = TradeDeal(
            dealId = UUID.randomUUID().toString(),
            partnerCountryId = partnerCountryId,
            commodity = commodity,
            amountPerTick = amount,
            pricePerUnit = pricePerUnit,
            type = type,
            durationTicks = duration,
            ticksRemaining = duration,
        )

        return state.copy(
            trade = state.trade.copy(
                activeDeals = state.trade.activeDeals + deal,
            ),
            diplomacy = state.diplomacy.updateRival(partnerCountryId) {
                it.copy(
                    relationshipScore = (it.relationshipScore + 2).coerceIn(-100, 100),
                )
            },
        )
    }

    fun cancelTradeDeal(state: GameState, dealId: String): GameState {
        if (state.gameOver.isGameOver) return state
        return state.copy(
            trade = state.trade.copy(
                activeDeals = state.trade.activeDeals.filterNot { it.dealId == dealId },
            ),
        )
    }

    fun setTariffRate(state: GameState, rate: Float): GameState {
        if (state.gameOver.isGameOver) return state
        return state.copy(
            trade = state.trade.copy(
                tariffRate = rate.coerceIn(MIN_TARIFF, MAX_TARIFF),
            ),
        )
    }

    /**
     * Instant market purchase of [amount] units at the current global price (+ tariff).
     */
    fun buyFromMarket(state: GameState, commodity: TradeCommodity, amount: Long): GameState {
        if (state.gameOver.isGameOver) return state
        if (amount <= 0L) return state
        val quote = state.market.quote(commodity)
        val baseCost = quote.currentPrice * amount
        val tariff = (baseCost * state.trade.tariffRate).toLong()
        val totalCost = baseCost + tariff
        if (state.vitals.budget < totalCost) return state

        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - totalCost),
            production = adjustStock(state.production, commodity, amount),
            trade = state.trade.copy(
                tradeBalance = state.trade.tradeBalance - baseCost,
                lastTariffRevenue = state.trade.lastTariffRevenue + tariff,
            ),
            market = state.market.updateResource(commodity) { resource ->
                resource.copy(
                    demandModifier = (resource.demandModifier + 0.01f).coerceAtMost(1.5f),
                )
            },
        )
    }

    /**
     * Instant market sale of [amount] units at the current global price.
     */
    fun sellToMarket(state: GameState, commodity: TradeCommodity, amount: Long): GameState {
        if (state.gameOver.isGameOver) return state
        if (amount <= 0L) return state
        val available = stockOf(state.production, commodity)
        if (available < amount) return state

        val quote = state.market.quote(commodity)
        val income = quote.currentPrice * amount

        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget + income),
            production = adjustStock(state.production, commodity, -amount),
            trade = state.trade.copy(
                tradeBalance = state.trade.tradeBalance + income,
            ),
            market = state.market.updateResource(commodity) { resource ->
                resource.copy(
                    demandModifier = (resource.demandModifier - 0.01f).coerceAtLeast(0.6f),
                )
            },
        )
    }

    fun forecastTariffRevenue(state: GameState, tariffRate: Float): Long {
        val projectedImports = state.trade.activeDeals
            .filter { it.type == TradeType.IMPORT }
            .sumOf { it.amountPerTick * it.pricePerUnit }
        return (projectedImports * tariffRate.coerceIn(MIN_TARIFF, MAX_TARIFF)).toLong()
    }

    fun forecastTariffApprovalPenalty(tariffRate: Float): Float {
        val rate = tariffRate.coerceIn(MIN_TARIFF, MAX_TARIFF)
        return if (rate <= HIGH_TARIFF_THRESHOLD) {
            0f
        } else {
            (rate - HIGH_TARIFF_THRESHOLD) * 20f
        }
    }

    fun negotiatedPrice(basePrice: Long, relationshipScore: Int, type: TradeType): Long {
        val relationFactor = when {
            relationshipScore >= 40 -> 0.92
            relationshipScore >= 10 -> 0.97
            relationshipScore >= -20 -> 1.0
            else -> 1.08
        }
        val typed = when (type) {
            TradeType.EXPORT -> basePrice * relationFactor
            TradeType.IMPORT -> basePrice * (2.0 - relationFactor)
        }
        return typed.roundToLong().coerceAtLeast(1L)
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private fun updateMarketPrices(state: GameState): GameState {
        val hasMacroEvent = state.diplomacy.activeWar != null
        val updated = state.market.resources.map { resource ->
            val baseVolatility = resource.commodity.marketVolatility
            val eventVolatility = if (hasMacroEvent) 0.05 else 0.0
            val volatility = baseVolatility + eventVolatility
            
            // True random walk from currentPrice, not basePrice
            val randomShift = 1.0 + random.nextDouble(-volatility.toDouble(), volatility.toDouble())
            
            // Demand slowly normalizes toward 1.0
            val newDemandModifier = if (resource.demandModifier > 1.0f) {
                (resource.demandModifier - 0.01f).coerceAtLeast(1.0f)
            } else if (resource.demandModifier < 1.0f) {
                (resource.demandModifier + 0.01f).coerceAtMost(1.0f)
            } else {
                1.0f
            }

            val nextPrice = (resource.currentPrice * randomShift * newDemandModifier.toDouble())
                .roundToLong()
                .coerceIn(
                    (resource.commodity.globalBasePrice * 0.25).toLong(),
                    (resource.commodity.globalBasePrice * 4.0).toLong()
                )

            resource.copy(
                previousPrice = resource.currentPrice,
                currentPrice = nextPrice,
                demandModifier = newDemandModifier,
            )
        }
        return state.copy(market = MarketState(resources = updated))
    }

    private data class DealSettlement(
        val state: GameState,
        val remainingDeals: List<TradeDeal>,
        val exportIncome: Long,
        val importSpend: Long,
    )

    private fun settleActiveDeals(state: GameState): DealSettlement {
        var budget = state.vitals.budget
        var production = state.production
        var exportIncome = 0L
        var importSpend = 0L
        val remaining = mutableListOf<TradeDeal>()

        state.trade.activeDeals.forEach { deal ->
            val partnerExists = state.diplomacy.rivalById(deal.partnerCountryId) != null
            if (!partnerExists) return@forEach

            when (deal.type) {
                TradeType.EXPORT -> {
                    val stock = stockOf(production, deal.commodity)
                    if (stock >= deal.amountPerTick) {
                        production = adjustStock(production, deal.commodity, -deal.amountPerTick)
                        val income = deal.amountPerTick * deal.pricePerUnit
                        budget += income
                        exportIncome += income
                        val nextTicks = deal.ticksRemaining - 1
                        if (nextTicks > 0) {
                            remaining += deal.copy(ticksRemaining = nextTicks)
                        }
                    }
                    // Insufficient surplus: contract skips this month but remains active.
                    else if (deal.ticksRemaining > 0) {
                        remaining += deal
                    }
                }
                TradeType.IMPORT -> {
                    val cost = deal.amountPerTick * deal.pricePerUnit
                    if (budget >= cost) {
                        budget -= cost
                        production = adjustStock(production, deal.commodity, deal.amountPerTick)
                        importSpend += cost
                        val nextTicks = deal.ticksRemaining - 1
                        if (nextTicks > 0) {
                            remaining += deal.copy(ticksRemaining = nextTicks)
                        }
                    } else if (deal.ticksRemaining > 0) {
                        remaining += deal
                    }
                }
            }
        }

        return DealSettlement(
            state = state.copy(
                vitals = state.vitals.copy(budget = budget),
                production = production,
            ),
            remainingDeals = remaining,
            exportIncome = exportIncome,
            importSpend = importSpend,
        )
    }

    companion object {
        const val MIN_TARIFF = 0f
        const val MAX_TARIFF = 0.50f
        const val HIGH_TARIFF_THRESHOLD = 0.25f
        const val HOSTILE_RELATION_THRESHOLD = -40
        const val DEFAULT_DEAL_DURATION = 6
        const val SPOT_BUNDLE_SIZE = 50L

        fun stockOf(production: ProductionState, commodity: TradeCommodity): Long = when (commodity) {
            TradeCommodity.OIL -> production.energy
            TradeCommodity.STEEL -> production.materials
            TradeCommodity.GRAIN -> production.food
            TradeCommodity.CONSUMER_GOODS -> production.goods
        }

        fun adjustStock(
            production: ProductionState,
            commodity: TradeCommodity,
            delta: Long,
        ): ProductionState = when (commodity) {
            TradeCommodity.OIL -> production.copy(
                energy = (production.energy + delta).coerceAtLeast(0L),
            )
            TradeCommodity.STEEL -> production.copy(
                materials = (production.materials + delta).coerceAtLeast(0L),
            )
            TradeCommodity.GRAIN -> production.copy(
                food = (production.food + delta).coerceAtLeast(0L),
            )
            TradeCommodity.CONSUMER_GOODS -> production.copy(
                goods = (production.goods + delta).coerceAtLeast(0L),
            )
        }

        fun canProposeDeal(state: GameState, partnerCountryId: String): Boolean {
            if (state.gameOver.isGameOver) return false
            val rival = state.diplomacy.rivalById(partnerCountryId) ?: return false
            return rival.relationshipScore > HOSTILE_RELATION_THRESHOLD
        }
    }
}
