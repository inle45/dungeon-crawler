# Dungeon Crawler — Wear OS

Standalone Wear OS dungeon-crawler built with Kotlin, Jetpack Compose for Wear OS, and
Horologist. Progression is driven passively by real steps (Health Services) and resolved
actively in short boss fights on the wrist.

**There are no levels and no XP.** Every point of hero power comes from equipped gear.

## Graphics constraints

- **Pure OLED black background** (`#000000`) everywhere, no scrims or gradients — see
  `core/theme/Color.kt` / `DungeonCrawlerTheme.kt`. Off pixels stay off, which matters for
  battery life on always-visible Wear displays.
- **100% Pixel Lab AI sprites**, transparent PNG, committed under
  `app/src/main/res/drawable-nodpi/`:
  - `hero_idle_spritesheet.png`, `boss_idle_spritesheet.png` — 4-frame idle loops, single row,
    stitched from PixelLab's per-frame output.
  - `icon_attack_sword.png`, `icon_defense_shield.png`, `icon_spell_wand.png` — combat actions.
  - `icon_item_armor.png`, `icon_item_ring.png`, `icon_item_relic.png`, `icon_item_potion.png` —
    inventory slot icons.
  - `mon_*.png` — 60 bestiary sprites, one per monster (64px mobs, 96px mini-bosses,
    112px supreme bosses), single-frame stills.
  - New sprites follow the same pipeline: generate with PixelLab, stitch multi-frame animations
    into one horizontal strip of 4-6 frames, drop the PNG into `drawable-nodpi`.
- **`PixelSpriteAnimation`** (`core/sprite/PixelSpriteAnimation.kt`) decodes a spritesheet once
  (`rememberSpriteSheet`) and draws a sub-rectangle of that single `ImageBitmap` per frame on a
  `Canvas`, so an animation never allocates per-frame bitmaps.

## Architecture

Clean Architecture in three layers, packages under `com.dungeoncrawler.wearos`, wired with
Hilt (`di/`):

```
domain/        pure Kotlin: models, catalogs, repository interfaces, use cases
data/          Room DB, Health Services passive monitoring, repository implementations
presentation/  MVI screens (Compose for Wear OS) + Tiles service
core/          theme, haptics, sprite rendering — shared by presentation and tiles
```

Each screen is MVI with `StateFlow`: `presentation/mvi/MviViewModel` exposes a single
`StateFlow<State>` plus a one-shot `Flow<Effect>`, driven by `Intent`s from the UI.

## Progression by equipment

`HeroStats.BASE` is the hero's naked line (100 HP, 6 ATK, 2 DEF, 5 % crit, 4 MAG). Everything
above it is gear:

```
TotalStat = BaseHeroStat + Sum(EquippedItems.Stats)
```

resolved in exactly one place, `ComputeHeroPowerUseCase`.

**Slots** — `WEAPON`, `ARMOR`, `RING`, `RELIC`, `CONSUMABLE`; one item each, equipping swaps.

**Five rarity tiers** (`domain/model/Rarity.kt`), each adding one more secondary bonus:

| Tier | Colour | Secondary bonuses | Multiplier |
|---|---|---|---|
| `COMMON` | grey | 0 | ×1.00 |
| `UNCOMMON` | green | 1 | ×1.00 |
| `RARE` | blue | 2 | ×1.00 |
| `EPIC` | purple | 3 | ×1.15 |
| `LEGENDARY` | gold | 3 + unique passive | ×1.30 |

Legendary and epic passives (`domain/model/ItemPassive.kt`) are resolved during combat:
life steal, riposte, armor pierce, damage reduction.

The ten starting items live in `domain/catalog/EquipmentCatalog.kt` and are seeded into Room on
first launch (`data/local/db/DatabaseSeeder.kt`).

## Dungeons, floors and bestiary

Three themed dungeons (`domain/catalog/DungeonCatalog.kt`), each **10 floors**:

1. *Crypte des Âmes Oubliées* — ×1.0 difficulty
2. *Fournaise d'Obsidienne* — ×1.6
3. *Sanctuaire du Vide Rampant* — ×2.4

Each dungeon owns exactly **20 monsters**: 15 micro-mobs, 4 mini-bosses, 1 supreme boss.
Drop tables are weighted twice — by the table row's own weight and by the item's
`Rarity.lootWeight` — so a legendary listed beside a common still lands far less often.

### The loop

1. `data/health/StepTrackingService.kt` (a Health Services `PassiveListenerService`) receives
   step and heart-rate updates in the background.
2. `TrackStepsUseCase` persists the running total and fires the thresholds:
   - **every 200 steps** → `ResolveMicroEventUseCase` rolls loot, a trap, or an off-screen
     micro-mob kill against the current dungeon's pool, and writes it straight to Room.
   - **every 2000 steps** → `TriggerBossEncounterUseCase` spawns the floor's boss (a mini-boss
     on floors 1-9, the supreme boss on floor 10) with the `BOSS_ALERT` haptic.
3. `BossCombatScreen` — hero/boss face-off, three actions (attack / defense / spell) selectable
   by touch or by the rotating crown (`RotarySelector`). Every outcome maps to its own haptic
   pattern.
4. Killing a mini-boss advances one floor. Killing the supreme boss on floor 10 opens
   **`DungeonClearScreen`**, offering two crown-navigable choices:
   - *Refaire le donjon* — restart at floor 1 to farm gear.
   - *Donjon suivant* — unlock and enter the next dungeon.

## Screens

| Screen | Role |
|---|---|
| `DungeonNavigationScreen` | Dungeon name, floor pips, «Étage X/10», HP, gauge to next boss |
| `BossCombatScreen` | Circular face-off, 3 actions, touch + rotary, dedicated haptics |
| `InventoryScreen` | Horologist `ScalingLazyColumn`, rarity colours, green/red stat diff before equipping |
| `DungeonClearScreen` | Victory screen with the farm / next-dungeon choice |
| `DungeonCrawlerTileService` | Tile: dungeon, floor X/10, HP, boss-progress bar |

## Persistence

Room v3 (`data/local/db/`), three entities:

- `HeroStateEntity` — base stats, current HP, steps, current dungeon + floor, unlocked/cleared sets.
- `InventoryItemEntity` — id, name, slot, rarity, bonus stats, passive, `isEquipped`.
- `MonsterEntity` — stats, packed drop table, sprite asset and its frame count.

## Building

Requires Android Studio (Koala+) with the Wear OS SDK, or the command line with `ANDROID_HOME`
set:

```
./gradlew :app:assembleDebug
```

(The Gradle wrapper jar is not vendored — run `gradle wrapper` once locally, or open the project
in Android Studio, before invoking `./gradlew`.)
