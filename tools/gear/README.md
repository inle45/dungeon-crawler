# Gear catalog generator

`DungeonGearCatalog.kt` is generated, not hand-edited.

- `gear_data.py` — the 60 dungeon items as plain data: family, name, slot, rarity, stats.
- `build_gear.py` — validates every item against its rarity's secondary-bonus contract, then
  emits the Kotlin. It exits non-zero and writes nothing if any item is off-contract, so a
  mis-tuned entry can't reach the app.

```
cd tools/gear && python3 build_gear.py
```
