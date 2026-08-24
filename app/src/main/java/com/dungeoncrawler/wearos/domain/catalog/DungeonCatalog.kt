package com.dungeoncrawler.wearos.domain.catalog

import com.dungeoncrawler.wearos.domain.catalog.EquipmentCatalog as Gear
import com.dungeoncrawler.wearos.domain.model.Dungeon
import com.dungeoncrawler.wearos.domain.model.LootDrop
import com.dungeoncrawler.wearos.domain.model.Monster
import com.dungeoncrawler.wearos.domain.model.MonsterRole

/**
 * The three themed dungeons and their bestiaries. Each dungeon holds exactly 20 monsters:
 * 15 micro-mobs (200-step encounters), 4 mini-bosses (floors 1-9 boss threshold) and one
 * supreme boss guarding floor 10.
 *
 * Drop tables are weighted by rarity band: micro-mobs mostly yield commons, mini-bosses open up
 * uncommon and rare gear, and only supreme bosses can hand out epics and legendaries.
 */
object DungeonCatalog {

    val CRYPT = Dungeon(
        id = "dng_crypt",
        name = "Crypte des Âmes Oubliées",
        subtitle = "Ossements, cierges éteints et murmures",
        order = 1,
        accentColorArgb = 0xFF6FA8DC.toInt(),
        difficultyMultiplier = 1.0f,
    )

    val FORGE = Dungeon(
        id = "dng_forge",
        name = "Fournaise d'Obsidienne",
        subtitle = "Verre volcanique et enclumes hurlantes",
        order = 2,
        accentColorArgb = 0xFFE07A3C.toInt(),
        difficultyMultiplier = 1.6f,
    )

    val VOID = Dungeon(
        id = "dng_void",
        name = "Sanctuaire du Vide Rampant",
        subtitle = "Là où la géométrie cesse de tenir",
        order = 3,
        accentColorArgb = 0xFF9C4DFF.toInt(),
        difficultyMultiplier = 2.4f,
    )

    val ALL: List<Dungeon> = listOf(CRYPT, FORGE, VOID)

    val FIRST: Dungeon = CRYPT

    private val byId: Map<String, Dungeon> = ALL.associateBy { it.id }

    fun findById(id: String): Dungeon? = byId[id]

    /** The next dungeon in [Dungeon.order], or null when the hero has cleared everything. */
    fun nextAfter(dungeonId: String): Dungeon? {
        val current = findById(dungeonId) ?: return null
        return ALL.filter { it.order > current.order }.minByOrNull { it.order }
    }

    // ---------------------------------------------------------------- drop tables

    private val COMMON_TABLE = listOf(
        LootDrop(Gear.RUSTY_SWORD.id, weight = 40),
        LootDrop(Gear.WORN_LEATHER_TUNIC.id, weight = 40),
        LootDrop(Gear.MAJOR_HEALING_POTION.id, weight = 20),
    )

    private val MINI_BOSS_TABLE = listOf(
        LootDrop(Gear.FORGED_PLATE_CUIRASS.id, weight = 30),
        LootDrop(Gear.RING_OF_VITALITY.id, weight = 30),
        LootDrop(Gear.SHADOW_DAGGER.id, weight = 20),
        LootDrop(Gear.ABYSS_AMULET.id, weight = 15),
        LootDrop(Gear.MAJOR_HEALING_POTION.id, weight = 5),
    )

    private val SUPREME_TABLE = listOf(
        LootDrop(Gear.ABYSSAL_TITAN_ARMOR.id, weight = 35),
        LootDrop(Gear.SLAYERS_SIGNET.id, weight = 35),
        LootDrop(Gear.ANCESTRAL_RUNEBLADE.id, weight = 20),
        LootDrop(Gear.SHADOW_DAGGER.id, weight = 10),
    )

    // ---------------------------------------------------------------- bestiaries

    private fun mob(dungeon: Dungeon, index: Int, name: String, hp: Int, atk: Int, def: Int) = Monster(
        id = "${dungeon.id}_mob_$index",
        name = name,
        dungeonId = dungeon.id,
        role = MonsterRole.MICRO_MOB,
        maxHp = (hp * dungeon.difficultyMultiplier).toInt(),
        attack = (atk * dungeon.difficultyMultiplier).toInt(),
        defense = (def * dungeon.difficultyMultiplier).toInt(),
        dropTable = COMMON_TABLE,
    )

