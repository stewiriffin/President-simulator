# UI Navigation and Screens

## Active Navigation Surface

Navigation destinations live in `ui/navigation/GameDestination.kt`.
Route composition is defined in `ui/navigation/GameNavigation.kt`.

### Active destinations

- `dashboard` -> `MainDashboardScreen`
- `economy` -> `EconomyScreen`
- `military` -> `MilitaryScreen`
- `diplomacy` -> `DiplomacyScreen`
- `secret_service` -> `SecurityScreen`
- `science` -> `ScienceScreen`
- `laws_society` -> `LawsScreen` (laws + SOCIETY funding/religion/universities tab)
- `governance` -> `GovernanceUNScreen`
- `audio_settings` -> `SettingsAudioScreen` (audio + save/load)
- `analytics` -> `AnalyticsScreen` (charts + save/load)
- `demographics` -> `ApprovalDemographicsScreen`

Bottom nav stays Overview / Economy / Defense / Foreign / Intel. Science, Domestic, UN, Settings, Analytics, and Demographics are reached from dashboard tiles (and Settings/Analytics for saves).

### Launch gate

When `showLaunchScreen` is true, `GameNavigation` shows `LaunchScreen` (Continue / New Game) instead of the HUD shell.

### Global overlays (priority)

1. `EventCrisisDialog` (active crisis)
2. `MissionResultDialog` (queued covert outcomes)
3. `TurnSummaryDialog` (post-tick deltas)
4. Campaign end dialog (coup loss, election loss, or victory) with Load Save + Return to Title

---

## Screen Roles

## `MainDashboardScreen`

- Macro status and event cards
- Quick ministry jump tiles (including Analytics / Demographics)
- Hero country header and vitals cards

## `EconomyScreen`

- Sector Invest builds factories/farms/housing/power/mines
- Tax policy slider
- Live Trade tab: tariffs, spot market, deals, propose contract

## `MilitaryScreen`

- Forces / recruitment / logistics
- Deployment posture + salary funding on Logistics

## `DiplomacyScreen`

- Rival cards, grain export, trade/NAP treaties, alliances, war

## `SecurityScreen`

- Internal security metrics and covert ops

## `ScienceScreen`

- Research progress and tech tree

## `LawsScreen`

- Law catalogs by tab + SOCIETY ministry/religion/university controls

## `GovernanceUNScreen`

- UN assembly, bribes, alliances

## `AnalyticsScreen` / `ApprovalDemographicsScreen`

- History charts + manual save/load
- Persistent approval cohorts and election year

## `SettingsAudioScreen`

- Music/SFX + optional save/load panel

## `LaunchScreen`

- Title Continue / New Game entry

---

## NSS UI Component System

Primary reusable components live in:

- `NssComponents.kt`
- `NssPhotoHeader.kt`
- `NssCardImages.kt`

Important shared pieces:

- `NssScreenHeader`
- `NssPanel`
- `NssGameBar` / `NssXpBar`
- `NssBadge`
- `NssPhotoHeader`
- `MinistryBottomNav`
- `GlobalHud`
- `TurnSummaryDialog` / `MissionResultDialog`

---

## Image System Notes

- Major cards/headers use remote image URLs from `NssCardImages`.
- `NssPhotoHeader` handles load/fallback/scrims through Coil with standardized `PhotoScrimAlpha` presets.

---

## Legacy / Non-primary UI Files

Older or parallel screens live in `ui/legacy/` and are not wired into active nav:

- `MainGameScreen`
- `AnalyticsDashboardScreen`
- `TradeLogisticsScreen`
- `ProductionLawScreen`
- `MilitaryDiplomacyScreen`
- `AdvancementSocietyScreen`
- `EspionageSecurityScreen`

When editing, verify whether a target screen is wired in `GameNavigation` before investing major effort.
