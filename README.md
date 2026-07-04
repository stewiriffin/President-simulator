# President Simulator

A UI-heavy, menu-driven grand strategy management simulator built with **Kotlin**, **Jetpack Compose**, and **MVVM** (`StateFlow`).

Inspired by *Modern Age 2* (Oxiwyle): manage a nation's budget, ministries, infrastructure, and policies through dashboards—not a text adventure.

## Architecture

| Layer | Responsibility |
| --- | --- |
| `data/GameState.kt` | Immutable nation snapshot (`Vitals`, `Economy`, `Military`, `Diplomacy`) |
| `viewmodel/GameViewModel.kt` | Owns `MutableStateFlow<GameState>`, monthly tick, player actions |
| `ui/` | Material 3 Compose screens (HUD, Cabinet grid, Economy ministry) |

Simulation advances **one month per tick** via the **Next Month** button or **auto-play** (1 month / real second).

## Open in Android Studio

1. **File → Open** this folder (`President Simulator`).
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or device (API 26+).

## Controls

- **Skip / Play** in the top app bar: advance one month or auto-tick.
- **Cabinet** grid: open ministries (Economy is fully interactive).
- **Economy**: tax slider with live projections; build Factories / Farms / Housing in batches (1x, 10x, Max).

Persistence (Room / DataStore) is intentionally out of scope for this foundation.

# President-simulator
