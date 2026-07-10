package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/**
 * Tradable commodities on the global exchange.
 * Each maps onto a domestic production stock pool.
 */
@Serializable
enum class TradeCommodity(
    val displayName: String,
    val iconEmoji: String,
    val globalBasePrice: Long,
    val marketVolatility: Float,
) {
    OIL("Oil", "🛢", 12_000_000L, 0.08f),
    STEEL("Steel", "⚙", 8_000_000L, 0.05f),
    GRAIN("Grain", "🌾", 5_000_000L, 0.06f),
    CONSUMER_GOODS("Consumer Goods", "📦", 15_000_000L, 0.04f),
}

/**
 * Live market quote for a single commodity.
 */
@Serializable
data class MarketResource(
    val commodity: TradeCommodity,
    val currentPrice: Long,
    val previousPrice: Long,
    val demandModifier: Float = 1f,
) {
    val isTrendingUp: Boolean get() = currentPrice >= previousPrice

    val priceDelta: Long get() = currentPrice - previousPrice

    companion object {
        fun initialQuotes(): List<MarketResource> = TradeCommodity.entries.map { commodity ->
            MarketResource(
                commodity = commodity,
                currentPrice = commodity.globalBasePrice,
                previousPrice = commodity.globalBasePrice,
                demandModifier = 1f,
            )
        }
    }
}

@Serializable
enum class TradeType {
    IMPORT,
    EXPORT,
}

/**
 * Ongoing commercial contract with a rival nation.
 */
@Serializable
data class TradeDeal(
    val dealId: String,
    val partnerCountryId: String,
    val commodity: TradeCommodity,
    val amountPerTick: Long,
    val pricePerUnit: Long,
    val type: TradeType,
    val durationTicks: Int,
    val ticksRemaining: Int,
    /** Consecutive failed settlement months; voids at [MAX_MISSED_DELIVERIES]. */
    val missedDeliveries: Int = 0,
) {
    val monthlyVolume: Long get() = amountPerTick * pricePerUnit

    companion object {
        const val MAX_MISSED_DELIVERIES = 3
    }
}

/**
 * Player-controlled trade policy and active contracts.
 */
@Serializable
data class TradeState(
    val activeDeals: List<TradeDeal> = emptyList(),
    /** Net cash flow from deals last tick (exports positive, imports negative before tariffs). */
    val tradeBalance: Long = 0L,
    /** Tariff revenue collected last tick. */
    val lastTariffRevenue: Long = 0L,
    /** National import tariff rate (0–0.50). */
    val tariffRate: Float = 0.10f,
    /** Share of manufactured goods sold each month (0 = stockpile all, 1 = liquidate all). */
    val goodsExportQuota: Float = 1.0f,
) {
    val netTradeCashflow: Long get() = tradeBalance + lastTariffRevenue
}

/**
 * Global commodity exchange board.
 */
@Serializable
data class MarketState(
    val resources: List<MarketResource> = MarketResource.initialQuotes(),
) {
    fun quote(commodity: TradeCommodity): MarketResource =
        resources.find { it.commodity == commodity }
            ?: MarketResource(
                commodity = commodity,
                currentPrice = commodity.globalBasePrice,
                previousPrice = commodity.globalBasePrice,
            )

    fun updateResource(commodity: TradeCommodity, transform: (MarketResource) -> MarketResource): MarketState =
        copy(resources = resources.map { if (it.commodity == commodity) transform(it) else it })
}