    private fun miniBoss(dungeon: Dungeon, index: Int, name: String, hp: Int, atk: Int, def: Int) = Monster(
        id = "${dungeon.id}_mini_$index",
        name = name,
        dungeonId = dungeon.id,
        role = MonsterRole.MINI_BOSS,
        maxHp = (hp * dungeon.difficultyMultiplier).toInt(),
        attack = (atk * dungeon.difficultyMultiplier).toInt(),
        defense = (def * dungeon.difficultyMultiplier).toInt(),
        dropTable = MINI_BOSS_TABLE,
    )

    private fun supreme(dungeon: Dungeon, name: String, hp: Int, atk: Int, def: Int) = Monster(
        id = "${dungeon.id}_supreme",
        name = name,
        dungeonId = dungeon.id,
        role = MonsterRole.SUPREME_BOSS,
        maxHp = (hp * dungeon.difficultyMultiplier).toInt(),
        attack = (atk * dungeon.difficultyMultiplier).toInt(),
        defense = (def * dungeon.difficultyMultiplier).toInt(),
        dropTable = SUPREME_TABLE,
    )

    private val CRYPT_BESTIARY: List<Monster> = listOf(
        mob(CRYPT, 1, "Rat de sépulture", hp = 18, atk = 4, def = 1),
        mob(CRYPT, 2, "Squelette ébréché", hp = 26, atk = 6, def = 2),
        mob(CRYPT, 3, "Chauve-souris pâle", hp = 15, atk = 5, def = 0),
        mob(CRYPT, 4, "Goule affamée", hp = 34, atk = 8, def = 3),
        mob(CRYPT, 5, "Cierge animé", hp = 20, atk = 7, def = 1),
        mob(CRYPT, 6, "Bras rampant", hp = 22, atk = 6, def = 2),
        mob(CRYPT, 7, "Spectre voilé", hp = 30, atk = 9, def = 2),
        mob(CRYPT, 8, "Fossoyeur damné", hp = 38, atk = 8, def = 4),
        mob(CRYPT, 9, "Araignée d'ossuaire", hp = 24, atk = 7, def = 2),
        mob(CRYPT, 10, "Moine sans visage", hp = 32, atk = 9, def = 3),
        mob(CRYPT, 11, "Lanterne folle", hp = 19, atk = 8, def = 1),
        mob(CRYPT, 12, "Vermine de reliquaire", hp = 21, atk = 6, def = 2),
        mob(CRYPT, 13, "Cadavre sanglé", hp = 40, atk = 7, def = 5),
        mob(CRYPT, 14, "Écho de pleureuse", hp = 28, atk = 10, def = 1),
        mob(CRYPT, 15, "Gardien de niche", hp = 36, atk = 9, def = 4),
        miniBoss(CRYPT, 1, "Ossuaire Ambulant", hp = 90, atk = 14, def = 6),
        miniBoss(CRYPT, 2, "Prêtre des Cendres", hp = 105, atk = 16, def = 7),
        miniBoss(CRYPT, 3, "Veuve du Caveau", hp = 120, atk = 18, def = 8),
        miniBoss(CRYPT, 4, "Chœur des Sans-Noms", hp = 140, atk = 20, def = 9),
        supreme(CRYPT, "Le Premier Enseveli", hp = 220, atk = 26, def = 12),
    )

