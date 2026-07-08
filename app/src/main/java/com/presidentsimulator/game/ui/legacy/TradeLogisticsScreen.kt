package com.presidentsimulator.game.ui.legacy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MarketResource
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeDeal
import com.presidentsimulator.game.data.TradeType
import com.presidentsimulator.game.ui.theme.InfoBlue
import com.presidentsimulator.game.ui.theme.ProfitGreen
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.TradeMarketViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

/**
 * Ministry of Commerce: commodity exchange, active contracts, and tariff policy.
 */
@Composable
fun TradeLogisticsScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var draftTariff by remember(state.trade.tariffRate) {
        mutableFloatStateOf(state.trade.tariffRate)
    }
    var expandedPartnerId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Ministry of Commerce",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Trade Terminal — global markets, contracts, and tariffs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        TariffControllerCard(
            state = state,
            draftTariff = draftTariff,
            forecastRevenue = viewModel.forecastTariffRevenue(draftTariff),
            forecastPenalty = viewModel.forecastTariffApprovalPenalty(draftTariff),
            onTariffChange = { draftTariff = it },
            onTariffCommit = { viewModel.setTariffRate(draftTariff) },
        )

        Text(
            text = "Global Commodity Exchange",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        state.market.resources.forEach { quote ->
            CommodityExchangeRow(
                state = state,
                quote = quote,
                onBuy = {
                    viewModel.buyFromMarket(quote.commodity, TradeMarketViewModel.SPOT_BUNDLE_SIZE)
                },
                onSell = {
                    viewModel.sellToMarket(quote.commodity, TradeMarketViewModel.SPOT_BUNDLE_SIZE)
                },
            )
        }

        Text(
            text = "Active Agreements Ledger",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (state.trade.activeDeals.isEmpty()) {
            Text(
                text = "No active contracts. Negotiate deals with non-hostile partners below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.trade.activeDeals.forEach { deal ->
                ActiveDealCard(
                    state = state,
                    deal = deal,
                    onTerminate = { viewModel.cancelTradeDeal(deal.dealId) },
                )
            }
        }

        Text(
            text = "Negotiate New Contracts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        state.diplomacy.rivals.forEach { rival ->
            PartnerDealCard(
                state = state,
                rival = rival,
                expanded = expandedPartnerId == rival.id,
                onToggle = {
                    expandedPartnerId = if (expandedPartnerId == rival.id) null else rival.id
                },
                canPropose = TradeMarketViewModel.canProposeDeal(state, rival.id),
                priceFor = { commodity, type ->
                    viewModel.negotiatedDealPrice(rival.id, commodity, type)
                },
                onPropose = { commodity, type, amount ->
                    viewModel.proposeTradeDeal(rival.id, commodity, amount, type)
                },
            )
        }
    }
}

@Composable
private fun TariffControllerCard(
    state: GameState,
    draftTariff: Float,
    forecastRevenue: Long,
    forecastPenalty: Float,
    onTariffChange: (Float) -> Unit,
    onTariffCommit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tariff Controller",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            BalanceRow("Trade balance (last tick)", state.trade.tradeBalance)
            BalanceRow("Tariff revenue (last tick)", state.trade.lastTariffRevenue)
            BalanceRow("Net trade cashflow", state.trade.netTradeCashflow)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "National tariff rate: ${(draftTariff * 100f).roundToInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Slider(
                value = draftTariff,
                onValueChange = onTariffChange,
                onValueChangeFinished = onTariffCommit,
                valueRange = TradeMarketViewModel.MIN_TARIFF..TradeMarketViewModel.MAX_TARIFF,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Forecast revenue: ${forecastRevenue.toBudgetString()}/mo from active imports",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = if (forecastPenalty <= 0f) {
                    "Approval impact: none (tariffs within tolerance)"
                } else {
                    "Approval impact: -${"%.1f".format(forecastPenalty)}% per month at this rate"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (forecastPenalty > 0f) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun BalanceRow(label: String, value: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value.toBudgetString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (value >= 0L) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.tertiary
            },
        )
    }
}

