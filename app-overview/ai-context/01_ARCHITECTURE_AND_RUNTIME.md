# Architecture and Runtime

## Stack

- Language: Kotlin
- UI: Jetpack Compose + Material3
- State: `StateFlow`
- Serialization: `kotlinx.serialization`
- Navigation: `navigation-compose`
- Image loading: Coil
- Audio: `MediaPlayer` + `SoundPool`
- Build: Gradle Kotlin DSL

## Build Targets

- `compileSdk` / `targetSdk`: 35
- `minSdk`: 26
- JVM target: 17
- Compose enabled in Gradle build features

## Runtime Entry

### 1) `MainActivity`

`MainActivity`:

- Initializes `GameAudioManager`
- Enables edge-to-edge
- Sets `PresidentSimulatorTheme`
- Mounts `GameNavigation(viewModel = gameViewModel)`

### 2) `GameNavigation`

`GameNavigation` is the shell and traffic controller:

- Collects `state`, `isAutoTicking`, and `currentActiveEvent` from `GameViewModel`
- Renders global overlays:
  - Crisis dialog (`EventCrisisDialog`)
  - Game-over modal dialog
- Renders shell frame:
  - `GlobalHud` (top)
  - route content via `NavHost` (middle)
  - `MinistryBottomNav` (bottom)

### 3) Route Set (active)

- Dashboard
- Economy
- Military
- Diplomacy
- Secret Service (Intelligence)
- Science
- Laws/Society
- Governance (UN)
- Audio Settings

---

## State Ownership Pattern

- `GameViewModel` is the single source of truth.
- `_state: MutableStateFlow<GameState>` is private, exposed as read-only `StateFlow`.
- UI reads only from public flows and sends intents via methods on `GameViewModel`.
- Feature engines are pure transformers called by `GameViewModel`.

---

## Engine Composition in `GameViewModel`

`GameViewModel` instantiates these pure engines:

- `DiplomacyViewModel`
- `ProductionLawViewModel`
- `AnalyticsSaveViewModel`
- `EspionageSecurityViewModel`
- `AdvancementViewModel`
- `TradeMarketViewModel`
- `GovernanceViewModel`

It orchestrates:

- Tick order
- Event blocking
- Save/load side effects
- Auto-tick lifecycle

---

## Monthly Tick Order (Critical)

`advanceTimeTick()` performs:

1. Date increment
2. Production/law tick
3. Budget settlement and baseline demographic drift
4. Diplomacy geopolitics + war battle simulation (if active war)
5. Security/espionage tick (can trigger coup game-over)
6. Science/society tick
7. Trade/market tick
8. Governance/UN tick
9. Historical snapshot append

Then:

- Stops auto-tick on game-over
- Else rolls crisis event chance

---

## Auto-Tick Behavior

- `toggleAutoTick()` starts/stops coroutine job
- Interval: `TICK_INTERVAL_MS = 1000`
- Auto-tick pauses progression when a blocking event exists
- Auto-tick is stopped when game-over is reached

---

## Event Blocking Model

- Active event is stored separately from `GameState` in `_currentActiveEvent`
- Most mutating user actions early-return if event is active
- Event choice resolves through `resolveEvent(choice)` and clears blocker

This protects consistency during crisis choice windows.

