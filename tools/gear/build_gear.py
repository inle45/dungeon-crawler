"""Emits DungeonGearCatalog.kt from gear_data.py, refusing to write if any item
breaks its rarity's secondary-bonus contract."""
import sys
from gear_data import DUNGEONS, PRIMARY

SECONDARY_COUNT = {"COMMON": 0, "UNCOMMON": 1, "RARE": 2, "EPIC": 3, "LEGENDARY": 3}
NEEDS_PASSIVE = {"LEGENDARY"}

errors, items_by_dungeon, all_ids = [], {}, set()

for const, dungeon_id, prefix, entries in DUNGEONS:
    if len(entries) != 20:
        errors.append(f"{const}: {len(entries)} objets, attendu 20")

    rendered = []
    for family, name, slot, rarity, stats, passive, heal in entries:
        item_id = f"{prefix}_{family}_{rarity.lower()}"
        if item_id in all_ids:
            errors.append(f"id dupliqué: {item_id}")
        all_ids.add(item_id)

        if slot == "CONSUMABLE":
            if heal <= 0:
                errors.append(f"{name}: consommable sans soin")
            if stats:
                errors.append(f"{name}: consommable avec des stats")
        else:
            primary = PRIMARY[slot]
            if stats.get(primary, 0) <= 0:
                errors.append(f"{name}: stat primaire '{primary}' absente pour le slot {slot}")
            secondary = len([k for k, v in stats.items() if k != primary and v])
            expected = SECONDARY_COUNT[rarity]
            if secondary != expected:
                errors.append(
                    f"{name} ({rarity}): {secondary} bonus secondaires, attendu {expected}")
            if rarity in NEEDS_PASSIVE and not passive:
                errors.append(f"{name}: légendaire sans passif")
            if rarity not in NEEDS_PASSIVE and passive:
                errors.append(f"{name}: passif sur un objet non légendaire")

        stat_args = ", ".join(f"{k} = {v}" for k, v in stats.items())
        stat_expr = f"StatBlock({stat_args})" if stat_args else "StatBlock.EMPTY"
        passive_expr = f"ItemPassive.{passive}" if passive else "ItemPassive.NONE"

        rendered.append(f"""        EquipmentItem(
            id = "{item_id}",
            name = "{name}",
            slot = EquipmentSlot.{slot},
            rarity = Rarity.{rarity},
            stats = {stat_expr},
            passive = {passive_expr},
            healPercent = {heal}f,
            iconRes = "gear_{item_id}",
            dungeonId = "{dungeon_id}",
            family = "{prefix}_{family}",
        ),""")
    items_by_dungeon[const] = (dungeon_id, rendered)

if errors:
    print("CONTRAT DE RARETÉ VIOLÉ :", file=sys.stderr)
    for e in errors:
        print("  -", e, file=sys.stderr)
    sys.exit(1)

blocks = []
for const, (dungeon_id, rendered) in items_by_dungeon.items():
    body = "\n".join(rendered)
    blocks.append(f"""    /** The 20 pieces that only drop inside [DungeonCatalog.{const}]. */
    val {const}_GEAR: List<EquipmentItem> = listOf(
{body}
    )
""")

out = f"""package com.dungeoncrawler.wearos.domain.catalog

import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.EquipmentSlot
import com.dungeoncrawler.wearos.domain.model.ItemPassive
import com.dungeoncrawler.wearos.domain.model.Rarity
import com.dungeoncrawler.wearos.domain.model.StatBlock

/**
 * Per-dungeon loot tables: 20 pieces each, matching the 20 monsters that can drop them.
 *
 * Items are grouped into families — a family is one piece of gear appearing at several rarities
 * (`Lame d'ossement` → `affûtée` → `runique`), so a player who keeps farming a dungeon sees the
 * same silhouette get better rather than a shapeless pile of unrelated names.
 *
 * Raw stats are written at their COMMON-tier value and scaled by [Rarity.statMultiplier] when
 * the item is built; percentage stats are never scaled (see [StatBlock.times]).
 *
 * GENERATED — edit `scratchpad/gear/gear_data.py` and re-run `build_gear.py`, which refuses to
 * emit this file if any item breaks its rarity's secondary-bonus contract.
 */
object DungeonGearCatalog {{

{"".join(blocks)}
    /** Every dungeon-specific piece, flattened. */
    val ALL: List<EquipmentItem> = CRYPT_GEAR + FORGE_GEAR + VOID_GEAR

    private val byDungeon: Map<String, List<EquipmentItem>> = ALL.groupBy {{ it.dungeonId!! }}

    fun gearOf(dungeonId: String): List<EquipmentItem> = byDungeon[dungeonId].orEmpty()

    fun gearOf(dungeonId: String, rarity: Rarity): List<EquipmentItem> =
        gearOf(dungeonId).filter {{ it.rarity == rarity }}
}}
"""

path = "/home/user/dungeon-crawler/app/src/main/java/com/dungeoncrawler/wearos/domain/catalog/DungeonGearCatalog.kt"
open(path, "w").write(out)

total = sum(len(r) for _, r in items_by_dungeon.values())
print(f"écrit {path}")
print(f"{total} objets · " + " · ".join(f"{k}: {len(v[1])}" for k, v in items_by_dungeon.items()))
print("contrat de rareté : OK")
