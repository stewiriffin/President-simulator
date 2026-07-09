package com.presidentsimulator.game.data

/**
 * World roster — every nation here can be selected as the player's country.
 * Unselected nations become AI rivals at game start.
 */
object PlayableNationCatalog {

    data class NationDefinition(
        val id: String,
        val name: String,
        val flagEmoji: String,
        val governmentLabel: String,
        val vitals: VitalsState,
        val economy: EconomyState,
        val military: MilitaryState,
        val production: ProductionState,
        val militaryStrength: Double,
        val economicPower: Double,
        val ideology: Ideology = Ideology.DEMOCRACY,
        val leaderImageUrl: String,
        val nationalPerk: String,
    ) {
        fun toPlayerNation(): PlayerNation = PlayerNation(
            id = id,
            name = name,
            flagEmoji = flagEmoji,
            governmentLabel = governmentLabel,
        )

        fun toRivalNation(playerCountryId: String): RivalNation {
            val (trade, nap) = WorldDiplomacy.treaties(playerCountryId, id)
            return RivalNation(
                id = id,
                name = name,
                flagEmoji = flagEmoji,
                relationshipScore = WorldDiplomacy.relationship(playerCountryId, id),
                militaryStrength = militaryStrength,
                economicPower = economicPower,
                hasTradeTreaty = trade,
                hasNonAggressionPact = nap,
            )
        }

        fun toInitialGameState(): GameState {
            val rivals = all()
                .filter { it.id != id }
                .map { it.toRivalNation(id) }
            return GameState(
                month = 1,
                year = 2026,
                playerNation = toPlayerNation(),
                vitals = vitals,
                economy = economy,
                military = military,
                diplomacy = DiplomacyState(rivals = rivals),
                production = production,
                legal = LegalState(ideology = ideology),
                demographics = DemographicsState(
                    workingClass = 55f,
                    businessElite = 58f,
                    military = 52f,
                    academics = 54f,
                ),
                nextElectionYear = 2030,
            )
        }
    }

    fun all(): List<NationDefinition> = NATIONS

    fun byId(id: String): NationDefinition? = NATIONS.find { it.id == id }

    fun initialState(countryId: String): GameState =
        byId(countryId)?.toInitialGameState() ?: NATIONS.first().toInitialGameState()

