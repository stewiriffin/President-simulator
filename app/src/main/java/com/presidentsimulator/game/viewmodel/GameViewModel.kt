package com.presidentsimulator.game.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.presidentsimulator.game.data.DeploymentStatus
import com.presidentsimulator.game.data.EventChoice
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.data.EventRepository
import com.presidentsimulator.game.data.GameEvent
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.ResearchState
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.data.MissionType
import com.presidentsimulator.game.data.SaveLoadFeedback
import com.presidentsimulator.game.data.SecurityProtocol
import com.presidentsimulator.game.data.SocietyMinistry
import com.presidentsimulator.game.data.StateReligion
import com.presidentsimulator.game.data.ResolutionType
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeType
import com.presidentsimulator.game.data.TreatyType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Owns the global [GameState], drives the monthly simulation tick,
 * and mediates all player actions including crisis resolution.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(GameState.initial())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _currentActiveEvent = MutableStateFlow<GameEvent?>(null)
    val currentActiveEvent: StateFlow<GameEvent?> = _currentActiveEvent.asStateFlow()

    private val _isAutoTicking = MutableStateFlow(false)
    val isAutoTicking: StateFlow<Boolean> = _isAutoTicking.asStateFlow()

    private val _saveLoadFeedback = MutableStateFlow(SaveLoadFeedback())
    val saveLoadFeedback: StateFlow<SaveLoadFeedback> = _saveLoadFeedback.asStateFlow()

    private var autoTickJob: Job? = null
    private val random = Random.Default
    private val diplomacyEngine = DiplomacyViewModel(random)
    private val productionLawEngine = ProductionLawViewModel()
    private val analyticsEngine = AnalyticsSaveViewModel()
    private val securityEngine = EspionageSecurityViewModel(random)
    private val advancementEngine = AdvancementViewModel()
    private val tradeEngine = TradeMarketViewModel(random)
    private val governanceEngine = GovernanceViewModel(random)

    private val savePrefs = application.getSharedPreferences(
        AnalyticsSaveViewModel.PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    // ── Time engine ──────────────────────────────────────────────────────────

    /**
     * Advances one simulation month:
     * production/law physics, budget settlement, population dynamics,
     * geopolitics / war resolution, then a 15% roll for a macro event.
     */
    fun advanceTimeTick() {
        if (_currentActiveEvent.value != null) return
        if (_state.value.gameOver.isGameOver) return

        _state.update { current ->
            if (current.gameOver.isGameOver) return@update current

            val (nextMonth, nextYear) = advanceDate(current.month, current.year)

            var next = current.copy(month = nextMonth, year = nextYear)

            // Industrial pipeline updates stocks and lastGoodsRevenue for this month.
            next = productionLawEngine.processProductionTick(next)

            val settledBudget = next.vitals.budget + next.netIncome
            val grownPopulation = applyPopulationChange(next)
            val driftedApproval = (next.vitals.approval +
                (50f - next.vitals.approval) * 0.01f).coerceIn(0f, 100f)

            next = next.copy(
                vitals = next.vitals.copy(
                    budget = settledBudget,
                    population = grownPopulation,
                    approval = driftedApproval,
                ),
            )

            next = diplomacyEngine.simulateGeopolitics(next)
            if (next.diplomacy.activeWar != null) {
                next = diplomacyEngine.simulateWarBattle(next)
            }

            // Domestic stability, coup risk, and covert mission resolution.
            next = securityEngine.processSecurityTick(next)
            if (next.gameOver.isGameOver) {
                return@update next
            }

            // Science generation, social ministry funding, and health demographics.
            next = advancementEngine.processSocietyTick(next)

            // Global markets, trade contracts, and tariff collection.
            next = tradeEngine.processTradeTick(next)

            // UN voting, resolution outcomes, and alliance passive effects.
            next = governanceEngine.processGovernanceTick(next)

            // Final ledger step: append KPI snapshot to the rolling history window.
            next = analyticsEngine.recordHistoricalSnapshot(next)
            next
        }

        if (_state.value.gameOver.isGameOver) {
            stopAutoTick()
        } else {
            maybeTriggerEvent()
        }
    }

    // ── Global governance ────────────────────────────────────────────────────

    fun proposeResolution(type: ResolutionType, targetCountryId: String? = null) {
        if (_currentActiveEvent.value != null) return
        _state.update { governanceEngine.proposeResolution(it, type, targetCountryId) }
    }

    fun bribeCountryVote(countryId: String, voteFor: Boolean) {
        if (_currentActiveEvent.value != null) return
        _state.update { governanceEngine.bribeCountryVote(it, countryId, voteFor) }
    }

    fun formAlliance(name: String, invitees: List<String>) {
        if (_currentActiveEvent.value != null) return
        _state.update { governanceEngine.formAlliance(it, name, invitees) }
    }

    fun dissolveAlliance(allianceId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { governanceEngine.dissolveAlliance(it, allianceId) }
    }

    // ── Trade & markets ──────────────────────────────────────────────────────

    fun proposeTradeDeal(
        partnerCountryId: String,
        commodity: TradeCommodity,
        amount: Long,
        type: TradeType,
    ) {
        if (_currentActiveEvent.value != null) return
        _state.update {
            tradeEngine.proposeTradeDeal(it, partnerCountryId, commodity, amount, type)
        }
    }

    fun cancelTradeDeal(dealId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.cancelTradeDeal(it, dealId) }
    }

    fun setTariffRate(rate: Float) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.setTariffRate(it, rate) }
    }

    fun buyFromMarket(commodity: TradeCommodity, amount: Long = TradeMarketViewModel.SPOT_BUNDLE_SIZE) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.buyFromMarket(it, commodity, amount) }
    }

    fun sellToMarket(commodity: TradeCommodity, amount: Long = TradeMarketViewModel.SPOT_BUNDLE_SIZE) {
        if (_currentActiveEvent.value != null) return
        _state.update { tradeEngine.sellToMarket(it, commodity, amount) }
    }

    fun forecastTariffRevenue(rate: Float): Long =
        tradeEngine.forecastTariffRevenue(_state.value, rate)

    fun forecastTariffApprovalPenalty(rate: Float): Float =
        tradeEngine.forecastTariffApprovalPenalty(rate)

    fun negotiatedDealPrice(
        partnerCountryId: String,
        commodity: TradeCommodity,
        type: TradeType,
    ): Long {
        val rival = _state.value.diplomacy.rivalById(partnerCountryId) ?: return 0L
        val quote = _state.value.market.quote(commodity)
        return tradeEngine.negotiatedPrice(quote.currentPrice, rival.relationshipScore, type)
    }

    // ── Science & society ────────────────────────────────────────────────────

    fun unlockTechnology(techId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.unlockTechnology(it, techId) }
    }

    fun startResearch(techId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.startResearch(it, techId) }
    }

    fun allocateExtraResearchFunding() {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.allocateExtraResearchFunding(it) }
    }

    fun canStartResearch(techId: String): Boolean =
        AdvancementViewModel.canStartResearch(_state.value, techId)

    fun canAllocateExtraResearchFunding(): Boolean {
        val research = _state.value.research
        return research.activeTechId != null &&
            research.extraFundingTier < ResearchState.MAX_EXTRA_FUNDING_TIER &&
            _state.value.vitals.budget >= AdvancementViewModel.EXTRA_RESEARCH_FUNDING_COST
    }

    fun adjustMinistryFunding(ministry: SocietyMinistry, newFundingLevel: Float) {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.adjustMinistryFunding(it, ministry, newFundingLevel) }
    }

    fun changeStateReligion(religion: StateReligion) {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.changeStateReligion(it, religion) }
    }

    fun buildUniversity() {
        if (_currentActiveEvent.value != null) return
        _state.update { advancementEngine.buildUniversity(it) }
    }

    fun projectedSciencePerTick(): Long =
        advancementEngine.calculateScienceGenerated(_state.value)

    fun ministryForecast(ministry: SocietyMinistry): String =
        advancementEngine.forecastMinistryText(_state.value.society, ministry)

    fun canUnlockTechnology(techId: String): Boolean =
        AdvancementViewModel.canUnlock(_state.value, techId)

    // ── Espionage & internal security ────────────────────────────────────────

    fun deploySpy(targetCountryId: String, missionType: MissionType) {
        if (_currentActiveEvent.value != null) return
        _state.update { securityEngine.deploySpy(it, targetCountryId, missionType) }
    }

    fun fundInternalSecurity(amount: Int) {
        if (_currentActiveEvent.value != null) return
        _state.update { securityEngine.fundInternalSecurity(it, amount) }
    }

    fun toggleSecurityProtocol(protocol: SecurityProtocol) {
        if (_currentActiveEvent.value != null) return
        _state.update { securityEngine.toggleSecurityProtocol(it, protocol) }
    }

    fun recruitSpy() {
        if (_currentActiveEvent.value != null) return
        _state.update { securityEngine.recruitSpy(it) }
    }

    fun cancelCovertMission(missionId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { securityEngine.cancelCovertMission(it, missionId) }
    }

    fun estimateMissionSuccess(targetCountryId: String, missionType: MissionType): Float {
        val rival = _state.value.diplomacy.rivalById(targetCountryId) ?: return 0f
        return securityEngine.estimateSuccessProbability(
            state = _state.value,
            rivalMilitaryStrength = rival.militaryStrength,
            missionType = missionType,
        )
    }

    fun canDeploySpy(targetCountryId: String, missionType: MissionType): Boolean =
        EspionageSecurityViewModel.canDeploy(_state.value, targetCountryId, missionType)

    // ── Analytics & save/restore ──────────────────────────────────────────────

    fun currentGdp(): Long = analyticsEngine.calculateGDP(_state.value)

    fun exportGameStateToJson(): String =
        analyticsEngine.exportGameStateToJson(_state.value)

    fun importGameStateFromJson(jsonString: String): Boolean {
        return try {
            val restored = analyticsEngine.importGameStateFromJson(jsonString)
            _state.value = restored
            _currentActiveEvent.value = null
            _saveLoadFeedback.value = analyticsEngine.feedbackForLoad(jsonString)
            true
        } catch (error: Exception) {
            _saveLoadFeedback.value =
                analyticsEngine.feedbackForLoadFailure(error.message ?: "invalid payload")
            false
        }
    }

    fun saveGameProgress() {
        val payload = analyticsEngine.exportGameStateToJson(_state.value)
        savePrefs.edit()
            .putString(AnalyticsSaveViewModel.KEY_AUTOMATED_SAVE, payload)
            .putInt(AnalyticsSaveViewModel.KEY_LAST_PAYLOAD_BYTES, payload.length)
            .apply()
        _saveLoadFeedback.value = analyticsEngine.feedbackForSave(payload)
    }

    fun loadLastAutomatedSave() {
        val payload = savePrefs.getString(AnalyticsSaveViewModel.KEY_AUTOMATED_SAVE, null)
        if (payload.isNullOrBlank()) {
            _saveLoadFeedback.value = analyticsEngine.feedbackForMissingSave()
            return
        }
        importGameStateFromJson(payload)
    }

    fun hasAutomatedSave(): Boolean =
        !savePrefs.getString(AnalyticsSaveViewModel.KEY_AUTOMATED_SAVE, null).isNullOrBlank()

    // ── Production & law actions ──────────────────────────────────────────────

    fun enactLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { productionLawEngine.enactLaw(it, lawId) }
    }

    fun repealLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { productionLawEngine.repealLaw(it, lawId) }
    }

    fun buildPowerPlant(amount: Int) {
        _state.update { productionLawEngine.buildPowerPlant(it, amount) }
    }

    fun buildMine(amount: Int) {
        _state.update { productionLawEngine.buildMine(it, amount) }
    }

    fun canEnactLaw(lawId: String): Boolean {
        val law = com.presidentsimulator.game.data.LawCatalog.byId(lawId) ?: return false
        return ProductionLawViewModel.canEnact(_state.value, law)
    }

    // ── Military & diplomacy actions ─────────────────────────────────────────

    fun declareWar(targetCountryId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.declareWar(it, targetCountryId) }
    }

    fun negotiateTreaty(targetCountryId: String, type: TreatyType) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.negotiateTreaty(it, targetCountryId, type) }
    }

    fun signArmistice() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.signArmistice(it) }
    }

    fun setDeployment(status: DeploymentStatus) {
        _state.update { diplomacyEngine.setDeployment(it, status) }
    }

    fun setSalaryFunding(funding: Float) {
        _state.update { diplomacyEngine.setSalaryFunding(it, funding) }
    }

    fun recruitPersonnel(amount: Long) {
        _state.update { diplomacyEngine.recruitPersonnel(it, amount) }
    }

    fun purchaseTanks(amount: Int) {
        _state.update { diplomacyEngine.purchaseTanks(it, amount) }
    }

    fun purchaseJets(amount: Int) {
        _state.update { diplomacyEngine.purchaseJets(it, amount) }
    }

    fun purchaseShips(amount: Int) {
        _state.update { diplomacyEngine.purchaseShips(it, amount) }
    }

    fun purchaseNukes(amount: Int) {
        _state.update { diplomacyEngine.purchaseNukes(it, amount) }
    }

    fun purchaseMilitaryHardware(hardware: MilitaryHardware, amount: Int) {
        _state.update { diplomacyEngine.purchaseHardware(it, amount, hardware) }
    }

    fun launchOffensive() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.launchOffensive(it) }
    }

    fun holdDefensiveLine() {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.holdDefensiveLine(it) }
    }

    fun canDeclareWar(targetCountryId: String): Boolean =
        DiplomacyViewModel.canDeclareWar(_state.value, targetCountryId)

    fun canAffordTreaty(type: TreatyType): Boolean =
        DiplomacyViewModel.treatyAffordable(_state.value, type)

    fun armisticeCost(): Long = DiplomacyViewModel.armisticeCost(_state.value)

    fun toggleAutoTick() {
        if (_isAutoTicking.value) stopAutoTick() else startAutoTick()
    }

    fun startAutoTick() {
        if (autoTickJob?.isActive == true) return
        _isAutoTicking.value = true
        autoTickJob = viewModelScope.launch {
            while (isActive) {
                if (_currentActiveEvent.value == null) {
                    advanceTimeTick()
                }
                delay(TICK_INTERVAL_MS)
            }
        }
    }

    fun stopAutoTick() {
        autoTickJob?.cancel()
        autoTickJob = null
        _isAutoTicking.value = false
    }

    // ── Crisis engine ────────────────────────────────────────────────────────

    fun resolveEvent(choice: EventChoice) {
        val active = _currentActiveEvent.value ?: return
        if (choice !in active.choices) return

        _state.update { choice.consequence.applyTo(it) }
        _currentActiveEvent.value = null
    }

    private fun maybeTriggerEvent() {
        if (_currentActiveEvent.value != null) return
        if (random.nextFloat() >= EVENT_CHANCE_PER_TICK) return
        _currentActiveEvent.value = EventRepository.randomEvent(random)
    }

    // ── Economy actions ──────────────────────────────────────────────────────

    fun buildFactory(amount: Int) = buildInfrastructure(InfrastructureType.FACTORY, amount)

    fun buildFarm(amount: Int) = buildInfrastructure(InfrastructureType.FARM, amount)

    fun buildHousing(amount: Int) = buildInfrastructure(InfrastructureType.HOUSING, amount)

    fun buildInfrastructure(type: InfrastructureType, amount: Int) {
        if (amount <= 0) return
        _state.update { current ->
            val cost = type.unitCost * amount
            if (current.vitals.budget < cost) return@update current

            when (type) {
                InfrastructureType.POWER_PLANT ->
                    return@update productionLawEngine.buildPowerPlant(current, amount)
                InfrastructureType.MINE ->
                    return@update productionLawEngine.buildMine(current, amount)
                else -> Unit
            }

            val economy = current.economy
            val updatedEconomy = when (type) {
                InfrastructureType.FACTORY ->
                    economy.copy(factories = economy.factories + amount)
                InfrastructureType.FARM ->
                    economy.copy(farms = economy.farms + amount)
                InfrastructureType.HOUSING ->
                    economy.copy(housing = economy.housing + amount)
                InfrastructureType.POWER_PLANT,
                InfrastructureType.MINE,
                -> economy
            }

            val approvalBump = when (type) {
                InfrastructureType.HOUSING -> 0.4f * amount
                InfrastructureType.FARM -> 0.2f * amount
                InfrastructureType.FACTORY -> 0.1f * amount
                InfrastructureType.POWER_PLANT -> 0.15f * amount
                InfrastructureType.MINE -> 0.1f * amount
            }

            current.copy(
                vitals = current.vitals.copy(
                    budget = current.vitals.budget - cost,
                    approval = (current.vitals.approval + approvalBump).coerceIn(0f, 100f),
                ),
                economy = updatedEconomy,
            )
        }
    }

    fun maxAffordable(type: InfrastructureType): Int {
        val budget = _state.value.vitals.budget
        if (type.unitCost <= 0L) return 0
        return (budget / type.unitCost).toInt().coerceAtLeast(0)
    }

    fun adjustTaxes(newRate: Float) {
        val clamped = newRate.coerceIn(MIN_TAX_RATE, MAX_TAX_RATE)
        _state.update { current ->
            val delta = clamped - current.economy.taxRate
            val approvalDelta = -delta * 100f * 0.8f
            current.copy(
                vitals = current.vitals.copy(
                    approval = (current.vitals.approval + approvalDelta).coerceIn(0f, 100f),
                ),
                economy = current.economy.copy(taxRate = clamped),
            )
        }
    }

    fun projectNetIncome(taxRate: Float): Long {
        val current = _state.value
        return current.economy
            .copy(taxRate = taxRate.coerceIn(MIN_TAX_RATE, MAX_TAX_RATE))
            .netIncome(current.vitals.population)
    }

    fun projectTaxRevenue(taxRate: Float): Long {
        val current = _state.value
        return current.economy
            .copy(taxRate = taxRate.coerceIn(MIN_TAX_RATE, MAX_TAX_RATE))
            .taxRevenue(current.vitals.population)
    }

    // ── Internal physics ─────────────────────────────────────────────────────

    private fun advanceDate(month: Int, year: Int): Pair<Int, Int> =
        if (month >= 12) 1 to (year + 1) else (month + 1) to year

    private fun applyPopulationChange(state: GameState): Long {
        val housingCapacity = state.economy.housing * 1_500_000L
        val population = state.vitals.population
        val approval = state.vitals.approval

        val housingFactor = when {
            population < housingCapacity * 0.85 -> 1.002
            population > housingCapacity -> 0.997
            else -> 1.0005
        }
        val approvalFactor = when {
            approval >= 60f -> 1.001
            approval <= 30f -> 0.998
            else -> 1.0
        }

        return (population * housingFactor * approvalFactor)
            .toLong()
            .coerceAtLeast(1_000_000L)
    }

    override fun onCleared() {
        stopAutoTick()
        super.onCleared()
    }

    companion object {
        const val MIN_TAX_RATE = 0.00f
        const val MAX_TAX_RATE = 0.50f
        const val EVENT_CHANCE_PER_TICK = 0.15f
        const val TICK_INTERVAL_MS = 1_000L
    }
}

fun Long.toBudgetString(): String {
    val abs = kotlin.math.abs(this)
    val sign = if (this < 0) "-" else ""
    val body = when {
        abs >= 1_000_000_000_000L -> "%.1fT".format(abs / 1_000_000_000_000.0)
        abs >= 1_000_000_000L -> "%.1fB".format(abs / 1_000_000_000.0)
        abs >= 1_000_000L -> "%.1fM".format(abs / 1_000_000.0)
        abs >= 1_000L -> "%.1fK".format(abs / 1_000.0)
        else -> abs.toString()
    }
    return "$sign$$body"
}

fun Float.toApprovalString(): String = "${roundToInt()}%"

fun Long.toPopulationString(): String = when {
    this >= 1_000_000_000L -> "%.2fB".format(this / 1_000_000_000.0)
    this >= 1_000_000L -> "%.1fM".format(this / 1_000_000.0)
    this >= 1_000L -> "%.1fK".format(this / 1_000.0)
    else -> toString()
}

fun Long.toArmyString(): String = when {
    this >= 1_000_000L -> "%.2fM".format(this / 1_000_000.0)
    this >= 1_000L -> "%.0fK".format(this / 1_000.0)
    else -> toString()
}
