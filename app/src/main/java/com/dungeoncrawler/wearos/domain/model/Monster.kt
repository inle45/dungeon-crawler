package com.dungeoncrawler.wearos.domain.model

/** Where a monster sits in a dungeon's 10-floor structure. */
enum class MonsterRole {
    /** Resolved passively every 200 steps. */
    MICRO_MOB,

    /** Fought actively at the 2000-step boss threshold of floors 1-9. */
    MINI_BOSS,

    /** Floor 10's dungeon-ending fight. */
    SUPREME_BOSS,
}

/**
 * One entry of a dungeon's 20-monster bestiary.
 *
 * @param dropTable weighted loot rolled when the monster dies; see [LootDrop].
 * @param spriteRes drawable name of the Pixel Lab AI sprite sheet used to render it.
 */
data class Monster(
    val id: String,
    val name: String,
    val dungeonId: String,
    val role: MonsterRole,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val dropTable: List<LootDrop>,
    val spriteRes: String = "boss_idle_spritesheet",
) {
    val isBoss: Boolean get() = role != MonsterRole.MICRO_MOB
}

/**
 * One row of a weighted drop table. [weight] is relative within its table; [chancePercent] is
 * the probability the table rolls anything at all for this row's rarity band.
 */
data class LootDrop(
    val itemId: String,
    val weight: Int,
)