    private val NATIONS = listOf(
        NationDefinition(
            id = "veltra",
            name = "Veltra",
            flagEmoji = "🏛",
            governmentLabel = "Republic",
            vitals = VitalsState(budget = 50_000_000_000L, approval = 55f, population = 50_000_000L),
            economy = EconomyState(taxRate = 0.22f, exports = 8_000_000_000L, imports = 6_000_000_000L, factories = 25, farms = 30, housing = 40),
            military = MilitaryState(personnel = 450_000L, tanks = 800, jets = 120, ships = 45, defcon = 4),
            production = ProductionState(energy = 2_500L, food = 6_000L, materials = 2_000L, goods = 1_000L, powerPlants = 22, mines = 18),
            militaryStrength = 580.0,
            economicPower = 1.0,
            leaderImageUrl = NationLeaders.VELTRA,
            nationalPerk = "+10% economic yield from trade treaties",
        ),
        NationDefinition(
            id = "northland",
            name = "Northland Federation",
            flagEmoji = "🟦",
            governmentLabel = "Federation",
            vitals = VitalsState(budget = 46_000_000_000L, approval = 58f, population = 42_000_000L),
            economy = EconomyState(taxRate = 0.24f, exports = 9_500_000_000L, imports = 5_500_000_000L, factories = 28, farms = 22, housing = 36),
            military = MilitaryState(personnel = 380_000L, tanks = 650, jets = 95, ships = 38, defcon = 4),
            production = ProductionState(energy = 2_200L, food = 4_500L, materials = 2_400L, goods = 1_200L, powerPlants = 20, mines = 22),
            militaryStrength = 520.0,
            economicPower = 0.92,
            leaderImageUrl = NationLeaders.NORTHLAND,
            nationalPerk = "+8% export revenue from federation trade",
        ),
        NationDefinition(
            id = "eastmark",
            name = "Eastmark Republic",
            flagEmoji = "🟥",
            governmentLabel = "Republic",
            vitals = VitalsState(budget = 62_000_000_000L, approval = 48f, population = 68_000_000L),
            economy = EconomyState(taxRate = 0.20f, exports = 11_000_000_000L, imports = 8_500_000_000L, factories = 35, farms = 28, housing = 48),
            military = MilitaryState(personnel = 520_000L, tanks = 950, jets = 140, ships = 52, defcon = 3),
            production = ProductionState(energy = 3_100L, food = 5_500L, materials = 2_800L, goods = 1_400L, powerPlants = 26, mines = 24),
            militaryStrength = 680.0,
            economicPower = 1.05,
            ideology = Ideology.AUTOCRACY,
            leaderImageUrl = NationLeaders.EASTMARK,
            nationalPerk = "+12% military mobilization speed",
        ),
        NationDefinition(
            id = "southreach",
            name = "Southreach Union",
            flagEmoji = "🟩",
            governmentLabel = "Union",
            vitals = VitalsState(budget = 38_000_000_000L, approval = 61f, population = 35_000_000L),
            economy = EconomyState(taxRate = 0.19f, exports = 6_500_000_000L, imports = 5_000_000_000L, factories = 18, farms = 38, housing = 32),
            military = MilitaryState(personnel = 290_000L, tanks = 420, jets = 70, ships = 28, defcon = 5),
            production = ProductionState(energy = 1_800L, food = 7_500L, materials = 1_500L, goods = 800L, powerPlants = 16, mines = 14),
            militaryStrength = 410.0,
            economicPower = 0.78,
            leaderImageUrl = NationLeaders.SOUTHREACH,
            nationalPerk = "+15% farm output and food security",
        ),
        NationDefinition(
            id = "westoria",
            name = "Westoria Empire",
            flagEmoji = "🟨",
            governmentLabel = "Empire",
            vitals = VitalsState(budget = 82_000_000_000L, approval = 42f, population = 95_000_000L),
            economy = EconomyState(taxRate = 0.26f, exports = 14_000_000_000L, imports = 10_000_000_000L, factories = 42, farms = 32, housing = 55),
            military = MilitaryState(personnel = 620_000L, tanks = 1_200, jets = 180, ships = 68, defcon = 2),
            production = ProductionState(energy = 3_800L, food = 6_200L, materials = 3_500L, goods = 1_800L, powerPlants = 32, mines = 28),
            militaryStrength = 900.0,
            economicPower = 1.35,
            ideology = Ideology.AUTOCRACY,
            leaderImageUrl = NationLeaders.WESTORIA,
            nationalPerk = "+15% military power projection",
        ),
        NationDefinition(
            id = "aurumcoast",
            name = "Aurum Coast",
            flagEmoji = "🟧",
            governmentLabel = "Coastal Republic",
            vitals = VitalsState(budget = 58_000_000_000L, approval = 64f, population = 28_000_000L),
            economy = EconomyState(taxRate = 0.18f, exports = 12_500_000_000L, imports = 7_000_000_000L, factories = 20, farms = 18, housing = 30),
            military = MilitaryState(personnel = 220_000L, tanks = 350, jets = 55, ships = 62, defcon = 5),
            production = ProductionState(energy = 1_600L, food = 3_200L, materials = 1_800L, goods = 1_600L, powerPlants = 14, mines = 12),
            militaryStrength = 360.0,
            economicPower = 0.88,
            leaderImageUrl = NationLeaders.AURUMCOAST,
            nationalPerk = "+20% maritime trade income",
        ),
        NationDefinition(
            id = "kryos",
            name = "Kryos Directorate",
            flagEmoji = "⬜",
            governmentLabel = "Directorate",
            vitals = VitalsState(budget = 60_000_000_000L, approval = 45f, population = 58_000_000L),
            economy = EconomyState(taxRate = 0.23f, exports = 9_000_000_000L, imports = 7_500_000_000L, factories = 30, farms = 24, housing = 42),
            military = MilitaryState(personnel = 500_000L, tanks = 880, jets = 130, ships = 48, defcon = 3),
            production = ProductionState(energy = 2_900L, food = 4_800L, materials = 3_000L, goods = 1_100L, powerPlants = 24, mines = 30),
            militaryStrength = 740.0,
            economicPower = 1.12,
            ideology = Ideology.AUTOCRACY,
            leaderImageUrl = NationLeaders.KRYOS,
            nationalPerk = "+10% industrial materials output",
        ),
        NationDefinition(
            id = "verdehaan",
            name = "Verdehaan Compact",
            flagEmoji = "🟫",
            governmentLabel = "Compact",
            vitals = VitalsState(budget = 32_000_000_000L, approval = 67f, population = 22_000_000L),
            economy = EconomyState(taxRate = 0.17f, exports = 5_500_000_000L, imports = 4_200_000_000L, factories = 14, farms = 42, housing = 26),
            military = MilitaryState(personnel = 180_000L, tanks = 280, jets = 45, ships = 22, defcon = 5),
            production = ProductionState(energy = 1_400L, food = 8_200L, materials = 1_200L, goods = 650L, powerPlants = 12, mines = 10),
            militaryStrength = 290.0,
            economicPower = 0.65,
            leaderImageUrl = NationLeaders.VERDEHAAN,
            nationalPerk = "+18% agricultural resilience",
        ),
    )

