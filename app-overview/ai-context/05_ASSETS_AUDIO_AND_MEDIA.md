# Assets, Audio, and Media

## Manifest and Permissions

`AndroidManifest.xml` includes:

- `android.permission.INTERNET` (required for remote card/header images)

Main activity is landscape-only and serves as app launcher.

---

## Image Pipeline

### Sources

- Central URL registry: `ui/components/NssCardImages.kt`
- URLs are mostly Unsplash-based and mapped by feature categories

### Rendering component

- `NssPhotoHeader` (`NssPhotoHeader.kt`) wraps Coil loading.
- Uses:
  - crossfade
  - memory/disk cache policies
  - fallback gradients on loading/error
  - optional scrims/overlays

### Practical behavior

- Missing/invalid URL does not crash UI; gradient fallback remains visible.
- UI readability is enforced via bottom/side scrims for text overlays.

### Event illustrations

`EventIllustration.kt` strategy:

1. Try local drawable named `event_<normalized_event_id>`
2. If missing, use remote fallback image via `NssPhotoHeader`

This supports both offline packaged art and remote thematic images.

---

## Audio Engine (`GameAudioManager`)

`GameAudioManager` is singleton + thread-safe.

### BGM

- Uses dual `MediaPlayer` instances for crossfading tracks.
- Track selection from game pressure:
  - War -> WAR
  - High coup risk -> CRISIS
  - Else -> PEACE

### SFX

- Uses `SoundPool`
- Preloads known `SfxType` entries
- Missing raw assets are tolerated gracefully (logged, skipped)

### Runtime controls

- `musicEnabled`, `sfxEnabled`
- `musicVolume`, `sfxVolume`
- diagnostics log exposed for UI

---

## Audio Integration

- `GameAudioBridge.kt` keeps BGM in sync with game conditions.
- `GameAudioCrisisEffect` reacts to active crises.
- UI interaction points call click SFX (`playClick`) frequently.

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