    private val FORGE_BESTIARY: List<Monster> = listOf(
        mob(FORGE, 1, "Éclat de scorie", hp = 22, atk = 6, def = 2),
        mob(FORGE, 2, "Salamandre de coulée", hp = 28, atk = 9, def = 2),
        mob(FORGE, 3, "Automate ébréché", hp = 34, atk = 7, def = 5),
        mob(FORGE, 4, "Souffleur de braise", hp = 25, atk = 10, def = 1),
        mob(FORGE, 5, "Marteau hanté", hp = 30, atk = 11, def = 3),
        mob(FORGE, 6, "Nuée de suie", hp = 20, atk = 8, def = 1),
        mob(FORGE, 7, "Ouvrier calciné", hp = 36, atk = 9, def = 4),
        mob(FORGE, 8, "Gargouille d'obsidienne", hp = 42, atk = 10, def = 6),
        mob(FORGE, 9, "Ver de magma", hp = 33, atk = 12, def = 2),
        mob(FORGE, 10, "Bouche de fournaise", hp = 27, atk = 13, def = 1),
        mob(FORGE, 11, "Enclume vivante", hp = 46, atk = 8, def = 7),
        mob(FORGE, 12, "Cendrier rampant", hp = 24, atk = 9, def = 2),
        mob(FORGE, 13, "Fondeur masqué", hp = 38, atk = 11, def = 4),
        mob(FORGE, 14, "Étincelle vorace", hp = 21, atk = 14, def = 0),
        mob(FORGE, 15, "Colosse de laitier", hp = 50, atk = 10, def = 8),
        miniBoss(FORGE, 1, "Contremaître Calciné", hp = 120, atk = 18, def = 8),
        miniBoss(FORGE, 2, "Golem de Verre Noir", hp = 145, atk = 19, def = 11),
        miniBoss(FORGE, 3, "Fileuse de Fonte", hp = 135, atk = 22, def = 8),
        miniBoss(FORGE, 4, "Cœur de Haut-Fourneau", hp = 165, atk = 24, def = 10),
        supreme(FORGE, "Vulcan, Marteau Primordial", hp = 280, atk = 32, def = 15),
    )

    private val VOID_BESTIARY: List<Monster> = listOf(
        mob(VOID, 1, "Fragment errant", hp = 26, atk = 9, def = 2),
        mob(VOID, 2, "Œil sans orbite", hp = 30, atk = 11, def = 2),
        mob(VOID, 3, "Pli de l'espace", hp = 34, atk = 10, def = 4),
        mob(VOID, 4, "Chuchoteur inversé", hp = 28, atk = 13, def = 1),
        mob(VOID, 5, "Marcheur d'angles", hp = 38, atk = 11, def = 5),
        mob(VOID, 6, "Nœud de silence", hp = 24, atk = 12, def = 2),
        mob(VOID, 7, "Miroir affamé", hp = 32, atk = 14, def = 3),
        mob(VOID, 8, "Tisseur de faille", hp = 40, atk = 12, def = 6),
        mob(VOID, 9, "Écho non euclidien", hp = 29, atk = 15, def = 1),
        mob(VOID, 10, "Prisme carnivore", hp = 36, atk = 13, def = 4),
        mob(VOID, 11, "Ombre à rebours", hp = 31, atk = 14, def = 3),
        mob(VOID, 12, "Vide-pèlerin", hp = 44, atk = 11, def = 7),
        mob(VOID, 13, "Constellation morte", hp = 27, atk = 16, def = 1),
        mob(VOID, 14, "Griffe de nulle part", hp = 35, atk = 15, def = 3),
        mob(VOID, 15, "Rumeur solidifiée", hp = 48, atk = 12, def = 8),
        miniBoss(VOID, 1, "Le Sixième Angle", hp = 160, atk = 24, def = 10),
        miniBoss(VOID, 2, "Chorégraphe du Néant", hp = 180, atk = 26, def = 11),
        miniBoss(VOID, 3, "Celle Qui Se Répète", hp = 200, atk = 28, def = 12),
        miniBoss(VOID, 4, "Axiome Brisé", hp = 225, atk = 30, def = 14),
        supreme(VOID, "L'Ourlet du Réel", hp = 360, atk = 40, def = 20),
    )

    val BESTIARY: List<Monster> = CRYPT_BESTIARY + FORGE_BESTIARY + VOID_BESTIARY

    private val bestiaryByDungeon: Map<String, List<Monster>> = BESTIARY.groupBy { it.dungeonId }

    fun bestiaryOf(dungeonId: String): List<Monster> = bestiaryByDungeon[dungeonId].orEmpty()

    fun microMobsOf(dungeonId: String): List<Monster> =
        bestiaryOf(dungeonId).filter { it.role == MonsterRole.MICRO_MOB }

    fun miniBossesOf(dungeonId: String): List<Monster> =
        bestiaryOf(dungeonId).filter { it.role == MonsterRole.MINI_BOSS }

    fun supremeBossOf(dungeonId: String): Monster? =
        bestiaryOf(dungeonId).firstOrNull { it.role == MonsterRole.SUPREME_BOSS }
}
