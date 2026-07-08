# Build, Test, and Release Workflow

## Environment Requirements

- Android Studio with SDK 35
- JDK 17
- Device/emulator API 26+
- Working `local.properties` SDK path

## Common Commands

From repo root:

- Build debug APK:
  - `./gradlew assembleDebug`
- Install to connected device:
  - `./gradlew installDebug`
- List devices:
  - `adb devices`

On Windows PowerShell, use:

- `.\gradlew assembleDebug --no-daemon`
- `.\gradlew installDebug --no-daemon`

---

## Validation Strategy After Changes

1. Compile check:
   - run `assembleDebug`
2. Install smoke test on real device:
   - run `installDebug`
3. Functional checks:
   - next turn tick works
   - event dialog blocks and resolves correctly
   - navigation routes open without crash
   - war/trade/research actions mutate state as expected
4. Media checks:
   - image cards load/fallback correctly
   - BGM/SFX settings screen is responsive

---

## Persistence Checks

After save/load changes:

- Save game
- Kill/relaunch app
- Load last automated save
- Verify:
  - state values restored
  - no stale active crisis lock
  - HUD and current route render correctly

---

## Git Guidance (Project Practice)

- Keep commits focused by concern (UI migration vs system logic vs assets).
- Avoid mixing large generated/reference folders with unrelated gameplay code unless intentional.
- Re-run build after resolving merge conflicts in UI component files.

---

## Known Risk Zones

- `GameViewModel.advanceTimeTick` ordering changes can create subtle balance regressions.
- Modifier changes in `NssPhotoHeader` can affect readability globally.
- Governance bans (nuclear/weapons) can silently block military purchase actions.
- Event blocking guard clauses can unintentionally disable UI actions if removed/altered.

