# Source of truth for the per-dungeon equipment catalog.
# Each entry: (family, name, slot, rarity, stats dict, passive, heal_percent)
#
# Rarity contract (enforced by build_gear.py):
#   COMMON     -> 1 primary stat, 0 secondary
#   UNCOMMON   -> 1 primary + 1 secondary
#   RARE       -> 1 primary + 2 secondary
#   EPIC       -> 1 primary + 3 secondary  (x1.15 on raw stats)
#   LEGENDARY  -> 1 primary + 3 secondary + unique passive (x1.30 on raw stats)
# Consumables are exempt: they carry a heal instead of a stat line.

# Primary stat per slot — the one the slot exists for.
PRIMARY = {
    "WEAPON": "attack",
    "ARMOR": "maxHp",
    "RING": "critRate",
    "RELIC": "magicPower",
}

CRYPT = [
    # --- Lame d'ossement family: the crypt's starter weapon ladder -----------
    ("bone_blade", "Lame d'ossement", "WEAPON", "COMMON",
     dict(attack=8), None, 0),
    ("bone_blade", "Lame d'ossement affûtée", "WEAPON", "UNCOMMON",
     dict(attack=14, critRate=6), None, 0),
    ("bone_blade", "Lame d'ossement runique", "WEAPON", "RARE",
     dict(attack=19, critRate=9, armorPierce=12), None, 0),

    # --- Faux du fossoyeur family ------------------------------------------
    ("gravedigger", "Faux du fossoyeur", "WEAPON", "UNCOMMON",
     dict(attack=15, lifeSteal=5), None, 0),
    ("gravedigger", "Faux du fossoyeur damné", "WEAPON", "EPIC",
     dict(attack=24, lifeSteal=11, critDamage=25, hpRegen=3), None, 0),

    ("censer", "Encensoir de cendres", "WEAPON", "RARE",
     dict(attack=17, magicPower=9, critRate=7), None, 0),

    ("first_king", "Sceptre du Premier Enseveli", "WEAPON", "LEGENDARY",
     dict(attack=30, critRate=14, critDamage=45, lifeSteal=14),
     "LIFE_STEAL", 0),

    # --- Armors -------------------------------------------------------------
    ("shroud", "Suaire élimé", "ARMOR", "COMMON",
     dict(maxHp=45), None, 0),
    ("shroud", "Suaire des veilleurs", "ARMOR", "UNCOMMON",
     dict(maxHp=85, defense=7), None, 0),
    ("shroud", "Suaire du chœur muet", "ARMOR", "RARE",
     dict(maxHp=120, defense=12, dodge=6), None, 0),

    ("ossuary_plate", "Plates d'ossuaire", "ARMOR", "RARE",
     dict(maxHp=135, defense=15, thorns=10), None, 0),
    ("ossuary_plate", "Plates d'ossuaire scellées", "ARMOR", "EPIC",
     dict(maxHp=200, defense=24, thorns=18, damageReduction=6), None, 0),

    ("widow", "Corset de la Veuve", "ARMOR", "LEGENDARY",
     dict(maxHp=230, defense=26, dodge=14, thorns=22),
     "RIPOSTE", 0),

    # --- Rings --------------------------------------------------------------
    ("candle_ring", "Anneau de cire", "RING", "COMMON",
     dict(critRate=5), None, 0),
    ("candle_ring", "Anneau de cire ardente", "RING", "UNCOMMON",
     dict(critRate=9, critDamage=18), None, 0),

    ("grave_ring", "Chevalière du caveau", "RING", "RARE",
     dict(critRate=11, critDamage=24, lootBonus=8), None, 0),
    ("grave_ring", "Chevalière du caveau profané", "RING", "EPIC",
     dict(critRate=15, critDamage=38, lootBonus=14, attack=6), None, 0),

    # --- Relics -------------------------------------------------------------
    ("whisper", "Fiole de murmures", "RELIC", "UNCOMMON",
     dict(magicPower=13, hpRegen=2), None, 0),
    ("whisper", "Fiole de murmures anciens", "RELIC", "EPIC",
     dict(magicPower=26, hpRegen=5, defense=9, lootBonus=10), None, 0),

    # --- Consumable ---------------------------------------------------------
    ("crypt_draught", "Élixir de sépulture", "CONSUMABLE", "UNCOMMON",
     {}, None, 0.65),
]

