# UI Navigation and Screens

## Active Navigation Surface

Navigation is defined in `ui/navigation/GameNavigation.kt`.

### Active destinations

- `dashboard` -> `MainDashboardScreen`
- `economy` -> `EconomyScreen`
- `military` -> `MilitaryScreen`
- `diplomacy` -> `DiplomacyScreen`
- `secret_service` -> `SecurityScreen`
- `science` -> `ScienceScreen`
- `laws_society` -> `LawsScreen`
- `governance` -> `GovernanceUNScreen`
- `audio_settings` -> `SettingsAudioScreen`

### Global overlays

- Event modal: `EventCrisisDialog`
- Game-over modal: custom dialog in `GameNavigation`

---

## Screen Roles

## `MainDashboardScreen`

- Macro status and event cards
- Quick ministry jump tiles
- Hero country header and vitals cards
- Surface for urgent situations and central command UX

## `EconomyScreen`

- Sector performance cards
- Policy sliders (e.g., tax controls)
- Budget breakdown lines
- Trade-related economic summaries

## `MilitaryScreen`

- Branch and unit cards with readiness and strength
- Recruitment/procurement cards
- Hardware purchase actions
- Integrates policy effects from diplomacy/governance state

## `DiplomacyScreen`

- Rival cards and relation controls
- Treaty and negotiation views
- Displays `ActiveWarPanel` when war exists

## `SecurityScreen`

- Internal security metrics (instability/coup risk)
- Protocol toggles and security funding
- Spy network stats
- Active/recent covert operation cards and target actions

## `ScienceScreen`

- Current research progress panel
- Tech tree list with start/unlock status
- Research funding controls and tech metadata

## `LawsScreen`

- Category tabs (constitution/economy/social mapped to law categories)
- Per-law cards, active state, upkeep/effects
- Confirmation dialog for enact/repeal

## `GovernanceUNScreen`

- UN assembly and coalition tabs
- Resolution proposal and voting tracking
- Nation bribery controls
- Alliance creation/dissolution actions

## `SettingsAudioScreen`

- Music/SFX toggles and volume controls
- Diagnostics information from `GameAudioManager`
- Test sound trigger

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

---

## Image System Notes

- All major cards and headers use remote image URLs from `NssCardImages`.
- `NssPhotoHeader` handles load/fallback/scrims through Coil.
- Event illustrations first try local `R.drawable.event_<id>` resources, then remote fallback image mapping.

---

## Legacy / Non-primary UI Files

The repo also includes older/parallel screens in `ui/legacy/` (not all are active in current nav), including:

- `MainGameScreen`
- `AnalyticsDashboardScreen`
- `TradeLogisticsScreen`
- `ProductionLawScreen`
- `MilitaryDiplomacyScreen`
- `AdvancementSocietyScreen`
- `EspionageSecurityScreen`

When editing, verify whether a target screen is wired in `GameNavigation` before investing major effort.

