package com.presidentsimulator.game.ui.navigation

sealed class GameDestination(val route: String, val title: String) {
    data object Dashboard : GameDestination("dashboard", "Command Center")
    data object Economy : GameDestination("economy", "Economy")
    data object Military : GameDestination("military", "Defense")
    data object Diplomacy : GameDestination("diplomacy", "Foreign Affairs")
    data object SecretService : GameDestination("secret_service", "Intelligence")
    data object Science : GameDestination("science", "Science")
    data object LawsSociety : GameDestination("laws_society", "Domestic Policy")
    data object Governance : GameDestination("governance", "United Nations")
    data object AudioSettings : GameDestination("audio_settings", "Settings")
    data object Analytics : GameDestination("analytics", "Analytics")
    data object Demographics : GameDestination("demographics", "Demographics")
    data object Cabinet : GameDestination("cabinet", "Cabinet")

    companion object {
        fun fromRoute(route: String): GameDestination? = when (route) {
            Dashboard.route -> Dashboard
            Economy.route -> Economy
            Military.route -> Military
            Diplomacy.route -> Diplomacy
            SecretService.route -> SecretService
            Science.route -> Science
            LawsSociety.route -> LawsSociety
            Governance.route -> Governance
            AudioSettings.route -> AudioSettings
            Analytics.route -> Analytics
            Demographics.route -> Demographics
            Cabinet.route -> Cabinet
            else -> null
        }
    }
}
