# AI Context Pack - President Simulator

This folder is the canonical handoff pack for AI agents working on this repository.

## Read Order (Fast Onboarding)

1. `01_ARCHITECTURE_AND_RUNTIME.md`
2. `02_DOMAIN_STATE_MODELS.md`
3. `03_SIMULATION_ENGINES_AND_RULES.md`
4. `04_UI_NAVIGATION_AND_SCREENS.md`
5. `05_ASSETS_AUDIO_AND_MEDIA.md`
6. `06_BUILD_TEST_RELEASE_WORKFLOW.md`
7. `07_AI_TASK_PLAYBOOK.md`

---

## Scope of This Pack

This pack documents:

- Actual app runtime structure and call graph
- Major models and formulas used by gameplay
- What each engine does every month tick
- What screens are active in navigation vs legacy/unwired
- How image and audio systems work
- Safe development workflows, test strategy, and common pitfalls
- Practical "how-to" steps for common feature edits

---

## Core Facts

- App name: `President Simulator`
- Package: `com.presidentsimulator.game`
- Platform: Android (Compose)
- Orientation: Landscape
- Main state owner: `GameViewModel`
- Root screen shell: `GameNavigation`
- Global immutable state: `GameState`
- Tick cadence: one in-game month per tick

---

## Important Paths

- Entry: `app/src/main/java/com/presidentsimulator/game/MainActivity.kt`
- Navigation: `app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt`
- State root: `app/src/main/java/com/presidentsimulator/game/data/Models.kt`
- Orchestrator: `app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt`
- Engines: `app/src/main/java/com/presidentsimulator/game/viewmodel/*ViewModel.kt`
- Primary UI screens: `app/src/main/java/com/presidentsimulator/game/ui/screens/*.kt`
- Shared UI components: `app/src/main/java/com/presidentsimulator/game/ui/components/*.kt`
- Audio: `app/src/main/java/com/presidentsimulator/game/audio/*.kt`

---

## Existing Overview Files

- High-level single-file overview: `app-overview/APP_OVERVIEW_DETAILED.md`
- This folder: detailed AI-oriented, multi-file context pack