    /** Leader portrait URLs from the zip UI reference (`IMG.leader_*`). */
    private object NationLeaders {
        const val VELTRA = "https://images.unsplash.com/photo-1556157382-97eda2d62296?w=400&h=400&fit=crop&auto=format"
        const val NORTHLAND = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&h=400&fit=crop&auto=format"
        const val EASTMARK = "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=400&h=400&fit=crop&auto=format"
        const val SOUTHREACH = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400&h=400&fit=crop&auto=format"
        const val WESTORIA = "https://images.unsplash.com/photo-1548142813-c348350df52b?w=400&h=400&fit=crop&auto=format"
        const val AURUMCOAST = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=400&fit=crop&auto=format"
        const val KRYOS = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&h=400&fit=crop&auto=format"
        const val VERDEHAAN = "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=400&h=400&fit=crop&auto=format"
    }

    private object WorldDiplomacy {
        private data class Bilateral(val a: String, val b: String) {
            companion object {
                fun of(x: String, y: String) = if (x <= y) Bilateral(x, y) else Bilateral(y, x)
            }
        }

        private data class Relation(val score: Int, val trade: Boolean = false, val nap: Boolean = false)

        private val explicit = mapOf(
            Bilateral.of("veltra", "northland") to Relation(25, trade = true),
            Bilateral.of("veltra", "eastmark") to Relation(-15),
            Bilateral.of("veltra", "southreach") to Relation(10, nap = true),
            Bilateral.of("veltra", "westoria") to Relation(-55),
            Bilateral.of("veltra", "aurumcoast") to Relation(40, trade = true),
            Bilateral.of("veltra", "kryos") to Relation(-35),
            Bilateral.of("veltra", "verdehaan") to Relation(18, nap = true),
            Bilateral.of("northland", "eastmark") to Relation(-25),
            Bilateral.of("northland", "westoria") to Relation(-40),
            Bilateral.of("eastmark", "westoria") to Relation(-60),
            Bilateral.of("southreach", "verdehaan") to Relation(30, trade = true),
            Bilateral.of("aurumcoast", "verdehaan") to Relation(35, trade = true),
            Bilateral.of("kryos", "westoria") to Relation(-20),
        )

        fun relationship(playerId: String, rivalId: String): Int =
            explicit[Bilateral.of(playerId, rivalId)]?.score ?: pseudoNeutral(playerId, rivalId)

        fun treaties(playerId: String, rivalId: String): Pair<Boolean, Boolean> {
            val rel = explicit[Bilateral.of(playerId, rivalId)]
            return (rel?.trade == true) to (rel?.nap == true)
        }

        private fun pseudoNeutral(a: String, b: String): Int =
            ((a.hashCode() xor b.hashCode()) % 50 - 10).coerceIn(-100, 100)
    }
}
