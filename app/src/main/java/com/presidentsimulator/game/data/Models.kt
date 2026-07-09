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
    val demographics: DemographicsState = DemographicsState(),
    /** Year of the next scheduled presidential election. */
    val nextElectionYear: Int = 2030,
    /** Nation the player leads — chosen at new game. */
    val playerNation: PlayerNation = PlayerNation(),
) {
    val dateLabel: String
        get() = "${monthName(month)} $year"

    /** Trade treaties grant a passive export bonus (scaled by national perk). */
    val tradeExportBonus: Long
        get() {
            val base = diplomacy.rivals.count { it.hasTradeTreaty } * 1_500_000_000L
            val perk = NationalPerkEffects.tradeIncomeMultiplier(
                NationalPerkEffects.forNationId(playerNation.id),
            )
            return (base * perk).toLong()
        }

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
            val perk = NationalPerkEffects.combatStrengthMultiplier(
                NationalPerkEffects.forNationId(playerNation.id),
            )
            var strength = military.combatStrength *
                research.combinedEffects.militaryStrengthMultiplier *
                society.stateReligion.militaryMultiplier *
                perk
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
        fun initial(countryId: String = "veltra"): GameState =
            PlayableNationCatalog.initialState(countryId)

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
    val instabilityChange: Float = 0f,
    val scienceChange: Long = 0L,
    val techUnlockId: String? = null,
    val relationshipChanges: Map<String, Int> = emptyMap(),
) {
    fun applyTo(state: GameState): GameState {
        var next = state.copy(
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
            internalSecurity = state.internalSecurity.copy(
                instabilityScore = (state.internalSecurity.instabilityScore + instabilityChange).coerceIn(0f, 100f),
            ),
            research = state.research.copy(
                sciencePoints = state.research.sciencePoints + scienceChange,
                unlockedTechIds = if (techUnlockId != null && techUnlockId !in state.research.unlockedTechIds) {
                    state.research.unlockedTechIds + techUnlockId
                } else state.research.unlockedTechIds,
            )
        )

        if (relationshipChanges.isNotEmpty()) {
            val updatedRivals = next.diplomacy.rivals.map { rival ->
                val change = relationshipChanges[rival.id] ?: 0
                if (change != 0) {
                    rival.copy(relationshipScore = (rival.relationshipScore + change).coerceIn(-100, 100))
                } else rival
            }
            next = next.copy(diplomacy = next.diplomacy.copy(rivals = updatedRivals))
        }

        return next
    }
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
        // Existing Events
        GameEvent(
            id = "general_strike",
            title = "General Strike",
            description = "Unions across transport and manufacturing have walked out, demanding higher wages and safer working conditions.",
            choices = listOf(
                EventChoice(
                    text = "Meet the unions' demands",
                    consequence = EventConsequence(budgetChange = -5_000_000_000L, approvalChange = 10f, instabilityChange = -5f),
                ),
                EventChoice(
                    text = "Offer a partial compromise",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, approvalChange = 3f, factoriesChange = -1),
                ),
                EventChoice(
                    text = "Break the strike by force",
                    consequence = EventConsequence(budgetChange = -500_000_000L, approvalChange = -15f, factoriesChange = -3, armySizeChange = -10_000L, instabilityChange = 12f),
                ),
            ),
        ),
        GameEvent(
            id = "resource_boom",
            title = "Resource Boom",
            description = "Surveyors confirm a massive mineral deposit under federal land. How the state exploits it will shape the next decade of growth.",
            choices = listOf(
                EventChoice(
                    text = "Nationalize and develop fully",
                    consequence = EventConsequence(budgetChange = -8_000_000_000L, approvalChange = 4f, factoriesChange = 4, farmsChange = 1),
                ),
                EventChoice(
                    text = "Auction extraction rights",
                    consequence = EventConsequence(budgetChange = 15_000_000_000L, approvalChange = -3f, factoriesChange = 1),
                ),
                EventChoice(
                    text = "Protect the land as a reserve",
                    consequence = EventConsequence(approvalChange = 8f, budgetChange = -1_000_000_000L, instabilityChange = -2f),
                ),
            ),
        ),
        GameEvent(
            id = "market_crash",
            title = "Global Market Crash",
            description = "Overseas markets have collapsed overnight. Exports are freezing and domestic banks are pleading for emergency liquidity.",
            choices = listOf(
                EventChoice(
                    text = "Bail out the banks",
                    consequence = EventConsequence(budgetChange = -12_000_000_000L, approvalChange = -8f, factoriesChange = -1, instabilityChange = 5f),
                ),
                EventChoice(
                    text = "Stimulus for households",
                    consequence = EventConsequence(budgetChange = -7_000_000_000L, approvalChange = 6f, populationChange = 100_000L),
                ),
                EventChoice(
                    text = "Let markets correct themselves",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, approvalChange = -18f, factoriesChange = -4, farmsChange = -1, instabilityChange = 15f),
                ),
            ),
        ),
        GameEvent(
            id = "epidemic",
            title = "Epidemic Outbreak",
            description = "A fast-spreading illness is overwhelming hospitals. Citizens demand decisive action as infection rates climb.",
            choices = listOf(
                EventChoice(
                    text = "Fund a national emergency response",
                    consequence = EventConsequence(budgetChange = -8_000_000_000L, approvalChange = 8f, populationChange = -200_000L),
                ),
                EventChoice(
                    text = "Impose strict lockdowns",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, approvalChange = -12f, populationChange = -80_000L, factoriesChange = -2, instabilityChange = 8f),
                ),
                EventChoice(
                    text = "Downplay the threat",
                    consequence = EventConsequence(approvalChange = -20f, populationChange = -1_200_000L, instabilityChange = 12f),
                ),
            ),
        ),
        GameEvent(
            id = "border_incident",
            title = "Border Incident",
            description = "A neighboring power has massed troops along the frontier after a disputed patrol clash. The cabinet awaits your orders.",
            choices = listOf(
                EventChoice(
                    text = "Mobilize and reinforce the border",
                    consequence = EventConsequence(budgetChange = -6_000_000_000L, approvalChange = 3f, armySizeChange = 40_000L, defconChange = -1, relationshipChanges = mapOf("westoria" to -15)),
                ),
                EventChoice(
                    text = "Open emergency diplomacy",
                    consequence = EventConsequence(budgetChange = -1_500_000_000L, approvalChange = 5f, defconChange = 1, relationshipChanges = mapOf("westoria" to 10)),
                ),
                EventChoice(
                    text = "Issue a public ultimatum",
                    consequence = EventConsequence(budgetChange = -500_000_000L, approvalChange = -4f, armySizeChange = 15_000L, defconChange = -2, relationshipChanges = mapOf("westoria" to -25)),
                ),
            ),
        ),

        // New Events
        GameEvent(
            id = "tech_breakthrough",
            title = "Research Breakthrough",
            description = "A rogue team of engineers claims to have cracked advanced robotic assembly in a makeshift lab. They demand federal funding.",
            choices = listOf(
                EventChoice(
                    text = "Fund their startup immediately",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, scienceChange = 50L, techUnlockId = "advanced_robotics", approvalChange = 2f),
                ),
                EventChoice(
                    text = "Absorb them into state science",
                    consequence = EventConsequence(budgetChange = -500_000_000L, scienceChange = 25L, approvalChange = -1f),
                ),
                EventChoice(
                    text = "Ignore the mavericks",
                    consequence = EventConsequence(approvalChange = -2f, scienceChange = -10L),
                ),
            ),
        ),
        GameEvent(
            id = "diplomatic_leak",
            title = "Intelligence Leak",
            description = "A disgruntled intelligence agent leaked classified cables mocking the leadership of the Northland Federation. They are furious.",
            choices = listOf(
                EventChoice(
                    text = "Formally apologize and pay reparations",
                    consequence = EventConsequence(budgetChange = -3_000_000_000L, approvalChange = -4f, relationshipChanges = mapOf("northland" to 5)),
                ),
                EventChoice(
                    text = "Deny the cables' authenticity",
                    consequence = EventConsequence(approvalChange = 2f, relationshipChanges = mapOf("northland" to -20), instabilityChange = 2f),
                ),
                EventChoice(
                    text = "Purge the intelligence agency",
                    consequence = EventConsequence(budgetChange = -1_000_000_000L, approvalChange = 1f, instabilityChange = 6f, relationshipChanges = mapOf("northland" to -5)),
                ),
            ),
        ),
        GameEvent(
            id = "student_protests",
            title = "Student Protests",
            description = "Students across the capital are protesting against perceived corruption and demanding sweeping social reforms.",
            choices = listOf(
                EventChoice(
                    text = "Promise sweeping reforms",
                    consequence = EventConsequence(budgetChange = -4_000_000_000L, approvalChange = 6f, instabilityChange = -8f),
                ),
                EventChoice(
                    text = "Wait for them to tire out",
                    consequence = EventConsequence(approvalChange = -5f, instabilityChange = 5f),
                ),
                EventChoice(
                    text = "Deploy riot police",
                    consequence = EventConsequence(budgetChange = -500_000_000L, approvalChange = -12f, instabilityChange = 15f),
                ),
            ),
        ),
        GameEvent(
            id = "bountiful_harvest",
            title = "Bountiful Harvest",
            description = "This year's crop yield is unprecedented. Warehouses are overflowing with grain, driving prices down but feeding everyone.",
            choices = listOf(
                EventChoice(
                    text = "Subsidize farmers to maintain prices",
                    consequence = EventConsequence(budgetChange = -1_500_000_000L, approvalChange = 4f, farmsChange = 1),
                ),
                EventChoice(
                    text = "Export the surplus aggressively",
                    consequence = EventConsequence(budgetChange = 4_000_000_000L, approvalChange = -2f),
                ),
                EventChoice(
                    text = "Distribute free rations to the poor",
                    consequence = EventConsequence(approvalChange = 10f, budgetChange = -500_000_000L, populationChange = 50_000L),
                ),
            ),
        ),
        GameEvent(
            id = "cyber_attack",
            title = "Infrastructure Cyber Attack",
            description = "Unknown hackers have crippled the national energy grid. Power plants are offline and the economy is stalling.",
            choices = listOf(
                EventChoice(
                    text = "Pay the ransom secretly",
                    consequence = EventConsequence(budgetChange = -5_000_000_000L, approvalChange = -6f, instabilityChange = 2f),
                ),
                EventChoice(
                    text = "Rebuild systems from backups",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, approvalChange = 2f, factoriesChange = -2, instabilityChange = 5f),
                ),
                EventChoice(
                    text = "Launch retaliatory cyber strikes",
                    consequence = EventConsequence(budgetChange = -1_000_000_000L, approvalChange = 8f, relationshipChanges = mapOf("eastmark" to -25), defconChange = -1),
                ),
            ),
        ),
        GameEvent(
            id = "olympic_bid",
            title = "Olympic Bid Opportunity",
            description = "The International Olympic Committee is accepting bids for the next games. Hosting would boost our global prestige immensely.",
            choices = listOf(
                EventChoice(
                    text = "Fund a massive infrastructure bid",
                    consequence = EventConsequence(budgetChange = -10_000_000_000L, approvalChange = 12f, housingChange = 3, relationshipChanges = mapOf("northland" to 5, "southreach" to 5)),
                ),
                EventChoice(
                    text = "Submit a modest proposal",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, approvalChange = 3f),
                ),
                EventChoice(
                    text = "Withdraw from consideration",
                    consequence = EventConsequence(approvalChange = -4f),
                ),
            ),
        ),
        GameEvent(
            id = "corruption_scandal",
            title = "Corruption Scandal",
            description = "Investigative journalists have uncovered a massive embezzlement ring involving several high-ranking generals.",
            choices = listOf(
                EventChoice(
                    text = "Arrest the generals and reform",
                    consequence = EventConsequence(approvalChange = 8f, armySizeChange = -20_000L, instabilityChange = 10f, budgetChange = 2_000_000_000L),
                ),
                EventChoice(
                    text = "Quietly force them to resign",
                    consequence = EventConsequence(approvalChange = 2f, armySizeChange = -5_000L, instabilityChange = 3f),
                ),
                EventChoice(
                    text = "Censor the press",
                    consequence = EventConsequence(budgetChange = -500_000_000L, approvalChange = -15f, instabilityChange = 15f),
                ),
            ),
        ),
        GameEvent(
            id = "refugee_crisis",
            title = "Refugee Crisis",
            description = "A civil war in a neighboring region has driven hundreds of thousands of refugees to our borders, seeking asylum.",
            choices = listOf(
                EventChoice(
                    text = "Welcome them and build camps",
                    consequence = EventConsequence(budgetChange = -4_000_000_000L, approvalChange = 2f, populationChange = 300_000L, instabilityChange = 6f),
                ),
                EventChoice(
                    text = "Accept skilled workers only",
                    consequence = EventConsequence(budgetChange = -1_000_000_000L, approvalChange = -2f, populationChange = 50_000L, factoriesChange = 1),
                ),
                EventChoice(
                    text = "Close the borders entirely",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, approvalChange = -6f, relationshipChanges = mapOf("southreach" to -15)),
                ),
            ),
        ),
        GameEvent(
            id = "foreign_aid_offer",
            title = "Foreign Aid Offer",
            description = "The Westoria Empire is offering a massive infrastructure grant, but critics warn of 'debt-trap diplomacy'.",
            choices = listOf(
                EventChoice(
                    text = "Accept the aid fully",
                    consequence = EventConsequence(budgetChange = 12_000_000_000L, approvalChange = 5f, relationshipChanges = mapOf("westoria" to 20, "northland" to -15), factoriesChange = 2),
                ),
                EventChoice(
                    text = "Negotiate a smaller, safer loan",
                    consequence = EventConsequence(budgetChange = 4_000_000_000L, approvalChange = 2f, relationshipChanges = mapOf("westoria" to 5)),
                ),
                EventChoice(
                    text = "Reject foreign interference",
                    consequence = EventConsequence(approvalChange = 4f, relationshipChanges = mapOf("westoria" to -10, "northland" to 10)),
                ),
            ),
        ),
        GameEvent(
            id = "military_defect",
            title = "Defecting General",
            description = "A top general from Eastmark has defected to our country, bringing highly classified war plans with them.",
            choices = listOf(
                EventChoice(
                    text = "Grant asylum and debrief them",
                    consequence = EventConsequence(scienceChange = 40L, relationshipChanges = mapOf("eastmark" to -30), defconChange = -1),
                ),
                EventChoice(
                    text = "Use them for a propaganda victory",
                    consequence = EventConsequence(approvalChange = 8f, relationshipChanges = mapOf("eastmark" to -20)),
                ),
                EventChoice(
                    text = "Quietly return them to Eastmark",
                    consequence = EventConsequence(approvalChange = -5f, relationshipChanges = mapOf("eastmark" to 20)),
                ),
            ),
        ),
        GameEvent(
            id = "natural_disaster",
            title = "Earthquake Hits Capital",
            description = "A 7.2 magnitude earthquake has struck the capital region. Infrastructure is devastated and casualties are rising.",
            choices = listOf(
                EventChoice(
                    text = "Deploy military and rebuild better",
                    consequence = EventConsequence(budgetChange = -15_000_000_000L, approvalChange = 10f, factoriesChange = -1, housingChange = -2, armySizeChange = -5_000L),
                ),
                EventChoice(
                    text = "Basic relief efforts only",
                    consequence = EventConsequence(budgetChange = -5_000_000_000L, approvalChange = -10f, factoriesChange = -3, housingChange = -4, populationChange = -50_000L),
                ),
                EventChoice(
                    text = "Accept international relief",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, approvalChange = 2f, factoriesChange = -2, housingChange = -3, relationshipChanges = mapOf("northland" to 10, "southreach" to 10)),
                ),
            ),
        ),
        GameEvent(
            id = "space_program",
            title = "National Space Program",
            description = "The Ministry of Science argues that launching a satellite would demonstrate our technological supremacy to the world.",
            choices = listOf(
                EventChoice(
                    text = "Fund the rocket launch",
                    consequence = EventConsequence(budgetChange = -8_000_000_000L, approvalChange = 12f, scienceChange = 30L),
                ),
                EventChoice(
                    text = "Delay for further testing",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, scienceChange = 10L),
                ),
                EventChoice(
                    text = "Cancel the vanity project",
                    consequence = EventConsequence(approvalChange = -6f, budgetChange = 1_000_000_000L),
                ),
            ),
        ),
        GameEvent(
            id = "union_formation",
            title = "Military Unionization",
            description = "Lower-ranking soldiers have formed an unsanctioned union demanding better pay and living conditions in the barracks.",
            choices = listOf(
                EventChoice(
                    text = "Increase military pay",
                    consequence = EventConsequence(budgetChange = -6_000_000_000L, approvalChange = 4f, instabilityChange = -5f),
                ),
                EventChoice(
                    text = "Arrest the union leaders",
                    consequence = EventConsequence(budgetChange = -500_000_000L, approvalChange = -5f, armySizeChange = -15_000L, instabilityChange = 12f),
                ),
                EventChoice(
                    text = "Ignore the demands",
                    consequence = EventConsequence(approvalChange = -8f, instabilityChange = 8f),
                ),
            ),
        ),
        GameEvent(
            id = "industrial_accident",
            title = "Industrial Accident",
            description = "A massive explosion at a chemical plant has poisoned a local river, causing outrage among environmental groups.",
            choices = listOf(
                EventChoice(
                    text = "Launch a massive cleanup effort",
                    consequence = EventConsequence(budgetChange = -5_000_000_000L, approvalChange = 5f, factoriesChange = -1),
                ),
                EventChoice(
                    text = "Fine the corporation heavily",
                    consequence = EventConsequence(budgetChange = 2_000_000_000L, approvalChange = -2f, factoriesChange = -2),
                ),
                EventChoice(
                    text = "Cover up the severity",
                    consequence = EventConsequence(budgetChange = -500_000_000L, approvalChange = -12f, instabilityChange = 10f),
                ),
            ),
        ),
        GameEvent(
            id = "cultural_renaissance",
            title = "Cultural Renaissance",
            description = "A new wave of literature and cinema from our country is taking the world by storm, boosting national pride.",
            choices = listOf(
                EventChoice(
                    text = "Subsidize the artists",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, approvalChange = 15f, relationshipChanges = mapOf("northland" to 5, "southreach" to 5, "eastmark" to 5, "westoria" to 5)),
                ),
                EventChoice(
                    text = "Tax the lucrative exports",
                    consequence = EventConsequence(budgetChange = 4_000_000_000L, approvalChange = 2f),
                ),
                EventChoice(
                    text = "Censor the radical works",
                    consequence = EventConsequence(approvalChange = -10f, instabilityChange = 8f),
                ),
            ),
        ),
        GameEvent(
            id = "water_shortage",
            title = "Regional Water Shortage",
            description = "A prolonged drought has emptied reservoirs in the agricultural belt. Farms are failing and cities are rationing taps.",
            choices = listOf(
                EventChoice(
                    text = "Fund emergency desalination and pipelines",
                    consequence = EventConsequence(budgetChange = -9_000_000_000L, approvalChange = 7f, farmsChange = 1, instabilityChange = -4f),
                ),
                EventChoice(
                    text = "Impose strict rationing",
                    consequence = EventConsequence(budgetChange = -1_000_000_000L, approvalChange = -8f, farmsChange = -2, instabilityChange = 6f),
                ),
                EventChoice(
                    text = "Import water and food at market rates",
                    consequence = EventConsequence(budgetChange = -4_000_000_000L, approvalChange = 2f, farmsChange = -1, relationshipChanges = mapOf("southreach" to 5)),
                ),
            ),
        ),
        GameEvent(
            id = "naval_standoff",
            title = "Naval Standoff",
            description = "Warships from Eastmark have entered disputed waters near our offshore energy platforms. Fishermen are fleeing and markets are jittery.",
            choices = listOf(
                EventChoice(
                    text = "Dispatch the fleet to escort platforms",
                    consequence = EventConsequence(budgetChange = -5_000_000_000L, approvalChange = 4f, armySizeChange = 20_000L, defconChange = -1, relationshipChanges = mapOf("eastmark" to -18)),
                ),
                EventChoice(
                    text = "Call an emergency summit",
                    consequence = EventConsequence(budgetChange = -1_000_000_000L, approvalChange = 3f, defconChange = 1, relationshipChanges = mapOf("eastmark" to 8, "northland" to 5)),
                ),
                EventChoice(
                    text = "Pull back and avoid escalation",
                    consequence = EventConsequence(approvalChange = -6f, relationshipChanges = mapOf("eastmark" to 10), instabilityChange = 4f),
                ),
            ),
        ),
        GameEvent(
            id = "housing_bubble",
            title = "Housing Bubble Burst",
            description = "Speculative real-estate prices have collapsed. Mortgages are underwater and construction sites sit abandoned.",
            choices = listOf(
                EventChoice(
                    text = "National housing relief program",
                    consequence = EventConsequence(budgetChange = -10_000_000_000L, approvalChange = 9f, housingChange = 2, instabilityChange = -5f),
                ),
                EventChoice(
                    text = "Rescue developers and banks",
                    consequence = EventConsequence(budgetChange = -7_000_000_000L, approvalChange = -6f, housingChange = 1, factoriesChange = -1),
                ),
                EventChoice(
                    text = "Let the market liquidate",
                    consequence = EventConsequence(budgetChange = -1_000_000_000L, approvalChange = -14f, housingChange = -3, instabilityChange = 12f),
                ),
            ),
        ),
        GameEvent(
            id = "scientific_prize",
            title = "International Science Prize",
            description = "Our national lab is a finalist for a global research prize. Winning would turbocharge recruitment—if we invest to finish the work.",
            choices = listOf(
                EventChoice(
                    text = "Bankroll the final push",
                    consequence = EventConsequence(budgetChange = -3_000_000_000L, scienceChange = 45L, approvalChange = 6f),
                ),
                EventChoice(
                    text = "Share the credit with allies",
                    consequence = EventConsequence(budgetChange = -1_000_000_000L, scienceChange = 20L, approvalChange = 3f, relationshipChanges = mapOf("northland" to 10)),
                ),
                EventChoice(
                    text = "Reassign the team to defense projects",
                    consequence = EventConsequence(scienceChange = 10L, armySizeChange = 10_000L, approvalChange = -2f),
                ),
            ),
        ),
        GameEvent(
            id = "smuggling_ring",
            title = "Arms Smuggling Ring",
            description = "Customs agents uncovered a sophisticated smuggling network moving weapons across the southern border.",
            choices = listOf(
                EventChoice(
                    text = "Fund a sweeping crackdown",
                    consequence = EventConsequence(budgetChange = -3_500_000_000L, approvalChange = 5f, instabilityChange = -6f, armySizeChange = 5_000L),
                ),
                EventChoice(
                    text = "Quietly negotiate with traffickers",
                    consequence = EventConsequence(budgetChange = 2_000_000_000L, approvalChange = -8f, instabilityChange = 8f, relationshipChanges = mapOf("southreach" to -10)),
                ),
                EventChoice(
                    text = "Tighten border fortifications",
                    consequence = EventConsequence(budgetChange = -6_000_000_000L, approvalChange = 2f, armySizeChange = 25_000L, defconChange = -1),
                ),
            ),
        ),
        GameEvent(
            id = "pension_crisis",
            title = "Pension Fund Crisis",
            description = "Actuaries warn the national pension system will be insolvent within a decade. Retirees are already organizing protests.",
            choices = listOf(
                EventChoice(
                    text = "Inject emergency funding",
                    consequence = EventConsequence(budgetChange = -11_000_000_000L, approvalChange = 8f, instabilityChange = -4f),
                ),
                EventChoice(
                    text = "Raise retirement age gradually",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, approvalChange = -10f, instabilityChange = 7f),
                ),
                EventChoice(
                    text = "Cut benefits for higher earners",
                    consequence = EventConsequence(budgetChange = 3_000_000_000L, approvalChange = -4f, instabilityChange = 3f),
                ),
            ),
        ),
        GameEvent(
            id = "media_blackout",
            title = "Foreign Media Blackout",
            description = "Westoria has banned our news outlets and diplomats after a critical documentary. Soft power and trade talks are freezing.",
            choices = listOf(
                EventChoice(
                    text = "Reciprocal bans and tariffs",
                    consequence = EventConsequence(budgetChange = 1_500_000_000L, approvalChange = 3f, factoriesChange = -1, relationshipChanges = mapOf("westoria" to -20)),
                ),
                EventChoice(
                    text = "Quiet diplomatic backchannel",
                    consequence = EventConsequence(budgetChange = -2_000_000_000L, approvalChange = 1f, relationshipChanges = mapOf("westoria" to 12)),
                ),
                EventChoice(
                    text = "Flood the region with cultural exports",
                    consequence = EventConsequence(budgetChange = -4_000_000_000L, approvalChange = 5f, scienceChange = 5L, relationshipChanges = mapOf("westoria" to 5, "northland" to 5)),
                ),
            ),
        ),
        GameEvent(
            id = "rare_earth_discovery",
            title = "Rare Earth Discovery",
            description = "Geologists found a rich rare-earth deposit under protected wetlands. Tech firms and environmentalists are colliding in the capital.",
            choices = listOf(
                EventChoice(
                    text = "Open mining with heavy regulation",
                    consequence = EventConsequence(budgetChange = -6_000_000_000L, approvalChange = 3f, factoriesChange = 3, scienceChange = 15L),
                ),
                EventChoice(
                    text = "Auction extraction to foreign consortia",
                    consequence = EventConsequence(budgetChange = 18_000_000_000L, approvalChange = -5f, factoriesChange = 1, relationshipChanges = mapOf("westoria" to 10, "eastmark" to 5)),
                ),
                EventChoice(
                    text = "Leave the wetlands untouched",
                    consequence = EventConsequence(approvalChange = 9f, budgetChange = -500_000_000L, instabilityChange = -3f),
                ),
            ),
        ),
        GameEvent(
            id = "coastal_flood_season",
            title = "Coastal Flood Season",
            description = "Unusual storm surges threaten ports and coastal cities. Relief agencies demand emergency appropriation before peak season.",
            choices = listOf(
                EventChoice(
                    text = "Fund nationwide seawall upgrades",
                    consequence = EventConsequence(budgetChange = -12_000_000_000L, approvalChange = 7f, instabilityChange = -4f, housingChange = 2),
                ),
                EventChoice(
                    text = "Target aid to swing districts only",
                    consequence = EventConsequence(budgetChange = -4_000_000_000L, approvalChange = 2f, instabilityChange = 3f),
                ),
                EventChoice(
                    text = "Declare it a local problem",
                    consequence = EventConsequence(approvalChange = -8f, instabilityChange = 6f, relationshipChanges = mapOf("westoria" to -3)),
                ),
            ),
        ),
        GameEvent(
            id = "whistleblower_leak",
            title = "Whistleblower Leak",
            description = "Classified procurement memos appear on foreign networks. Allies demand answers while opposition calls for hearings.",
            choices = listOf(
                EventChoice(
                    text = "Launch a transparent inquiry",
                    consequence = EventConsequence(budgetChange = -1_500_000_000L, approvalChange = 4f, instabilityChange = -2f, relationshipChanges = mapOf("westoria" to 6)),
                ),
                EventChoice(
                    text = "Plug the leak and deny everything",
                    consequence = EventConsequence(approvalChange = -6f, instabilityChange = 4f, scienceChange = -5L),
                ),
                EventChoice(
                    text = "Blame a rival intelligence service",
                    consequence = EventConsequence(approvalChange = 1f, relationshipChanges = mapOf("eastmark" to -12, "northland" to -5), instabilityChange = 2f),
                ),
            ),
        ),
        GameEvent(
            id = "grain_cartel_talks",
            title = "Grain Cartel Talks",
            description = "Major exporters have invited you into a quiet price-coordination pact. Domestic bakers and foreign buyers are watching closely.",
            choices = listOf(
                EventChoice(
                    text = "Join and lock in export premiums",
                    consequence = EventConsequence(budgetChange = 10_000_000_000L, approvalChange = -3f, farmsChange = 1, relationshipChanges = mapOf("northland" to 8, "westoria" to -6)),
                ),
                EventChoice(
                    text = "Refuse and champion free markets",
                    consequence = EventConsequence(approvalChange = 5f, relationshipChanges = mapOf("westoria" to 7), budgetChange = -1_000_000_000L),
                ),
                EventChoice(
                    text = "Leak the talks to embarrass rivals",
                    consequence = EventConsequence(approvalChange = 2f, instabilityChange = 1f, relationshipChanges = mapOf("eastmark" to -8, "northland" to -8)),
                ),
            ),
        ),

    )

    fun randomEvent(random: kotlin.random.Random = kotlin.random.Random.Default): GameEvent =
        eventPool.random(random)

    fun weightedEvent(
        state: GameState,
        random: kotlin.random.Random = kotlin.random.Random.Default,
    ): GameEvent {
        val weights = eventPool.map { event ->
            event to eventWeight(event, state)
        }
        val total = weights.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
        var roll = random.nextFloat() * total
        for ((event, weight) in weights) {
            roll -= weight
            if (roll <= 0f) return event
        }
        return eventPool.last()
    }

    private enum class EventTag {
        SHORTAGE, WAR, DIPLOMACY, ECONOMY, DOMESTIC, ELECTION,
    }

    private fun tagsFor(eventId: String): Set<EventTag> = when {
        eventId.contains("strike") || eventId.contains("shortage") ||
            eventId.contains("drought") || eventId.contains("blackout") -> setOf(EventTag.SHORTAGE, EventTag.DOMESTIC)
        eventId.contains("border") || eventId.contains("war") || eventId.contains("arms") ||
            eventId.contains("mobiliz") || eventId.contains("coup") -> setOf(EventTag.WAR, EventTag.DIPLOMACY)
        eventId.contains("grain") || eventId.contains("trade") || eventId.contains("market") ||
            eventId.contains("sanction") -> setOf(EventTag.ECONOMY, EventTag.DIPLOMACY)
        eventId.contains("election") || eventId.contains("scandal") || eventId.contains("protest") ->
            setOf(EventTag.ELECTION, EventTag.DOMESTIC)
        else -> setOf(EventTag.DOMESTIC)
    }

    private fun monthsUntilElection(state: GameState): Int {
        val monthsLeft = (state.nextElectionYear - state.year) * 12 + (12 - state.month)
        return monthsLeft.coerceAtLeast(0)
    }

    private fun eventWeight(event: GameEvent, state: GameState): Float {
        val tags = tagsFor(event.id)
        var weight = 1f

        if (state.production.foodShortage && EventTag.SHORTAGE in tags) weight *= 4f
        if (state.production.energyShortage && EventTag.SHORTAGE in tags) weight *= 3f

        if (state.diplomacy.activeWar != null) {
            if (EventTag.WAR in tags) weight *= 5f
        } else if (EventTag.WAR in tags) {
            weight *= 0.12f
        }

        if (monthsUntilElection(state) in 1..6 && EventTag.ELECTION in tags) weight *= 3f
        if (state.netIncome < 0 && EventTag.ECONOMY in tags) weight *= 2.2f
        if (state.internalSecurity.instabilityScore > 50f && EventTag.DOMESTIC in tags) weight *= 1.8f
        if (state.diplomacy.rivals.any { it.relationshipScore < -40 } && EventTag.DIPLOMACY in tags) {
            weight *= 1.5f
        }

        return weight
    }
}