FORGE = [
    ("obsidian_edge", "Tranchant d'obsidienne", "WEAPON", "COMMON",
     dict(attack=13), None, 0),
    ("obsidian_edge", "Tranchant d'obsidienne trempé", "WEAPON", "UNCOMMON",
     dict(attack=21, armorPierce=10), None, 0),
    ("obsidian_edge", "Tranchant d'obsidienne noire", "WEAPON", "RARE",
     dict(attack=28, armorPierce=18, critDamage=22), None, 0),

    ("forge_hammer", "Marteau de fonderie", "WEAPON", "UNCOMMON",
     dict(attack=23, thorns=8), None, 0),
    ("forge_hammer", "Marteau du contremaître", "WEAPON", "EPIC",
     dict(attack=34, thorns=16, critDamage=32, defense=8), None, 0),

    ("molten_lash", "Fouet de magma", "WEAPON", "RARE",
     dict(attack=26, lifeSteal=9, critRate=8), None, 0),

    ("vulcan", "Marteau Primordial de Vulcan", "WEAPON", "LEGENDARY",
     dict(attack=44, armorPierce=30, critDamage=55, thorns=20),
     "ARMOR_PIERCE", 0),

    ("slag_mail", "Cotte de scorie", "ARMOR", "COMMON",
     dict(maxHp=70), None, 0),
    ("slag_mail", "Cotte de scorie durcie", "ARMOR", "UNCOMMON",
     dict(maxHp=120, defense=11), None, 0),
    ("slag_mail", "Cotte de scorie vitrifiée", "ARMOR", "RARE",
     dict(maxHp=165, defense=18, damageReduction=5), None, 0),

    ("glass_carapace", "Carapace de verre noir", "ARMOR", "RARE",
     dict(maxHp=175, defense=20, thorns=14), None, 0),
    ("glass_carapace", "Carapace du golem fondu", "ARMOR", "EPIC",
     dict(maxHp=255, defense=31, thorns=24, damageReduction=8), None, 0),

    ("blast_heart", "Plastron du Haut-Fourneau", "ARMOR", "LEGENDARY",
     dict(maxHp=300, defense=35, damageReduction=14, hpRegen=8),
     "DAMAGE_REDUCTION", 0),

    ("ember_ring", "Anneau de braise", "RING", "COMMON",
     dict(critRate=7), None, 0),
    ("ember_ring", "Anneau de braise vive", "RING", "UNCOMMON",
     dict(critRate=12, critDamage=24), None, 0),

    ("anvil_ring", "Sceau de l'enclume", "RING", "RARE",
     dict(critRate=13, critDamage=30, defense=10), None, 0),
    ("anvil_ring", "Sceau de l'enclume hurlante", "RING", "EPIC",
     dict(critRate=18, critDamage=46, defense=15, armorPierce=12), None, 0),

    ("bellows", "Soufflet ardent", "RELIC", "UNCOMMON",
     dict(magicPower=20, lifeSteal=6), None, 0),
    ("bellows", "Soufflet du Cœur de Fonte", "RELIC", "EPIC",
     dict(magicPower=38, lifeSteal=12, hpRegen=6, lootBonus=12), None, 0),

    ("forge_draught", "Trempe du forgeron", "CONSUMABLE", "RARE",
     {}, None, 0.80),
]

VOID = [
    ("angle_edge", "Arête impossible", "WEAPON", "COMMON",
     dict(attack=20), None, 0),
    ("angle_edge", "Arête impossible repliée", "WEAPON", "UNCOMMON",
     dict(attack=32, dodge=7), None, 0),
    ("angle_edge", "Arête impossible récursive", "WEAPON", "RARE",
     dict(attack=42, dodge=11, critDamage=30), None, 0),

    ("rift_claw", "Griffe de faille", "WEAPON", "UNCOMMON",
     dict(attack=34, armorPierce=14), None, 0),
    ("rift_claw", "Griffe de faille béante", "WEAPON", "EPIC",
     dict(attack=50, armorPierce=26, lifeSteal=13, critRate=11), None, 0),

    ("echo_blade", "Lame en écho", "WEAPON", "RARE",
     dict(attack=40, critRate=13, critDamage=28), None, 0),

    ("hem", "Ourlet du Réel", "WEAPON", "LEGENDARY",
     dict(attack=62, critRate=20, critDamage=70, armorPierce=35),
     "ARMOR_PIERCE", 0),

    ("void_weave", "Tissage du vide", "ARMOR", "COMMON",
     dict(maxHp=105), None, 0),
    ("void_weave", "Tissage du vide dense", "ARMOR", "UNCOMMON",
     dict(maxHp=175, dodge=9), None, 0),
    ("void_weave", "Tissage du vide insondable", "ARMOR", "RARE",
     dict(maxHp=235, dodge=14, defense=22), None, 0),

    ("mirror_plate", "Plates-miroirs", "ARMOR", "RARE",
     dict(maxHp=250, defense=26, thorns=20), None, 0),
    ("mirror_plate", "Plates-miroirs affamées", "ARMOR", "EPIC",
     dict(maxHp=350, defense=40, thorns=32, lifeSteal=10), None, 0),

    ("repeater", "Robe de Celle Qui Se Répète", "ARMOR", "LEGENDARY",
     dict(maxHp=400, defense=44, dodge=22, damageReduction=16),
     "RIPOSTE", 0),

    ("star_ring", "Anneau de constellation morte", "RING", "COMMON",
     dict(critRate=10), None, 0),
    ("star_ring", "Anneau de constellation éteinte", "RING", "UNCOMMON",
     dict(critRate=17, critDamage=34), None, 0),

    ("axiom_ring", "Sceau d'axiome brisé", "RING", "RARE",
     dict(critRate=19, critDamage=42, magicPower=18), None, 0),
    ("axiom_ring", "Sceau d'axiome effondré", "RING", "EPIC",
     dict(critRate=25, critDamage=62, magicPower=28, lootBonus=18), None, 0),

    ("silence", "Nœud de silence", "RELIC", "UNCOMMON",
     dict(magicPower=32, dodge=8), None, 0),
    ("silence", "Nœud de silence absolu", "RELIC", "EPIC",
     dict(magicPower=58, dodge=15, hpRegen=10, damageReduction=9), None, 0),

    ("void_draught", "Gorgée de néant", "CONSUMABLE", "EPIC",
     {}, None, 1.0),
]

DUNGEONS = [("CRYPT", "dng_crypt", "crypt", CRYPT),
            ("FORGE", "dng_forge", "forge", FORGE),
            ("VOID", "dng_void", "void", VOID)]
