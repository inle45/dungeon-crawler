package com.dungeoncrawler.wearos.domain

object GameConstants {
    const val STEPS_PER_MICRO_EVENT = 200
    const val STEPS_PER_BOSS_ENCOUNTER = 2000
    const val SPRITE_ANIMATION_FRAME_MILLIS = 150

    /**
     * Frames in the hero's stitched idle sheet. Monsters carry their own
     * [com.dungeoncrawler.wearos.domain.model.Monster.spriteFrameCount] — bestiary art is
     * generated as single stills, so this constant is the hero's alone.
     */
    const val HERO_SPRITE_FRAME_COUNT = 4
}
