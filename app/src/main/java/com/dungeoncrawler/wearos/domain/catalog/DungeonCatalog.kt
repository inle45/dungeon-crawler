package com.dungeoncrawler.wearos.domain.catalog

import com.dungeoncrawler.wearos.domain.catalog.EquipmentCatalog as Gear
import com.dungeoncrawler.wearos.domain.model.Dungeon
import com.dungeoncrawler.wearos.domain.model.LootDrop
import com.dungeoncrawler.wearos.domain.model.Monster
import com.dungeoncrawler.wearos.domain.model.MonsterRole
import com.dungeoncrawler.wearos.domain.model.Rarity

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

    /**
     * Tables are built from the dungeon's own 20-piece pool in [DungeonGearCatalog], plus the
     * base catalog that drops anywhere. A monster's rank decides which rarity bands it can open:
     * micro-mobs stay in the low tiers, only supreme bosses can hand out legendaries.
     */
    private fun tableFor(dungeonId: String, role: MonsterRole): List<LootDrop> {
        val bands: Map<Rarity, Int> = when (role) {
            MonsterRole.MICRO_MOB -> mapOf(
                Rarity.COMMON to 60,
                Rarity.UNCOMMON to 30,
                Rarity.RARE to 10,
            )
            MonsterRole.MINI_BOSS -> mapOf(
                Rarity.UNCOMMON to 40,
                Rarity.RARE to 40,
                Rarity.EPIC to 20,
            )
            MonsterRole.SUPREME_BOSS -> mapOf(
                Rarity.RARE to 25,
                Rarity.EPIC to 45,
                Rarity.LEGENDARY to 30,
            )
        }

        return bands.flatMap { (rarity, bandWeight) ->
            val pool = DungeonGearCatalog.gearOf(dungeonId, rarity) + Gear.ofRarity(rarity)
            if (pool.isEmpty()) {
                emptyList()
            } else {
                // Split the band's weight across its pool so adding an item to a rarity dilutes
                // that rarity rather than making the whole band more likely.
                val perItem = (bandWeight / pool.size).coerceAtLeast(1)
                pool.map { LootDrop(it.id, weight = perItem) }
            }
        }
    }

    // ---------------------------------------------------------------- bestiaries

    private fun mob(
        dungeon: Dungeon,
        index: Int,
        name: String,
        sprite: String,
        hp: Int,
        atk: Int,
        def: Int,
    ) = Monster(
        id = "${dungeon.id}_mob_$index",
        name = name,
        dungeonId = dungeon.id,
        role = MonsterRole.MICRO_MOB,
        maxHp = (hp * dungeon.difficultyMultiplier).toInt(),
        attack = (atk * dungeon.difficultyMultiplier).toInt(),
        defense = (def * dungeon.difficultyMultiplier).toInt(),
        dropTable = tableFor(dungeon.id, MonsterRole.MICRO_MOB),
        spriteRes = sprite,
    )

    private fun miniBoss(
        dungeon: Dungeon,
        index: Int,
        name: String,
        sprite: String,
        hp: Int,
        atk: Int,
        def: Int,
    ) = Monster(
        id = "${dungeon.id}_mini_$index",
        name = name,
        dungeonId = dungeon.id,
        role = MonsterRole.MINI_BOSS,
        maxHp = (hp * dungeon.difficultyMultiplier).toInt(),
        attack = (atk * dungeon.difficultyMultiplier).toInt(),
        defense = (def * dungeon.difficultyMultiplier).toInt(),
        dropTable = tableFor(dungeon.id, MonsterRole.MINI_BOSS),
        spriteRes = sprite,
    )

    private fun supreme(
        dungeon: Dungeon,
        name: String,
        sprite: String,
        hp: Int,
        atk: Int,
        def: Int,
    ) = Monster(
        id = "${dungeon.id}_supreme",
        name = name,
        dungeonId = dungeon.id,
        role = MonsterRole.SUPREME_BOSS,
        maxHp = (hp * dungeon.difficultyMultiplier).toInt(),
        attack = (atk * dungeon.difficultyMultiplier).toInt(),
        defense = (def * dungeon.difficultyMultiplier).toInt(),
        dropTable = tableFor(dungeon.id, MonsterRole.SUPREME_BOSS),
        spriteRes = sprite,
    )

    private val CRYPT_BESTIARY: List<Monster> = listOf(
        mob(CRYPT, 1, "Rat de sépulture", "mon_crypt_rat", hp = 18, atk = 4, def = 1),
        mob(CRYPT, 2, "Squelette ébréché", "mon_crypt_skeleton", hp = 26, atk = 6, def = 2),
        mob(CRYPT, 3, "Chauve-souris pâle", "mon_crypt_bat", hp = 15, atk = 5, def = 0),
        mob(CRYPT, 4, "Goule affamée", "mon_crypt_ghoul", hp = 34, atk = 8, def = 3),
        mob(CRYPT, 5, "Cierge animé", "mon_crypt_candle", hp = 20, atk = 7, def = 1),
        mob(CRYPT, 6, "Bras rampant", "mon_crypt_hand", hp = 22, atk = 6, def = 2),
        mob(CRYPT, 7, "Spectre voilé", "mon_crypt_spectre", hp = 30, atk = 9, def = 2),
        mob(CRYPT, 8, "Fossoyeur damné", "mon_crypt_gravedigger", hp = 38, atk = 8, def = 4),
        mob(CRYPT, 9, "Araignée d'ossuaire", "mon_crypt_spider", hp = 24, atk = 7, def = 2),
        mob(CRYPT, 10, "Moine sans visage", "mon_crypt_monk", hp = 32, atk = 9, def = 3),
        mob(CRYPT, 11, "Lanterne folle", "mon_crypt_lantern", hp = 19, atk = 8, def = 1),
        mob(CRYPT, 12, "Vermine de reliquaire", "mon_crypt_vermin", hp = 21, atk = 6, def = 2),
        mob(CRYPT, 13, "Cadavre sanglé", "mon_crypt_corpse", hp = 40, atk = 7, def = 5),
        mob(CRYPT, 14, "Écho de pleureuse", "mon_crypt_weeper", hp = 28, atk = 10, def = 1),
        mob(CRYPT, 15, "Gardien de niche", "mon_crypt_gargoyle", hp = 36, atk = 9, def = 4),
        miniBoss(CRYPT, 1, "Ossuaire Ambulant", "mon_crypt_ossuary", hp = 90, atk = 14, def = 6),
        miniBoss(CRYPT, 2, "Prêtre des Cendres", "mon_crypt_ashpriest", hp = 105, atk = 16, def = 7),
        miniBoss(CRYPT, 3, "Veuve du Caveau", "mon_crypt_widow", hp = 120, atk = 18, def = 8),
        miniBoss(CRYPT, 4, "Chœur des Sans-Noms", "mon_crypt_choir", hp = 140, atk = 20, def = 9),
        supreme(CRYPT, "Le Premier Enseveli", "mon_crypt_firstburied", hp = 220, atk = 26, def = 12),
    )

    private val FORGE_BESTIARY: List<Monster> = listOf(
        mob(FORGE, 1, "Éclat de scorie", "mon_forge_slag", hp = 22, atk = 6, def = 2),
        mob(FORGE, 2, "Salamandre de coulée", "mon_forge_salamander", hp = 28, atk = 9, def = 2),
        mob(FORGE, 3, "Automate ébréché", "mon_forge_automaton", hp = 34, atk = 7, def = 5),
        mob(FORGE, 4, "Souffleur de braise", "mon_forge_emberblower", hp = 25, atk = 10, def = 1),
        mob(FORGE, 5, "Marteau hanté", "mon_forge_hammer", hp = 30, atk = 11, def = 3),
        mob(FORGE, 6, "Nuée de suie", "mon_forge_soot", hp = 20, atk = 8, def = 1),
        mob(FORGE, 7, "Ouvrier calciné", "mon_forge_worker", hp = 36, atk = 9, def = 4),
        mob(FORGE, 8, "Gargouille d'obsidienne", "mon_forge_gargoyle", hp = 42, atk = 10, def = 6),
        mob(FORGE, 9, "Ver de magma", "mon_forge_magmaworm", hp = 33, atk = 12, def = 2),
        mob(FORGE, 10, "Bouche de fournaise", "mon_forge_furnacemaw", hp = 27, atk = 13, def = 1),
        mob(FORGE, 11, "Enclume vivante", "mon_forge_anvil", hp = 46, atk = 8, def = 7),
        mob(FORGE, 12, "Cendrier rampant", "mon_forge_ashcrawler", hp = 24, atk = 9, def = 2),
        mob(FORGE, 13, "Fondeur masqué", "mon_forge_smelter", hp = 38, atk = 11, def = 4),
        mob(FORGE, 14, "Étincelle vorace", "mon_forge_spark", hp = 21, atk = 14, def = 0),
        mob(FORGE, 15, "Colosse de laitier", "mon_forge_slagcolossus", hp = 50, atk = 10, def = 8),
        miniBoss(FORGE, 1, "Contremaître Calciné", "mon_forge_foreman", hp = 120, atk = 18, def = 8),
        miniBoss(FORGE, 2, "Golem de Verre Noir", "mon_forge_glassgolem", hp = 145, atk = 19, def = 11),
        miniBoss(FORGE, 3, "Fileuse de Fonte", "mon_forge_spinner", hp = 135, atk = 22, def = 8),
        miniBoss(FORGE, 4, "Cœur de Haut-Fourneau", "mon_forge_blastheart", hp = 165, atk = 24, def = 10),
        supreme(FORGE, "Vulcan, Marteau Primordial", "mon_forge_vulcan", hp = 280, atk = 32, def = 15),
    )

    private val VOID_BESTIARY: List<Monster> = listOf(
        mob(VOID, 1, "Fragment errant", "mon_void_fragment", hp = 26, atk = 9, def = 2),
        mob(VOID, 2, "Œil sans orbite", "mon_void_eye", hp = 30, atk = 11, def = 2),
        mob(VOID, 3, "Pli de l'espace", "mon_void_fold", hp = 34, atk = 10, def = 4),
        mob(VOID, 4, "Chuchoteur inversé", "mon_void_whisperer", hp = 28, atk = 13, def = 1),
        mob(VOID, 5, "Marcheur d'angles", "mon_void_anglewalker", hp = 38, atk = 11, def = 5),
        mob(VOID, 6, "Nœud de silence", "mon_void_silence", hp = 24, atk = 12, def = 2),
        mob(VOID, 7, "Miroir affamé", "mon_void_mirror", hp = 32, atk = 14, def = 3),
        mob(VOID, 8, "Tisseur de faille", "mon_void_riftweaver", hp = 40, atk = 12, def = 6),
        mob(VOID, 9, "Écho non euclidien", "mon_void_echo", hp = 29, atk = 15, def = 1),
        mob(VOID, 10, "Prisme carnivore", "mon_void_prism", hp = 36, atk = 13, def = 4),
        mob(VOID, 11, "Ombre à rebours", "mon_void_shadow", hp = 31, atk = 14, def = 3),
        mob(VOID, 12, "Vide-pèlerin", "mon_void_pilgrim", hp = 44, atk = 11, def = 7),
        mob(VOID, 13, "Constellation morte", "mon_void_constellation", hp = 27, atk = 16, def = 1),
        mob(VOID, 14, "Griffe de nulle part", "mon_void_claw", hp = 35, atk = 15, def = 3),
        mob(VOID, 15, "Rumeur solidifiée", "mon_void_rumor", hp = 48, atk = 12, def = 8),
        miniBoss(VOID, 1, "Le Sixième Angle", "mon_void_sixthangle", hp = 160, atk = 24, def = 10),
        miniBoss(VOID, 2, "Chorégraphe du Néant", "mon_void_choreographer", hp = 180, atk = 26, def = 11),
        miniBoss(VOID, 3, "Celle Qui Se Répète", "mon_void_repeater", hp = 200, atk = 28, def = 12),
        miniBoss(VOID, 4, "Axiome Brisé", "mon_void_axiom", hp = 225, atk = 30, def = 14),
        supreme(VOID, "L'Ourlet du Réel", "mon_void_hem", hp = 360, atk = 40, def = 20),
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
