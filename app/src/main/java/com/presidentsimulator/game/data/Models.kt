package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

/**
 * Root immutable snapshot of the nation simulation.
 * Every tick and player action produces a new [GameState] via [copy].
 */
@Serializable
data class GameState(
    val month: Int = 1,
    val year: Int = 2026,
    val vitals: VitalsState = VitalsState(),
    val economy: EconomyState = EconomyState(),
    val military: MilitaryState = MilitaryState(),
    val diplomacy: DiplomacyState = DiplomacyState(),
    val production: ProductionState = ProductionState(),
    val legal: LegalState = LegalState(),
    val analytics: AnalyticsState = AnalyticsState(),
    val internalSecurity: InternalSecurityState = InternalSecurityState(),
    val espionage: EspionageState = EspionageState(),
    val gameOver: GameOverState = GameOverState(),
    val research: ResearchState = ResearchState(),
    val society: SocietyState = SocietyState(),
    val trade: TradeState = TradeState(),
    val market: MarketState = MarketState(),
    val governance: GlobalGovernanceState = GlobalGovernanceState(),
) {
    val dateLabel: String
        get() = "${monthName(month)} $year"

    /** Trade treaties grant a passive export bonus. */
    val tradeExportBonus: Long
        get() = diplomacy.rivals.count { it.hasTradeTreaty } * 1_500_000_000L

    val netIncome: Long
        get() = economy.totalRevenue(vitals.population) +
            tradeExportBonus +
            production.lastGoodsRevenue +
            society.tourismIncome -
            economy.totalExpenses -
            military.monthlyUpkeep -
            legal.totalUpkeep -
            internalSecurity.monthlyUpkeep

    /** Combat power including tech, religion, and UN embargo modifiers. */
    val effectiveCombatStrength: Double
        get() {
            var strength = military.combatStrength *
                research.combinedEffects.militaryStrengthMultiplier *
                society.stateReligion.militaryMultiplier
            if (governance.nuclearEmbargoActive) {
                strength *= 0.88
            }
            if (governance.weaponsBanActive) {
                strength *= 0.94
            }
            return strength
        }

    /** Global production multiplier from laws, tech, and religion. */
    val effectiveProductionMultiplier: Float
        get() = legal.combinedProductionModifier *
            research.combinedEffects.productionMultiplier *
            society.stateReligion.productionMultiplier

    companion object {
        fun initial(): GameState = GameState(
            month = 1,
            year = 2026,
            vitals = VitalsState(
                budget = 50_000_000_000L,
                approval = 55f,
                population = 50_000_000L,
            ),
            economy = EconomyState(
                taxRate = 0.22f,
                exports = 8_000_000_000L,
                imports = 6_000_000_000L,
                factories = 25,
                farms = 30,
                housing = 40,
            ),
            military = MilitaryState(
                personnel = 450_000L,
                upkeepPerUnit = 8_000L,
                tanks = 800,
                jets = 120,
                deployment = DeploymentStatus.DEFENSIVE,
                defcon = 4,
            ),
            diplomacy = DiplomacyState(),
            production = ProductionState(
                energy = 2_500L,
                food = 6_000L,
                materials = 2_000L,
                goods = 1_000L,
                powerPlants = 22,
                mines = 18,
            ),
            legal = LegalState(
                ideology = Ideology.DEMOCRACY,
                activeLawIds = emptyList(),
            ),
            analytics = AnalyticsState(),
            internalSecurity = InternalSecurityState(),
            espionage = EspionageState(),
            gameOver = GameOverState(),
            research = ResearchState(),
            society = SocietyState(),
            trade = TradeState(),
            market = MarketState(),
            governance = GlobalGovernanceState(),
        )

        fun monthName(month: Int): String = MONTH_NAMES.getOrElse(month - 1) { "?" }

        private val MONTH_NAMES = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        )
    }
}

@Serializable
data class VitalsState(
    val budget: Long = 0L,
    val approval: Float = 50f,
    val population: Long = 0L,
)

/**
 * Fiscal and industrial state.
 * Revenue and expense formulas are pure functions of population and infrastructure.
 */
