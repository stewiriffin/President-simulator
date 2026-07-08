# Codebase File Index (AI-Oriented)

This is a practical index of where to edit what.

## Entry and App Shell

- `app/src/main/java/com/presidentsimulator/game/MainActivity.kt`
- `app/src/main/java/com/presidentsimulator/game/ui/navigation/GameDestination.kt`
- `app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt`

## Data Models (`data/`)

- `Models.kt` - root `GameState`, core vitals/economy, events, infrastructure enums
- `DemographicsModels.kt` - persistent approval cohorts + blend weights
- `MilitaryAndDiplomacyModels.kt` - military assets, rivals, treaties, war state
- `ProductionAndLawModels.kt` - production stocks/flows, laws, legal state/catalog
- `AdvancementModels.kt` - society, religion, technology, research catalog/state
- `EspionageAndSecurityModels.kt` - instability/coup, espionage, covert missions, `GameOverState` (incl. victory)
- `TradeAndMarketModels.kt` - commodities, market quotes, trade deals/state
- `GovernanceModels.kt` - alliances, UN resolutions, governance state
- `AnalyticsAndSaveModels.kt` - historical snapshots and save feedback models
- `TurnSummary.kt` - post-tick summary DTO

## ViewModel / Engine Layer (`viewmodel/`)

- `GameViewModel.kt` - action facade + tick orchestration + launch/overlays
- `DiplomacyViewModel.kt` - war/diplomacy simulation and military actions
- `ProductionLawViewModel.kt` - production tick + law enact/repeal
- `AdvancementViewModel.kt` - science/research/society/religion logic
- `EspionageSecurityViewModel.kt` - security and covert mission logic
- `TradeMarketViewModel.kt` - market pricing, trade settlement, tariff logic
- `GovernanceViewModel.kt` - UN voting, alliances, global modifiers
- `AnalyticsSaveViewModel.kt` - GDP/snapshots/JSON save-load helpers
- `DemographicsCampaignViewModel.kt` - cohort approval, elections, alternate victories

## Active Screen Layer (`ui/screens/` + active governance)

- `LaunchScreen.kt`
- `MainDashboardScreen.kt`
- `EconomyScreen.kt`
- `MilitaryScreen.kt`
- `DiplomacyScreen.kt`
- `SecurityScreen.kt`
- `ScienceScreen.kt`
- `LawsScreen.kt`
- `GovernanceUNScreen.kt` (`ui/GovernanceUNScreen.kt`)
- `SettingsAudioScreen.kt`
- `AnalyticsScreen.kt`
- `ApprovalDemographicsScreen.kt`

## Additional/Legacy UI files in `ui/legacy/`

- `MainGameScreen.kt` (legacy shell)
- `AnalyticsDashboardScreen.kt`
- `TradeLogisticsScreen.kt`
- `ProductionLawScreen.kt`
- `MilitaryDiplomacyScreen.kt`
- `AdvancementSocietyScreen.kt`
- `EspionageSecurityScreen.kt`

## Shared Components (`ui/components/`)

- `NssComponents.kt` - primary reusable NSS UI primitives/cards
- `NssPhotoHeader.kt` - shared media header with Coil fallback/scrims
- `NssCardImages.kt` - image URL registry and mapping helpers
- `GlobalHud.kt` - top-level HUD controls
- `MinistryBottomNav.kt` - active navigation bar
- `MinistrySideNav.kt` - alternative/legacy nav style
- `EventCrisisDialog.kt` - crisis modal
- `ActiveWarPanel.kt` - war actions/status panel
- `ActiveOperationCard.kt` - covert mission status card
- `BulkBuildControls.kt` - quantity control component
- `graphics/CountryFlag.kt` - flag rendering helper
- `graphics/EventIllustration.kt` - crisis illustration fallback pipeline

## Audio

- `audio/GameAudioManager.kt` - BGM/SFX engine
- `audio/GameAudioBridge.kt` - game-state-to-audio bridge

## Theme

- `ui/theme/Color.kt`
- `ui/theme/Theme.kt`
- `ui/theme/GameIcons.kt`
