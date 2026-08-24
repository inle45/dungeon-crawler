# Dungeon Crawler — Wear OS

Standalone Wear OS dungeon-crawler built with Kotlin, Jetpack Compose for Wear OS, and
Horologist. Progression is driven passively by real steps (Health Services) and resolved
actively in short boss fights on the wrist.

## Graphics constraints

- **Pure OLED black background** (`#000000`) everywhere, no scrims or gradients — see
  `core/theme/Color.kt` / `DungeonCrawlerTheme.kt`. Off pixels stay off, which matters for
  battery life on always-visible Wear displays.
- **100% Pixel Lab AI sprites**, transparent PNG. The hero, boss and the three action-button
  icons in this repo were generated through the PixelLab MCP server (`create_character`,
  `animate_character` with the `breathing-idle` template, `create_image_pixflux`) and committed
  under `app/src/main/res/drawable-nodpi/`:
  - `hero_idle_spritesheet.png`, `boss_idle_spritesheet.png` — 4-frame idle loops, single row,
    stitched from PixelLab's per-frame output with Pillow (`scratchpad/assets/stitch.py`).
  - `icon_attack_sword.png`, `icon_defense_shield.png`, `icon_spell_wand.png` — combat action
    icons.
  - Any new sprite should be produced the same way: generate with PixelLab, stitch multi-frame
    animations into one horizontal strip of 4-6 frames, drop the PNG into `drawable-nodpi`.
- **`PixelSpriteAnimation`** (`core/sprite/PixelSpriteAnimation.kt`) decodes a spritesheet once
  (`rememberSpriteSheet`, `core/sprite/SpriteSheetLoader.kt`) and draws a sub-rectangle of that
  single `ImageBitmap` per frame on a `Canvas`, so an animation never allocates per-frame
  bitmaps — bounded to 1-6 frames by `SpriteSheet`.

## Architecture

Clean Architecture in three layers, packages under `com.dungeoncrawler.wearos`, wired with
Hilt (`di/`):

```
domain/        pure Kotlin: models, repository interfaces, use cases (no Android deps)
data/          Room DB, Health Services passive monitoring, repository implementations
presentation/  MVI screens (Compose for Wear OS) + Tiles service
core/          theme, haptics, sprite rendering — shared by presentation and tiles
```

Each screen is MVI with `StateFlow`: `presentation/mvi/MviViewModel` exposes a single
`StateFlow<State>` plus a one-shot `Flow<Effect>` (navigation, haptics-triggering events),
driven by `Intent`s from the UI. See `presentation/home/HomeContract.kt` and
`presentation/combat/BossCombatContract.kt`.

### Gameplay loop

1. `data/health/StepTrackingService.kt` — a `PassiveListenerService` registered with Health
   Services' `PassiveMonitoringClient` — receives step and heart-rate updates in the background
   and pushes them into `HealthRepositoryImpl`'s shared flows.
2. `domain/usecase/TrackStepsUseCase.kt` collects step deltas, persists the running total via
   `PlayerRepository`, and fires thresholds from `domain/GameConstants.kt`:
   - every 200 steps → `ResolveMicroEventUseCase` rolls a loot / trap / micro-mob event and
     writes the result straight to Room.
   - every 2000 steps → `TriggerBossEncounterUseCase` spawns a `BossEncounter` and fires the
     `BOSS_ALERT` haptic pattern.
3. `HomeViewModel` observes `GameProgressRepository.gameState` and navigates to
   `BossCombatScreen` when a `BossEncounterTriggered` state appears.
4. `BossCombatScreen` — circular hero/boss face-off, 3 `ActionButton`s (Attack / Defense /
   Spell) selectable by touch or by rotating the crown (`RotaryActionSelector`, built on
   `Modifier.onRotaryScrollEvent`). `ExecuteCombatActionUseCase` resolves each turn; every
   outcome maps to a dedicated `HapticFeedbackManager` pattern (critical hit, parry, damage
   taken, spell cast).
5. `data/local/db/AppDatabase.kt` (Room) persists player stats, equipped gear, total steps and
   current floor across process death.
6. `tile/DungeonCrawlerTileService.kt` — a Horologist `SuspendingTileService` — renders current
   HP and a boss-progress bar (`TileRenderer.kt`) without launching the app.

## Building

Requires Android Studio (Koala+) with the Wear OS SDK, or the command line with `ANDROID_HOME`
set:

```
./gradlew :app:assembleDebug
```

(The Gradle wrapper jar is not vendored in this commit — run `gradle wrapper` once locally, or
open the project in Android Studio, before invoking `./gradlew`.)
