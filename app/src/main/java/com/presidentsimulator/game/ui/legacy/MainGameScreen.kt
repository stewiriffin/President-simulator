package com.presidentsimulator.game.ui.legacy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presidentsimulator.game.data.EventChoice
import com.presidentsimulator.game.data.EventConsequence
import com.presidentsimulator.game.data.GameEvent
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.data.Ministry
import com.presidentsimulator.game.ui.GovernanceUNScreen
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toApprovalString
import com.presidentsimulator.game.viewmodel.toArmyString
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toPopulationString
import kotlin.math.roundToInt

/**
 * Root Compose shell: HUD, ministry navigation, economy portal, and crisis interceptor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAutoTicking by viewModel.isAutoTicking.collectAsStateWithLifecycle()
    val activeEvent by viewModel.currentActiveEvent.collectAsStateWithLifecycle()
    val gameOver = state.gameOver.isGameOver
    val eventBlocking = activeEvent != null || gameOver

    var selectedMinistry by rememberSaveable { mutableStateOf<String?>(null) }
    val ministry = selectedMinistry?.let { name ->
        Ministry.entries.find { it.name == name }
    }

    activeEvent?.let { event ->
        EventInterceptorDialog(
            event = event,
            onChoiceSelected = viewModel::resolveEvent,
        )
    }

    if (gameOver) {
        GameOverDialog(
            reason = state.gameOver.reason,
            onLoadSave = viewModel::loadLastAutomatedSave,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = ministry?.title ?: "Nation State Simulator")
                },
                navigationIcon = {
                    if (ministry != null) {
                        IconButton(onClick = { selectedMinistry = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to dashboard",
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::advanceTimeTick,
                        enabled = !eventBlocking,
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next month",
                        )
                    }
                    IconButton(
                        onClick = viewModel::toggleAutoTick,
                        enabled = !gameOver,
                    ) {
                        Icon(
                            imageVector = if (isAutoTicking) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = if (isAutoTicking) {
                                "Pause auto-tick"
                            } else {
                                "Start auto-tick"
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            HudBanner(state = state)

            when (ministry) {
                null -> MinistryDashboard(
                    state = state,
                    onMinistrySelected = { selectedMinistry = it.name },
                )
                Ministry.ECONOMY -> EconomyPortal(
                    state = state,
                    viewModel = viewModel,
                )
                Ministry.COMMERCE -> TradeLogisticsScreen(
                    state = state,
                    viewModel = viewModel,
                )
                Ministry.INDUSTRY -> IndustryMinistryScreen(
                    state = state,
                    viewModel = viewModel,
                )
                Ministry.SCIENCE -> ScienceMinistryScreen(
                    state = state,
                    viewModel = viewModel,
                )
                Ministry.SOCIETY -> SocietyMinistriesScreen(
                    state = state,
                    viewModel = viewModel,
                )
                Ministry.PARLIAMENT -> ParliamentScreen(
                    state = state,
                    viewModel = viewModel,
                )
                Ministry.DEFENSE -> DefenseMinistryScreen(
                    state = state,
                    viewModel = viewModel,
                )
                Ministry.FOREIGN -> ForeignAffairsScreen(
                    state = state,
                    viewModel = viewModel,
                )
                Ministry.GOVERNANCE -> GovernanceUNScreen(
                    state = state,
                    viewModel = viewModel,
                )
                Ministry.STATISTICS -> AnalyticsDashboardScreen(
                    state = state,
                    viewModel = viewModel,
                )
                Ministry.INTERIOR -> EspionageSecurityScreen(
                    state = state,
                    viewModel = viewModel,
                )
            }
        }
    }
}

// ── HUD ──────────────────────────────────────────────────────────────────────

@Composable
private fun HudBanner(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HudStat(label = "Date", value = state.dateLabel)
            HudStat(
                label = "Budget",
                value = state.vitals.budget.toBudgetString(),
                subtitle = buildNetIncomeLabel(state.netIncome),
            )
            HudStat(
                label = "Approval",
                value = state.vitals.approval.toApprovalString(),
            )
            HudStat(
                label = "Population",
                value = state.vitals.population.toPopulationString(),
            )
        }
    }
}

@Composable
private fun HudStat(
    label: String,
    value: String,
    subtitle: String? = null,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (subtitle.startsWith("-")) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
            )
        }
    }
}

private fun buildNetIncomeLabel(netIncome: Long): String {
    val prefix = if (netIncome >= 0) "+" else ""
    return "$prefix${netIncome.toBudgetString()}/mo"
}

// ── Ministry dashboard ───────────────────────────────────────────────────────

@Composable
private fun MinistryDashboard(
    state: GameState,
    onMinistrySelected: (Ministry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Cabinet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
        Text(
            text = "Select a ministry to manage national policy.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "National Snapshot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SnapshotStat(
                        label = "Revenue",
                        value = state.economy.totalRevenue(state.vitals.population).toBudgetString(),
                    )
                    SnapshotStat(
                        label = "Expenses",
                        value = state.economy.totalExpenses.toBudgetString(),
                    )
                    SnapshotStat(
                        label = "Army",
                        value = state.military.armySize.toArmyString(),
                    )
                    SnapshotStat(
                        label = "DEFCON",
                        value = state.military.defcon.toString(),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(Ministry.entries) { ministry ->
                MinistryCard(
                    ministry = ministry,
                    onClick = { onMinistrySelected(ministry) },
                )
            }
        }
    }
}

@Composable
private fun SnapshotStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MinistryCard(
    ministry: Ministry,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = ministry.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(
                    text = ministry.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = ministry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open ${ministry.title}",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun Ministry.icon(): ImageVector = when (this) {
    Ministry.ECONOMY -> Icons.Default.AccountBalance
    Ministry.COMMERCE -> Icons.Default.Storefront
    Ministry.INDUSTRY -> Icons.Default.Factory
    Ministry.SCIENCE -> Icons.Default.Science
    Ministry.SOCIETY -> Icons.Default.VolunteerActivism
    Ministry.PARLIAMENT -> Icons.Default.Balance
    Ministry.DEFENSE -> Icons.Default.Security
    Ministry.FOREIGN -> Icons.Default.Public
    Ministry.GOVERNANCE -> Icons.Default.Language
    Ministry.STATISTICS -> Icons.Default.Analytics
    Ministry.INTERIOR -> Icons.Default.Groups
}

// ── Economy portal ───────────────────────────────────────────────────────────

@Composable
private fun EconomyPortal(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val economy = state.economy
    val population = state.vitals.population

    var draftTaxRate by remember(economy.taxRate) {
        mutableFloatStateOf(economy.taxRate)
    }
    var factoryAmount by remember { mutableIntStateOf(1) }
    var farmAmount by remember { mutableIntStateOf(1) }
    var housingAmount by remember { mutableIntStateOf(1) }

    val projectedRevenue = viewModel.projectTaxRevenue(draftTaxRate)
    val projectedNet = viewModel.projectNetIncome(draftTaxRate)
    val netDelta = projectedNet - state.netIncome

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Ministry of Economy",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tax Policy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Rate: ${(draftTaxRate * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Slider(
                    value = draftTaxRate,
                    onValueChange = { draftTaxRate = it },
                    onValueChangeFinished = { viewModel.adjustTaxes(draftTaxRate) },
                    valueRange = GameViewModel.MIN_TAX_RATE..GameViewModel.MAX_TAX_RATE,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth(),
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Projected Impact",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                ProjectionRow("Tax revenue / mo", projectedRevenue.toBudgetString())
                ProjectionRow("Net income / mo", projectedNet.toBudgetString())
                ProjectionRow(
                    label = "Change vs current",
                    value = "${if (netDelta >= 0) "+" else ""}${netDelta.toBudgetString()}",
                    emphasize = true,
                    positive = netDelta >= 0,
                )
                Text(
                    text = "Higher taxes raise revenue but lower public approval.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Monthly Cash Flow",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProjectionRow("Taxes", economy.taxRevenue(population).toBudgetString())
                ProjectionRow("Exports", economy.effectiveExports.toBudgetString())
                ProjectionRow("Goods sales", state.production.lastGoodsRevenue.toBudgetString())
                ProjectionRow("Imports", "-${economy.effectiveImports.toBudgetString()}")
                ProjectionRow("Infra upkeep", "-${economy.upkeep.toBudgetString()}")
                ProjectionRow("Military", "-${state.military.monthlyUpkeep.toBudgetString()}")
                ProjectionRow("Law upkeep", "-${state.legal.totalUpkeep.toBudgetString()}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProjectionRow(
                    label = "Net",
                    value = state.netIncome.toBudgetString(),
                    emphasize = true,
                    positive = state.netIncome >= 0,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Infrastructure",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    InfraStat("Factories", economy.factories)
                    InfraStat("Farms", economy.farms)
                    InfraStat("Housing", economy.housing)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "Construction Orders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                BuildAmountSelector(
                    type = InfrastructureType.FACTORY,
                    selectedAmount = factoryAmount,
                    maxAffordable = viewModel.maxAffordable(InfrastructureType.FACTORY),
                    onAmountSelected = { factoryAmount = it },
                    onBuild = {
                        viewModel.buildFactory(it)
                        factoryAmount = 1
                    },
                )
                HorizontalDivider()
                BuildAmountSelector(
                    type = InfrastructureType.FARM,
                    selectedAmount = farmAmount,
                    maxAffordable = viewModel.maxAffordable(InfrastructureType.FARM),
                    onAmountSelected = { farmAmount = it },
                    onBuild = {
                        viewModel.buildFarm(it)
                        farmAmount = 1
                    },
                )
                HorizontalDivider()
                BuildAmountSelector(
                    type = InfrastructureType.HOUSING,
                    selectedAmount = housingAmount,
                    maxAffordable = viewModel.maxAffordable(InfrastructureType.HOUSING),
                    onAmountSelected = { housingAmount = it },
                    onBuild = {
                        viewModel.buildHousing(it)
                        housingAmount = 1
                    },
                )
            }
        }
    }
}

@Composable
private fun ProjectionRow(
    label: String,
    value: String,
    emphasize: Boolean = false,
    positive: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (emphasize) {
                MaterialTheme.typography.bodyLarge
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            text = value,
            style = if (emphasize) {
                MaterialTheme.typography.bodyLarge
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = when {
                !emphasize -> MaterialTheme.colorScheme.onSurface
                positive -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.tertiary
            },
        )
    }
}

@Composable
private fun InfraStat(label: String, count: Int) {
    Column {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BuildAmountSelector(
    type: InfrastructureType,
    selectedAmount: Int,
    maxAffordable: Int,
    onAmountSelected: (Int) -> Unit,
    onBuild: (Int) -> Unit,
) {
    val presets = listOf(1, 10)
    val effectiveAmount = selectedAmount.coerceIn(0, maxAffordable.coerceAtLeast(0))
    val totalCost = type.unitCost * effectiveAmount
    val canBuild = effectiveAmount > 0 && maxAffordable >= effectiveAmount

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Build ${type.displayName}",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "Unit cost: ${type.unitCost.toBudgetString()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { amount ->
                FilterChip(
                    selected = selectedAmount == amount,
                    onClick = { if (maxAffordable >= amount) onAmountSelected(amount) },
                    enabled = maxAffordable >= amount,
                    label = { Text("${amount}x") },
                )
            }
            FilterChip(
                selected = selectedAmount == maxAffordable &&
                    maxAffordable > 0 &&
                    selectedAmount !in presets,
                onClick = {
                    if (maxAffordable > 0) onAmountSelected(maxAffordable)
                },
                enabled = maxAffordable > 0,
                label = {
                    Text(if (maxAffordable > 0) "Max ($maxAffordable)" else "Max")
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (canBuild) {
                "Order: $effectiveAmount × ${type.displayName} = ${totalCost.toBudgetString()}"
            } else {
                "Cannot afford any ${type.displayName.lowercase()}s"
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onBuild(effectiveAmount) },
            enabled = canBuild,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Commit Build")
        }
    }
}

// ── Game over ────────────────────────────────────────────────────────────────

@Composable
private fun GameOverDialog(
    reason: String,
    onLoadSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = {
            Text(
                text = "Coup d'État",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(text = reason)
        },
        confirmButton = {
            TextButton(onClick = onLoadSave) {
                Text("Load Last Save")
            }
        },
    )
}

// ── Event interceptor ────────────────────────────────────────────────────────

@Composable
private fun EventInterceptorDialog(
    event: GameEvent,
    onChoiceSelected: (EventChoice) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = {
            Column {
                Text(
                    text = "CRISIS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Choose a response",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                event.choices.forEach { choice ->
                    Card(
                        onClick = { onChoiceSelected(choice) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = choice.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = choice.consequence.toTradeOffSummary(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Text(
                text = "Time paused until resolved",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

private fun EventConsequence.toTradeOffSummary(): String {
    val parts = buildList {
        if (budgetChange != 0L) {
            val prefix = if (budgetChange > 0) "+" else ""
            add("Budget: $prefix${budgetChange.toBudgetString()}")
        }
        if (approvalChange != 0f) {
            val prefix = if (approvalChange > 0) "+" else ""
            add("Approval: $prefix${approvalChange.roundToInt()}%")
        }
        if (populationChange != 0L) {
            add("Population: ${populationChange.toSignedCountString()}")
        }
        if (factoriesChange != 0) {
            val prefix = if (factoriesChange > 0) "+" else ""
            add("Factories: $prefix$factoriesChange")
        }
        if (farmsChange != 0) {
            val prefix = if (farmsChange > 0) "+" else ""
            add("Farms: $prefix$farmsChange")
        }
        if (housingChange != 0) {
            val prefix = if (housingChange > 0) "+" else ""
            add("Housing: $prefix$housingChange")
        }
        if (armySizeChange != 0L) {
            add("Army: ${armySizeChange.toSignedCountString()}")
        }
        if (defconChange != 0) {
            val prefix = if (defconChange > 0) "+" else ""
            add("DEFCON: $prefix$defconChange")
        }
    }
    return parts.joinToString(" | ").ifEmpty { "No direct effect" }
}

private fun Long.toSignedCountString(): String {
    val sign = when {
        this > 0L -> "+"
        this < 0L -> "-"
        else -> ""
    }
    val abs = kotlin.math.abs(this)
    val body = when {
        abs >= 1_000_000_000L -> "%.2fB".format(abs / 1_000_000_000.0)
        abs >= 1_000_000L -> "%.1fM".format(abs / 1_000_000.0)
        abs >= 1_000L -> "%.1fK".format(abs / 1_000.0)
        else -> abs.toString()
    }
    return "$sign$body"
}
