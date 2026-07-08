# President Simulator - Detailed Application Overview

## 1) What This App Is

`President Simulator` is a Kotlin/Android strategy simulation game built with Jetpack Compose.  
The player governs a nation across monthly turns, balancing economy, security, diplomacy, law, research, trade, and global governance.

At a high level, the app combines:

- A deterministic simulation tick (one month at a time)
- A central immutable game state model
- Feature-specific simulation engines
- A Compose-first UI shell with ministry-based navigation
- A persistent save/load pipeline
- A dynamic audio layer (BGM + SFX)

The current visual implementation uses an NSS v3-inspired gamified UI style with photo-backed cards, parchment backgrounds, panel cards, progress bars, and mission-oriented HUD affordances.

---

## 2) Platform and Build Configuration

From `app/build.gradle.kts`:

- **Namespace / Application ID:** `com.presidentsimulator.game`
- **Compile / Target SDK:** 35
- **Min SDK:** 26
- **Language Level:** Java 17 / Kotlin JVM target 17
- **UI Toolkit:** Jetpack Compose
- **Navigation:** `androidx.navigation:navigation-compose`
- **State / Lifecycle:** Lifecycle Runtime + ViewModel Compose
- **Serialization:** `kotlinx-serialization-json`
- **Image Loading:** `coil-compose`

From `AndroidManifest.xml`:

- INTERNET permission is enabled for remote image loading.
- App is locked to **landscape orientation** for the main activity.
- Entry point is `.MainActivity`.

---

## 3) Runtime Entry Flow

### `MainActivity`

`MainActivity` is minimal and intentionally orchestration-only:

1. Initializes `GameAudioManager`
2. Enables edge-to-edge
3. Applies app theme (`PresidentSimulatorTheme`)
4. Mounts `GameNavigation(viewModel = gameViewModel)`

When the activity is finishing, it releases the global audio instance.

---

## 4) Architectural Pattern

The app follows a practical MVVM + state-reducer style:

- **Model layer (`data/`)**  
  Immutable data classes (`GameState` + subsystem states).

- **ViewModel layer (`viewmodel/`)**  
  `GameViewModel` owns global state and routes actions to domain engines.

- **UI layer (`ui/`)**  
  Compose screens + reusable design components + navigation shell.

- **Audio layer (`audio/`)**  
  Handles BGM transitions and SFX playback independently of screen code.

Key principle: almost every gameplay action produces a new state via `copy`, making state transitions explicit and predictable.

---

## 5) Core Domain Model (`GameState`)

`GameState` is the root simulation snapshot and contains all active systems:

- Time (`month`, `year`)
- Vitals (budget, approval, population)
- Economy
- Military
- Diplomacy
- Production
- Legal
- Analytics
- Internal Security
- Espionage
- Research
- Society
- Trade + Market
- Global Governance
- Game-over status

It also exposes computed properties like:

- `netIncome`
- `effectiveCombatStrength`
- `effectiveProductionMultiplier`
- formatted date labels

The simulation starts from `GameState.initial()` with seeded values so the first turn is immediately playable.

---

## 6) Simulation Engine and Time Progression

### `GameViewModel`

`GameViewModel` is the authoritative runtime coordinator. It owns:

- `StateFlow<GameState>`
- `StateFlow<GameEvent?>` for blocking crises
- `StateFlow<Boolean>` for auto-tick state
- save/load feedback state

### Monthly Tick (`advanceTimeTick`)

A single tick performs a multi-system pipeline:

1. Advance month/year
2. Process production/law dynamics
3. Settle budget and demographic drift
4. Simulate diplomacy and active wars
5. Process security and espionage
6. Process science/society effects
7. Process trade and market effects
8. Process UN/governance outcomes
9. Record analytics snapshot

Then:

- Rolls for event trigger (`EVENT_CHANCE_PER_TICK = 0.15`)
- Stops auto-tick if game-over is reached

### Auto-Tick

- Driven by coroutine job in `viewModelScope`
- 1 second interval (`TICK_INTERVAL_MS = 1000`)
- Pauses if an active event blocks progression

---

## 7) Gameplay Subsystems

Each subsystem has both state and action APIs exposed through `GameViewModel`.

### Economy & Infrastructure

- Tax rate management
- Revenue/expense projection
- Infrastructure builds (factory, farm, housing, power plant, mine)
- Monthly net income derived from tax + exports - expenses/upkeep

### Military & Diplomacy

- War declaration and armistice
- Treaty negotiation
- Deployment posture (offensive/defensive/mobilized)
- Personnel and hardware procurement
- War simulation impacts combat outcomes and losses