@Composable
private fun CommodityExchangeRow(
    state: GameState,
    quote: MarketResource,
    onBuy: () -> Unit,
    onSell: () -> Unit,
) {
    val stock = TradeMarketViewModel.stockOf(state.production, quote.commodity)
    val bundle = TradeMarketViewModel.SPOT_BUNDLE_SIZE
    val buyCost = quote.currentPrice * bundle
    val buyTotal = buyCost + (buyCost * state.trade.tariffRate).toLong()
    val canBuy = state.vitals.budget >= buyTotal
    val canSell = stock >= bundle
    val trendColor = if (quote.isTrendingUp) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${quote.commodity.iconEmoji} ${quote.commodity.displayName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Domestic stock: $stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = quote.currentPrice.toBudgetString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (quote.isTrendingUp) {
                            "▲ ${quote.priceDelta.toBudgetString()}"
                        } else {
                            "▼ ${quote.priceDelta.toBudgetString()}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = trendColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onBuy,
                    enabled = canBuy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Buy $bundle")
                }
                OutlinedButton(
                    onClick = onSell,
                    enabled = canSell,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Sell $bundle")
                }
            }
        }
    }
}

@Composable
private fun ActiveDealCard(
    state: GameState,
    deal: TradeDeal,
    onTerminate: () -> Unit,
) {
    val partnerName = state.diplomacy.rivalById(deal.partnerCountryId)?.name
        ?: deal.partnerCountryId
    val isImport = deal.type == TradeType.IMPORT
    val flowColor = if (isImport) ProfitGreen else InfoBlue
    val flowLabel = if (isImport) "⬇ IMPORT" else "⬆ EXPORT"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = partnerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = flowLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = flowColor,
                )
            }
            Text(
                text = "${deal.commodity.iconEmoji} ${deal.commodity.displayName} · " +
                    "${deal.amountPerTick}/mo @ ${deal.pricePerUnit.toBudgetString()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Volume: ${deal.monthlyVolume.toBudgetString()}/mo · " +
                    "${deal.ticksRemaining} months remaining",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onTerminate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                ),
            ) {
                Text("Terminate Contract")
            }
        }
    }
}

@Composable
private fun PartnerDealCard(
    state: GameState,
    rival: RivalNation,
    expanded: Boolean,
    onToggle: () -> Unit,
    canPropose: Boolean,
    priceFor: (TradeCommodity, TradeType) -> Long,
    onPropose: (TradeCommodity, TradeType, Long) -> Unit,
) {
    var selectedCommodity by remember { mutableStateOf(TradeCommodity.GRAIN) }
    var selectedType by remember { mutableStateOf(TradeType.IMPORT) }
    var amount by remember { mutableStateOf(100L) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${rival.flagEmoji} ${rival.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (canPropose) {
                            "Relations: ${rival.relationshipScore}"
                        } else {
                            "Hostile — deals rejected"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = onToggle, enabled = canPropose) {
                    Text(if (expanded) "Hide" else "Propose Deal")
                }
            }

            AnimatedVisibility(visible = expanded && canPropose) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Commodity", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TradeCommodity.entries.forEach { commodity ->
                            FilterChip(
                                selected = selectedCommodity == commodity,
                                onClick = { selectedCommodity = commodity },
                                label = { Text(commodity.displayName) },
                            )
                        }
                    }
                    Text("Direction", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedType == TradeType.IMPORT,
                            onClick = { selectedType = TradeType.IMPORT },
                            label = { Text("Import") },
                        )
                        FilterChip(
                            selected = selectedType == TradeType.EXPORT,
                            onClick = { selectedType = TradeType.EXPORT },
                            label = { Text("Export") },
                        )
                    }
                    Text("Monthly amount", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(50L, 100L, 250L).forEach { option ->
                            FilterChip(
                                selected = amount == option,
                                onClick = { amount = option },
                                label = { Text("$option") },
                            )
                        }
                    }
                    val unitPrice = priceFor(selectedCommodity, selectedType)
                    Text(
                        text = "Negotiated price: ${unitPrice.toBudgetString()}/unit · " +
                            "Volume: ${(unitPrice * amount).toBudgetString()}/mo · " +
                            "${TradeMarketViewModel.DEFAULT_DEAL_DURATION} months",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(
                        onClick = { onPropose(selectedCommodity, selectedType, amount) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Sign ${selectedType.name.lowercase()} contract")
                    }
                }
            }
        }
    }
}