@Serializable
data class EconomyState(
    val taxRate: Float = 0.20f,
    val exports: Long = 0L,
    val imports: Long = 0L,
    val factories: Int = 0,
    val farms: Int = 0,
    val housing: Int = 0,
) {
    fun taxRevenue(population: Long): Long =
        (population * 40L * taxRate).toLong()

    val effectiveExports: Long
        get() = exports + factories * 120_000_000L

    val effectiveImports: Long
        get() = (imports - farms * 40_000_000L).coerceAtLeast(0L)

    val upkeep: Long
        get() = housing * 15_000_000L + factories * 25_000_000L + farms * 10_000_000L

    val totalExpenses: Long
        get() = upkeep + effectiveImports

    fun totalRevenue(population: Long): Long =
        taxRevenue(population) + effectiveExports

    fun netIncome(population: Long): Long =
        totalRevenue(population) - totalExpenses
}

data class GameEvent(
    val id: String,
    val title: String,
    val description: String,
    val choices: List<EventChoice>,
)

data class EventChoice(
    val text: String,
    val consequence: EventConsequence,
)

/**
 * Delta payload applied atomically when the player resolves an event choice.
 * Positive values increase the target stat; negative values decrease it.
 */
data class EventConsequence(
    val budgetChange: Long = 0L,
    val approvalChange: Float = 0f,
    val populationChange: Long = 0L,
    val factoriesChange: Int = 0,
    val farmsChange: Int = 0,
    val housingChange: Int = 0,
    val armySizeChange: Long = 0L,
    val defconChange: Int = 0,
) {
    fun applyTo(state: GameState): GameState = state.copy(
        vitals = state.vitals.copy(
            budget = state.vitals.budget + budgetChange,
            approval = (state.vitals.approval + approvalChange).coerceIn(0f, 100f),
            population = (state.vitals.population + populationChange).coerceAtLeast(1_000_000L),
        ),
        economy = state.economy.copy(
            factories = (state.economy.factories + factoriesChange).coerceAtLeast(0),
            farms = (state.economy.farms + farmsChange).coerceAtLeast(0),
            housing = (state.economy.housing + housingChange).coerceAtLeast(0),
        ),
        military = state.military.copy(
            personnel = (state.military.personnel + armySizeChange).coerceAtLeast(0L),
            defcon = (state.military.defcon + defconChange).coerceIn(1, 5),
        ),
    )
}

enum class InfrastructureType(
    val displayName: String,
    val unitCost: Long,
) {
    FACTORY("Factory", 2_000_000_000L),
    FARM("Farm", 800_000_000L),
    HOUSING("Housing", 1_200_000_000L),
    POWER_PLANT("Power Plant", 3_000_000_000L),
    MINE("Mine", 1_500_000_000L),
}

enum class Ministry(
    val title: String,
    val subtitle: String,
) {
    ECONOMY("Ministry of Economy", "Taxes & fiscal policy"),
    COMMERCE("Ministry of Commerce", "Trade deals, markets & tariffs"),
    INDUSTRY("Ministry of Industry", "Energy, food, materials & goods"),
    SCIENCE("Ministry of Science", "Technology tree & research"),
    SOCIETY("Social Ministries", "Health, education, culture & religion"),
    PARLIAMENT("Parliament", "Ideology & national laws"),
    DEFENSE("Ministry of Defense", "Army size & DEFCON"),
    FOREIGN("Foreign Affairs", "Treaties & diplomacy"),
    GOVERNANCE("Global Summit", "UN assembly & coalitions"),
    STATISTICS("Ministry of Statistics", "Analytics, trends & save data"),
    INTERIOR("Ministry of Interior", "Secret service, unrest & espionage"),
}

/**
 * Static pool of macro-level national events drawn by the crisis engine.
 */
object EventRepository {

