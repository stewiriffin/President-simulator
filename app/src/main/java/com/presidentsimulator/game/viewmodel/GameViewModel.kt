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
import com.presidentsimulator.game.data.TurnSummary
import com.presidentsimulator.game.data.WarOutcome
import com.presidentsimulator.game.data.CovertMission
import com.presidentsimulator.game.data.MissionStatus
import com.presidentsimulator.game.data.PlayableNationCatalog
import com.presidentsimulator.game.data.TechCatalog
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

    private val _timeSpeedMode = MutableStateFlow(TimeSpeedMode.PAUSED)
    val timeSpeedMode: StateFlow<TimeSpeedMode> = _timeSpeedMode.asStateFlow()

    private val _saveLoadFeedback = MutableStateFlow(SaveLoadFeedback())
    val saveLoadFeedback: StateFlow<SaveLoadFeedback> = _saveLoadFeedback.asStateFlow()

    /** Non-null after each End Turn — consumed by TurnSummaryDialog then cleared. */
    private val _turnSummary = MutableStateFlow<TurnSummary?>(null)
    val turnSummary: StateFlow<TurnSummary?> = _turnSummary.asStateFlow()

    private val _missionResults = MutableStateFlow<List<CovertMission>>(emptyList())
    val missionResults: StateFlow<List<CovertMission>> = _missionResults.asStateFlow()

    private val _warOutcome = MutableStateFlow<WarOutcome?>(null)
    val warOutcome: StateFlow<WarOutcome?> = _warOutcome.asStateFlow()

    /** Whether a persisted save exists so the launch screen can offer Continue. */
    private val _hasSave = MutableStateFlow(false)
    val hasSave: StateFlow<Boolean> = _hasSave.asStateFlow()

    /** True while showing the launch/new-game screen. */
    private val _showLaunchScreen = MutableStateFlow(true)
    val showLaunchScreen: StateFlow<Boolean> = _showLaunchScreen.asStateFlow()

    private var autoTickJob: Job? = null
    private val random = Random.Default
    private val diplomacyEngine = DiplomacyViewModel(random)
    private val productionLawEngine = ProductionLawViewModel()
    private val analyticsEngine = AnalyticsSaveViewModel()
    private val securityEngine = EspionageSecurityViewModel(random)
    private val advancementEngine = AdvancementViewModel()
    private val tradeEngine = TradeMarketViewModel(random)
    private val governanceEngine = GovernanceViewModel(random)
    private val demographicsEngine = DemographicsCampaignViewModel()

    private val savePrefs = application.getSharedPreferences(
        AnalyticsSaveViewModel.PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    init {
        _hasSave.value = hasAutomatedSave()
    }

    // ── Time engine ──────────────────────────────────────────────────────────

    /**
     * Advances one simulation month:
     * production/law physics, budget settlement, population dynamics,
     * geopolitics / war resolution, then a 15% roll for a macro event.
     */
    fun advanceTimeTick() {
        if (_currentActiveEvent.value != null) return
        if (_state.value.gameOver.isGameOver) return
        if (_turnSummary.value != null) return
        if (_missionResults.value.isNotEmpty()) return
        if (_warOutcome.value != null) return

        val before = _state.value
        val beforeMissions = before.espionage.activeMissions.associateBy { it.id }
        val beforeUnlocked = before.research.unlockedTechIds.toSet()
        val beforeActiveLaws = before.legal.activeLawIds.toSet()
        val beforePending = before.legal.pendingLaws.map { it.lawId }.toSet()

        _state.update { current ->
            if (current.gameOver.isGameOver) return@update current

            val (nextMonth, nextYear) = advanceDate(current.month, current.year)

            var next = current.copy(month = nextMonth, year = nextYear)

            // Industrial pipeline updates stocks and lastGoodsRevenue for this month.
            next = productionLawEngine.processProductionTick(next)

            val settledBudget = next.vitals.budget + next.netIncome
            val grownPopulation = applyPopulationChange(next)

            next = next.copy(
                vitals = next.vitals.copy(
                    budget = settledBudget,
                    population = grownPopulation,
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

            // Resolve pending laws in parliament
            next = productionLawEngine.processLawsTick(next)

            // Global markets, trade contracts, and tariff collection.
            next = tradeEngine.processTradeTick(next)

            // UN voting, resolution outcomes, and alliance passive effects.
            next = governanceEngine.processGovernanceTick(next)

            // Cohort approval, elections, and alternate victory paths.
            next = demographicsEngine.processDemographicsTick(next)
            if (next.gameOver.isGameOver) {
                return@update next
            }

            // Final ledger step: append KPI snapshot to the rolling history window.
            next = analyticsEngine.recordHistoricalSnapshot(next)
            next
        }

        val after = _state.value
        diplomacyEngine.consumeLastResolvedWar()?.let { outcome ->
            _warOutcome.value = outcome
            pauseTimeAdvance()
        }

        // Build turn summary before auto-save so deltas can be computed.
        val beforeSnap = after.analytics.history.takeLast(2)
        if (beforeSnap.size >= 2) {
            val prev = beforeSnap[beforeSnap.size - 2]
            val curr = beforeSnap.last()
            _turnSummary.value = TurnSummary(
                year = curr.year,
                month = curr.month,
                budgetDelta = curr.budget - prev.budget,
                approvalDelta = curr.approval - prev.approval,
                populationDelta = curr.population - prev.population,
                gdpDelta = curr.gdp - prev.gdp,
                netIncome = after.netIncome,
                bulletin = buildMonthlyBulletin(
                    before = before,
                    after = after,
                    beforeUnlocked = beforeUnlocked,
                    beforeActiveLaws = beforeActiveLaws,
                    beforePending = beforePending,
                ),
            )
        }

        if (after.gameOver.isGameOver) {
            pauseTimeAdvance()
        } else {
            val newlyResolved = after.espionage.activeMissions.filter { mission ->
                val prior = beforeMissions[mission.id]
                prior?.status == MissionStatus.ACTIVE &&
                    (mission.status == MissionStatus.SUCCESS || mission.status == MissionStatus.FAILED)
            }

            if (newlyResolved.isNotEmpty()) {
                _missionResults.value = _missionResults.value + newlyResolved
                pauseTimeAdvance()
            }

            // Auto-save after every tick.
            saveGameProgress()
            maybeTriggerEvent()
            if (_currentActiveEvent.value != null) {
                pauseTimeAdvance()
            }
        }
    }

    private fun buildMonthlyBulletin(
        before: GameState,
        after: GameState,
        beforeUnlocked: Set<String>,
        beforeActiveLaws: Set<String>,
        beforePending: Set<String>,
    ): List<String> {
        val lines = mutableListOf<String>()
        if (after.production.energyShortage) {
            lines += "Energy shortage — industrial output penalized to 30%."
        }
        if (after.production.foodShortage) {
            lines += "Food shortage — approval and population under pressure."
        }
        after.diplomacy.activeWar?.lastBattleSummary?.takeIf { it.isNotBlank() }?.let {
            lines += "War: $it"
        }
        _warOutcome.value?.let { war ->
            lines += if (war.victory) {
                "War won vs ${war.targetName} after ${war.monthsActive} months."
            } else {
                "War lost vs ${war.targetName} after ${war.monthsActive} months."
            }
        }
        val newTechs = after.research.unlockedTechIds.toSet() - beforeUnlocked
        newTechs.forEach { techId ->
            val name = TechCatalog.byId(techId)?.name ?: techId
            lines += "Technology unlocked: $name"
        }
        val enacted = after.legal.activeLawIds.toSet() - beforeActiveLaws
        enacted.forEach { lawId ->
            val name = com.presidentsimulator.game.data.LawCatalog.byId(lawId)?.name ?: lawId
            lines += "Law enacted: $name"
        }
        val repealed = beforeActiveLaws - after.legal.activeLawIds.toSet()
        repealed.forEach { lawId ->
            if (lawId !in beforePending || after.legal.pendingLaws.none { it.lawId == lawId }) {
                val name = com.presidentsimulator.game.data.LawCatalog.byId(lawId)?.name ?: lawId
                lines += "Law repealed: $name"
            }
        }
        after.espionage.activeMissions
            .filter { it.status == MissionStatus.SUCCESS || it.status == MissionStatus.FAILED }
            .filter { before.espionage.activeMissions.find { b -> b.id == it.id }?.status == MissionStatus.ACTIVE }
            .forEach { mission ->
                val rival = after.diplomacy.rivalById(mission.targetCountryId)?.name ?: mission.targetCountryId
                val tag = if (mission.status == MissionStatus.SUCCESS) "succeeded" else "failed"
                lines += "Covert op $tag in $rival"
            }
        if (after.internalSecurity.coupRisk >= 60f) {
            lines += "Coup risk elevated (${after.internalSecurity.coupRisk.roundToInt()}%)."
        }
        if (after.governance.activeResolution != null) {
            lines += "UN resolution pending: ${after.governance.activeResolution!!.type.displayName}"
        }
        if (after.trade.activeDeals.isNotEmpty()) {
            lines += "Trade contracts active: ${after.trade.activeDeals.size}"
        }
        return lines.take(8)
    }

    fun dismissMissionResult() {
        val current = _missionResults.value
        if (current.isNotEmpty()) {
            _missionResults.value = current.drop(1)
        }
    }

    fun clearWarOutcome() {
        _warOutcome.value = null
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
            _turnSummary.value = null
            _missionResults.value = emptyList()
            pauseTimeAdvance()
            _showLaunchScreen.value = false
            _hasSave.value = true
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
        _hasSave.value = true
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

    fun listSaveSlots(): List<SaveSlotInfo> =
        (1..AnalyticsSaveViewModel.SLOT_COUNT).map { slot ->
            val payload = savePrefs.getString(AnalyticsSaveViewModel.slotKey(slot), null)
            if (payload.isNullOrBlank()) {
                SaveSlotInfo(slotIndex = slot, occupied = false, label = "Slot $slot — Empty")
            } else {
                try {
                    val snap = analyticsEngine.importGameStateFromJson(payload)
                    SaveSlotInfo(
                        slotIndex = slot,
                        occupied = true,
                        year = snap.year,
                        month = snap.month,
                        label = "Slot $slot — ${snap.month}/${snap.year}",
                    )
                } catch (_: Exception) {
                    SaveSlotInfo(slotIndex = slot, occupied = true, label = "Slot $slot — Corrupt?")
                }
            }
        }

    fun saveToSlot(slot: Int) {
        if (slot !in 1..AnalyticsSaveViewModel.SLOT_COUNT) return
        val payload = analyticsEngine.exportGameStateToJson(_state.value)
        savePrefs.edit()
            .putString(AnalyticsSaveViewModel.slotKey(slot), payload)
            .putInt(AnalyticsSaveViewModel.KEY_LAST_PAYLOAD_BYTES, payload.length)
            .apply()
        _saveLoadFeedback.value = SaveLoadFeedback(
            message = "Saved to slot $slot (${AnalyticsSaveViewModel.formatBytes(payload.length)}).",
            payloadBytes = payload.length,
            success = true,
        )
        _hasSave.value = true
    }

    fun loadFromSlot(slot: Int) {
        if (slot !in 1..AnalyticsSaveViewModel.SLOT_COUNT) return
        val payload = savePrefs.getString(AnalyticsSaveViewModel.slotKey(slot), null)
        if (payload.isNullOrBlank()) {
            _saveLoadFeedback.value = SaveLoadFeedback(
                message = "Slot $slot is empty.",
                payloadBytes = 0,
                success = false,
            )
            return
        }
        if (importGameStateFromJson(payload)) {
            _saveLoadFeedback.value = SaveLoadFeedback(
                message = "Loaded slot $slot (${AnalyticsSaveViewModel.formatBytes(payload.length)}).",
                payloadBytes = payload.length,
                success = true,
            )
        }
    }

    fun clearTurnSummary() {
        _turnSummary.value = null
    }

    fun continueGame() {
        if (_hasSave.value) {
            loadLastAutomatedSave()
            _showLaunchScreen.value = false
        }
    }

    fun playableNations(): List<PlayableNationCatalog.NationDefinition> =
        PlayableNationCatalog.all()

    fun startNewGame(countryId: String = "veltra") {
        _state.value = GameState.initial(countryId)
        _currentActiveEvent.value = null
        _turnSummary.value = null
        _missionResults.value = emptyList()
        pauseTimeAdvance()
        _showLaunchScreen.value = false
        saveGameProgress()
    }

    fun returnToLaunch() {
        pauseTimeAdvance()
        _currentActiveEvent.value = null
        _turnSummary.value = null
        _missionResults.value = emptyList()
        _hasSave.value = hasAutomatedSave()
        _showLaunchScreen.value = true
    }

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

    fun breakTreaty(targetCountryId: String, type: TreatyType) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.breakTreaty(it, targetCountryId, type) }
    }

    fun setDefcon(level: Int) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.setDefcon(it, level) }
    }

    fun setIdeology(ideology: com.presidentsimulator.game.data.Ideology) {
        if (_currentActiveEvent.value != null) return
        _state.update { productionLawEngine.setIdeology(it, ideology) }
    }

    fun cancelPendingLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { productionLawEngine.cancelPendingLaw(it, lawId) }
    }

    fun rushPendingLaw(lawId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { productionLawEngine.rushPendingLaw(it, lawId) }
    }

    fun sendForeignAid(targetCountryId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.sendForeignAid(it, targetCountryId) }
    }

    fun conductStateVisit(targetCountryId: String) {
        if (_currentActiveEvent.value != null) return
        _state.update { diplomacyEngine.conductStateVisit(it, targetCountryId) }
    }

    fun runCampaignAction(action: CampaignAction) {
        if (_currentActiveEvent.value != null) return
        _state.update { demographicsEngine.runCampaignAction(it, action) }
    }

    fun worldEconomicRank(): Int = analyticsEngine.worldEconomicRank(_state.value)

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

    fun setTimeSpeedMode(mode: TimeSpeedMode) {
        if (_state.value.gameOver.isGameOver && mode != TimeSpeedMode.PAUSED) return
        _timeSpeedMode.value = mode
        syncTimeTickJob()
    }

    private fun syncTimeTickJob() {
        autoTickJob?.cancel()
        autoTickJob = null

        val interval = _timeSpeedMode.value.intervalMs ?: return
        autoTickJob = viewModelScope.launch {
            while (isActive) {
                val blocked = _currentActiveEvent.value != null ||
                    _turnSummary.value != null ||
                    _missionResults.value.isNotEmpty() ||
                    _warOutcome.value != null ||
                    _state.value.gameOver.isGameOver
                if (!blocked) {
                    advanceTimeTick()
                }
                delay(interval)
            }
        }
    }

    private fun pauseTimeAdvance() {
        _timeSpeedMode.value = TimeSpeedMode.PAUSED
        autoTickJob?.cancel()
        autoTickJob = null
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
        pauseTimeAdvance()
        super.onCleared()
    }

    companion object {
        const val MIN_TAX_RATE = 0.00f
        const val MAX_TAX_RATE = 0.50f
        const val EVENT_CHANCE_PER_TICK = 0.15f
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
