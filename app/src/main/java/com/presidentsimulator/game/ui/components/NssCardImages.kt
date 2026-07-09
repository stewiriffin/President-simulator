package com.presidentsimulator.game.ui.components

/**
 * Unsplash image URLs from the current Nation State Simulator Figma Make export
 * Leader portraits and banner art URLs (Nation State Simulator design reference).
 * Loaded at runtime via Coil; gradients are used as offline fallback.
 */
object NssCardImages {
    const val SERVICES = "https://images.unsplash.com/photo-1627327053419-fe894c4650ed?w=600&h=300&fit=crop&auto=format"
    const val INDUSTRY = "https://images.unsplash.com/photo-1517065963912-27f75001ebe2?w=600&h=300&fit=crop&auto=format"
    const val MANUFACTURING = "https://images.unsplash.com/photo-1700727448686-b314cb5f9948?w=600&h=300&fit=crop&auto=format"
    const val TECHNOLOGY = "https://images.unsplash.com/photo-1651340608985-d25cc73156e8?w=600&h=300&fit=crop&auto=format"
    const val AGRICULTURE = "https://images.unsplash.com/photo-1508175688576-0c076b47b5b5?w=600&h=300&fit=crop&auto=format"
    const val ENERGY = "https://images.unsplash.com/photo-1588011930968-eadac80e6a5a?w=600&h=300&fit=crop&auto=format"
    const val DEFENSE_IND = "https://images.unsplash.com/photo-1693515156811-25156b5345a3?w=600&h=300&fit=crop&auto=format"

    const val INFANTRY = "https://images.unsplash.com/photo-1630534658718-395efda906cb?w=600&h=300&fit=crop&auto=format"
    const val ARMORED = "https://images.unsplash.com/photo-1693515157462-e217eec5e786?w=600&h=300&fit=crop&auto=format"
    const val ARTILLERY = "https://images.unsplash.com/photo-1517065963912-27f75001ebe2?w=600&h=300&fit=crop&auto=format"
    const val SPECIAL_OPS = "https://images.unsplash.com/flagged/photo-1560177776-295b9cd779de?w=600&h=300&fit=crop&auto=format"
    const val DESTROYER = "https://images.unsplash.com/photo-1719553946838-1190abdeee92?w=600&h=300&fit=crop&auto=format"
    const val FRIGATE = "https://images.unsplash.com/photo-1708342421457-9c59f4843fe1?w=600&h=300&fit=crop&auto=format"
    const val SUBMARINE = "https://images.unsplash.com/photo-1775384222998-c3b458424353?w=600&h=300&fit=crop&auto=format"
    const val CARRIER = "https://images.unsplash.com/photo-1719553946838-1190abdeee92?w=600&h=300&fit=crop&auto=format"
    const val FIGHTER = "https://images.unsplash.com/photo-1689182314475-ff55f109b430?w=600&h=300&fit=crop&auto=format"
    const val BOMBER = "https://images.unsplash.com/photo-1536714303373-a2114b28b6b7?w=600&h=300&fit=crop&auto=format"
    const val DRONE = "https://images.unsplash.com/photo-1514598800938-f7125ea1aa1c?w=600&h=300&fit=crop&auto=format"

    const val BANNER_ECONOMY = "https://images.unsplash.com/photo-1605702012553-e954fbde66eb?w=1400&h=300&fit=crop&auto=format"
    const val BANNER_DEFENSE = "https://images.unsplash.com/photo-1678818048682-44b5cc5375a1?w=1400&h=300&fit=crop&auto=format"
    const val BANNER_FOREIGN = "https://images.unsplash.com/photo-1770308144171-77831cf9130a?w=1400&h=300&fit=crop&auto=format"
    const val MAP = "https://images.unsplash.com/photo-1543191879-742cb35a3a4e?w=1400&h=500&fit=crop&auto=format"
    const val PARLIAMENT = "https://images.unsplash.com/photo-1524634036752-81ec41a4f1ea?w=500&h=240&fit=crop&auto=format"

    const val LEADER_1 = "https://images.unsplash.com/photo-1556157382-97eda2d62296?w=150&h=150&fit=crop&auto=format"
    const val LEADER_2 = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&h=150&fit=crop&auto=format"
    const val LEADER_3 = "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=150&h=150&fit=crop&auto=format"
    const val LEADER_4 = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150&h=150&fit=crop&auto=format"
    const val LEADER_5 = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&auto=format"
    const val LEADER_6 = "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&h=150&fit=crop&auto=format"
    const val LEADER_7 = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&auto=format"
    const val LEADER_8 = "https://images.unsplash.com/photo-1548142813-c348350df52b?w=150&h=150&fit=crop&auto=format"
    const val LOCKED_BG = "https://images.unsplash.com/photo-1614064641938-3bbee52942c7?w=800&h=600&fit=crop&auto=format"

    const val BANNER_DOMESTIC = "https://images.unsplash.com/photo-1524634036752-81ec41a4f1ea?w=1400&h=200&fit=crop&auto=format"
    const val BANNER_INTELLIGENCE = "https://images.unsplash.com/photo-1737502483541-92e91801cfaf?w=1400&h=200&fit=crop&auto=format"
    const val BANNER_SCIENCE = "https://images.unsplash.com/photo-1651340608985-d25cc73156e8?w=1400&h=200&fit=crop&auto=format"
    const val BANNER_COMMAND = "https://images.unsplash.com/photo-1515868769-ad822a0c67e9?w=1400&h=200&fit=crop&auto=format"

    private val nationCardRotation = listOf(BANNER_FOREIGN, MAP, PARLIAMENT, BANNER_DEFENSE, DESTROYER, BANNER_ECONOMY)

    fun nationCardImage(index: Int): String = nationCardRotation[index.mod(nationCardRotation.size)]

    fun eventImage(eventId: String): String = when {
        eventId.contains("strike", ignoreCase = true) -> INDUSTRY
        eventId.contains("boom", ignoreCase = true) -> ENERGY
        eventId.contains("crash", ignoreCase = true) || eventId.contains("market", ignoreCase = true) -> BANNER_ECONOMY
        eventId.contains("epidemic", ignoreCase = true) -> SERVICES
        eventId.contains("border", ignoreCase = true) || eventId.contains("war", ignoreCase = true) -> INFANTRY
        else -> PARLIAMENT
    }

    fun techCategoryImage(category: com.presidentsimulator.game.data.TechCategory): String = when (category) {
        com.presidentsimulator.game.data.TechCategory.ECONOMY -> TECHNOLOGY
        com.presidentsimulator.game.data.TechCategory.MILITARY -> DEFENSE_IND
        com.presidentsimulator.game.data.TechCategory.SOCIETY -> SERVICES
    }

    fun lawCategoryImage(category: com.presidentsimulator.game.data.LawCategory): String = when (category) {
        com.presidentsimulator.game.data.LawCategory.MILITARY -> BANNER_DEFENSE
        com.presidentsimulator.game.data.LawCategory.ECONOMIC -> BANNER_ECONOMY
        com.presidentsimulator.game.data.LawCategory.SOCIAL -> PARLIAMENT
    }
}