### Internal Security & Espionage

- Security protocol toggles
- Coup/instability pressure management
- Spy recruitment, deployment, mission success estimation
- Mission lifecycle management (active/cancelled/resolved)

### Science & Society

- Tech unlock and active research tracks
- Extra research funding tiers
- Ministry funding controls
- Religion policy shifts
- University and science generation effects

### Trade & Markets

- Bilateral trade deal proposals/cancellations
- Tariff setting and revenue forecasting
- Spot buy/sell commodity operations
- Price calculations influenced by relations and deal type

### Governance (UN / Alliances)

- Resolution proposal and voting windows
- Country vote influence/bribery actions
- Alliance creation/dissolution
- Resolution modifiers affect military/geopolitical conditions

### Laws & Production

- Law enact/repeal mechanics
- Production tick updates (energy/food/materials/goods economics)
- Modifiers propagated into economy and capability calculations

### Events / Crises

- Randomly selected from static event repository
- Present blocking modal until resolved
- Choice consequences are applied atomically to state

---

## 8) Navigation and Screen Topology

### `GameNavigation`

Main routes:

- Dashboard
- Economy
- Military
- Diplomacy
- Secret Service (Intelligence)
- Science
- Laws/Society
- Governance (UN)
- Audio Settings

Global shell responsibilities:

- Top HUD controls
- Bottom ministry nav
- Event crisis dialog overlay
- Game-over dialog
- Route-driven screen rendering with `NavHost`

This creates a single consistent frame while features swap as central content.

---

## 9) UI System and Visual Language

The UI is built from reusable NSS components:

- `NssScreenHeader`
- `NssPanel`
- `NssGameBar` / `NssXpBar`
- `NssBadge`
- `NssPhotoHeader`
- specialized cards for sectors, units, operations, diplomacy, and crises

### Image Model

`NssCardImages` centralizes all remote image URLs and helper selectors for:

- ministry banners
- sector/unit/category visuals
- event and nation card image mapping

`NssPhotoHeader` uses Coil to fetch images with:

- crossfade
- caching policies
- gradient fallbacks
- configurable scrims for text readability

This ensures image-heavy UI remains stable even with network variability.

---

## 10) Audio System

`GameAudioManager` is a singleton service with:

- **BGM:** two-player crossfade strategy (`MediaPlayer`)
- **SFX:** low-latency `SoundPool`
- runtime toggles and volume controls
- diagnostics log for debugging missing assets/state

Track switching is state-aware:

- War -> WAR track
- High coup risk -> CRISIS track
- Otherwise -> PEACE track

Audio behavior is integrated through `GameAudioBridge` and direct UI action click hooks.

---

## 11) Persistence and Save/Load

Persistence is managed through analytics/save engine hooks in `GameViewModel`:

- Export/import full state as JSON
- Automated save payload stored in `SharedPreferences`
- Restoration resets active event blocking state
- Save/load status surfaced as feedback state

This approach keeps persistence robust and human-portable (JSON).

---

## 12) Current Strengths

- Clear separation of feature engines under a single orchestrator
- Immutable state updates simplify reasoning and debugging
- Broad system coverage (economy to geopolitics)
- Strong UI consistency after NSS v3 migration
- Device-ready deployment/build flow (`assembleDebug`, `installDebug`)

---

## 13) Practical Extension Points

If you expand the app, the cleanest seams are:

- Add new monthly mechanics in feature engines + tick pipeline
- Add new ministries by extending `GameDestination` and nav content
- Add new crises via `EventRepository`
- Add balancing by adjusting formulas in model/engine functions
- Add richer persistence (multiple slots/cloud) using current JSON export format
- Add telemetry by piggybacking on analytics snapshots

---

## 14) File/Module Orientation (Quick Map)

- `MainActivity.kt` - app bootstrap
- `ui/navigation/GameNavigation.kt` - global shell + routing
- `viewmodel/GameViewModel.kt` - state owner + action facade
- `data/*.kt` - domain state and formulas
- `viewmodel/*ViewModel.kt` - subsystem engines
- `ui/screens/*.kt` - ministry screens
- `ui/components/*.kt` - reusable NSS UI primitives
- `audio/*.kt` - BGM/SFX runtime

---

## 15) One-Line Mental Model

This app is a **single-state, multi-engine geopolitical simulation**, presented through a **photo-rich, card-based command interface**, where each player action or monthly tick transforms immutable state and immediately re-renders the entire nation dashboard.