    val eventPool: List<GameEvent> = listOf(
        GameEvent(
            id = "general_strike",
            title = "General Strike",
            description = "Unions across transport and manufacturing have walked out, " +
                "demanding higher wages and safer working conditions.",
            choices = listOf(
                EventChoice(
                    text = "Meet the unions' demands",
                    consequence = EventConsequence(
                        budgetChange = -5_000_000_000L,
                        approvalChange = 10f,
                    ),
                ),
                EventChoice(
                    text = "Offer a partial compromise",
                    consequence = EventConsequence(
                        budgetChange = -2_000_000_000L,
                        approvalChange = 3f,
                        factoriesChange = -1,
                    ),
                ),
                EventChoice(
                    text = "Break the strike by force",
                    consequence = EventConsequence(
                        budgetChange = -500_000_000L,
                        approvalChange = -15f,
                        factoriesChange = -3,
                        armySizeChange = -10_000L,
                    ),
                ),
            ),
        ),
        GameEvent(
            id = "resource_boom",
            title = "Resource Boom",
            description = "Surveyors confirm a massive mineral deposit under federal land. " +
                "How the state exploits it will shape the next decade of growth.",
            choices = listOf(
                EventChoice(
                    text = "Nationalize and develop fully",
                    consequence = EventConsequence(
                        budgetChange = -8_000_000_000L,
                        approvalChange = 4f,
                        factoriesChange = 4,
                        farmsChange = 1,
                    ),
                ),
                EventChoice(
                    text = "Auction extraction rights",
                    consequence = EventConsequence(
                        budgetChange = 15_000_000_000L,
                        approvalChange = -3f,
                        factoriesChange = 1,
                    ),
                ),
                EventChoice(
                    text = "Protect the land as a reserve",
                    consequence = EventConsequence(
                        approvalChange = 8f,
                        budgetChange = -1_000_000_000L,
                    ),
                ),
            ),
        ),
        GameEvent(
            id = "market_crash",
            title = "Global Market Crash",
            description = "Overseas markets have collapsed overnight. Exports are freezing " +
                "and domestic banks are pleading for emergency liquidity.",
            choices = listOf(
                EventChoice(
                    text = "Bail out the banks",
                    consequence = EventConsequence(
                        budgetChange = -12_000_000_000L,
                        approvalChange = -8f,
                        factoriesChange = -1,
                    ),
                ),
                EventChoice(
                    text = "Stimulus for households",
                    consequence = EventConsequence(
                        budgetChange = -7_000_000_000L,
                        approvalChange = 6f,
                        populationChange = 100_000L,
                    ),
                ),
                EventChoice(
                    text = "Let markets correct themselves",
                    consequence = EventConsequence(
                        budgetChange = -2_000_000_000L,
                        approvalChange = -18f,
                        factoriesChange = -4,
                        farmsChange = -1,
                    ),
                ),
            ),
        ),
        GameEvent(
            id = "epidemic",
            title = "Epidemic Outbreak",
            description = "A fast-spreading illness is overwhelming hospitals. " +
                "Citizens demand decisive action as infection rates climb.",
            choices = listOf(
                EventChoice(
                    text = "Fund a national emergency response",
                    consequence = EventConsequence(
                        budgetChange = -8_000_000_000L,
                        approvalChange = 8f,
                        populationChange = -200_000L,
                    ),
                ),
                EventChoice(
                    text = "Impose strict lockdowns",
                    consequence = EventConsequence(
                        budgetChange = -2_000_000_000L,
                        approvalChange = -12f,
                        populationChange = -80_000L,
                        factoriesChange = -2,
                    ),
                ),
                EventChoice(
                    text = "Downplay the threat",
                    consequence = EventConsequence(
                        approvalChange = -20f,
                        populationChange = -1_200_000L,
                    ),
                ),
            ),
        ),
        GameEvent(
            id = "border_incident",
            title = "Border Incident",
            description = "A neighboring power has massed troops along the frontier after " +
                "a disputed patrol clash. The cabinet awaits your orders.",
            choices = listOf(
                EventChoice(
                    text = "Mobilize and reinforce the border",
                    consequence = EventConsequence(
                        budgetChange = -6_000_000_000L,
                        approvalChange = 3f,
                        armySizeChange = 40_000L,
                        defconChange = -1,
                    ),
                ),
                EventChoice(
                    text = "Open emergency diplomacy",
                    consequence = EventConsequence(
                        budgetChange = -1_500_000_000L,
                        approvalChange = 5f,
                        defconChange = 1,
                    ),
                ),
                EventChoice(
                    text = "Issue a public ultimatum",
                    consequence = EventConsequence(
                        budgetChange = -500_000_000L,
                        approvalChange = -4f,
                        armySizeChange = 15_000L,
                        defconChange = -2,
                    ),
                ),
            ),
        ),
    )

    fun randomEvent(random: kotlin.random.Random = kotlin.random.Random.Default): GameEvent =
        eventPool.random(random)
}
