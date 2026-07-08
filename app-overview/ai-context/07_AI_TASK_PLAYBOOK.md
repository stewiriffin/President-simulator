# AI Task Playbook

This file tells an AI exactly how to implement common tasks in this app safely.

## General Rules for AI Edits

1. Treat `GameViewModel` as orchestrator, not formula host.
2. Put subsystem logic in the appropriate feature engine file.
3. Keep model updates immutable via `copy`.
4. Preserve event/game-over guards on gameplay actions.
5. Rebuild after edits (`assembleDebug`).

---

## Task Pattern A: Add a New Gameplay Mechanic

### Steps

1. Add/extend fields in relevant `data/*.kt` models.
2. Implement rule in corresponding engine (`viewmodel/*ViewModel.kt`).
3. Thread through tick pipeline if it is monthly.
4. Expose intent method in `GameViewModel`.
5. Add/adjust UI controls in target screen.
6. Add formatting helper if needed.
7. Build and smoke test.

### Example placements

- Economic calculation -> `ProductionLawViewModel` or economy action methods in `GameViewModel`
- War rule -> `DiplomacyViewModel`
- Security/coup rule -> `EspionageSecurityViewModel`
- UN rule -> `GovernanceViewModel`

---

## Task Pattern B: Add a New Screen

1. Create screen in `ui/screens`.
2. Add destination entry in `ui/navigation/GameDestination.kt`.
3. Register route in `GameNavigation`.
4. Add nav button entry in `MinistryBottomNav` (if user-facing).
5. Wire state + intents from `GameViewModel`.
6. Rebuild and route-test.

---

## Task Pattern C: Add a New Law

1. Add law to `LawCatalog.all`.
2. Ensure category and modifiers are correct.
3. Confirm enact thresholds/costs align with balance.
4. Verify `LawsScreen` shows it under correct tab.
5. Run tick to ensure modifiers apply as intended.

---

## Task Pattern D: Add a New Technology

1. Add technology entry to `TechCatalog.all`.
2. Set prerequisites and effect modifiers.
3. Confirm unlock/start gating rules.
4. Verify research progress and completion effects.

---

## Task Pattern E: Add a New Event

1. Add entry to `EventRepository.eventPool`.
2. Ensure consequences only touch valid fields.
3. Optional: add drawable `event_<id>` asset.
4. Verify `EventIllustration` fallback handles missing art.

---

## Task Pattern F: Add/Change Card Images

1. Add URL constants/helper mappings in `NssCardImages`.
2. Pass URLs into target components/screens.
3. Ensure `NssPhotoHeader` scrim still keeps text readable.
4. Test on slower network / offline fallback.

---

## Task Pattern G: Debug "Action Does Nothing"

Checklist:

1. Is there an active event? (many actions return early)
2. Is game over set?
3. Are budget/intel/influence prerequisites met?
4. Is a governance ban active (weapons/nuclear)?
5. Is the target ID valid (rival exists)?

---

## Task Pattern H: Balance Tuning

Prefer adjusting named constants first:

- Engine companion object constants
- Law/tech catalog values
- Base production conversion rates

After tuning, run at least 12 ticks to observe trend behavior.

---

## Troubleshooting Media Issues

If images appear missing:

1. Confirm internet/device connectivity
2. Confirm URL in `NssCardImages`
3. Verify no over-aggressive overlay hides image content
4. Check that screen uses `NssPhotoHeader` and non-null `imageUrl`

If audio appears missing:

1. Confirm raw assets exist for expected tracks/sfx
2. Confirm music/sfx toggles are enabled in settings
3. Inspect diagnostics shown in `SettingsAudioScreen`
4. Check Logcat and debug logs for `SoundPool` skipped raw asset warnings

---

## "Do Not Break" Checklist

- Keep `advanceTimeTick` order stable unless intentionally redesigning.
- Preserve crisis blocker behavior.
- Preserve game-over stop conditions.
- Keep save/load JSON compatible when possible.
- Keep navigation destination route strings stable.

