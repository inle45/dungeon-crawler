package com.dungeoncrawler.wearos.tile

import androidx.wear.protolayout.ResourceBuilders as ProtoResourceBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TimelineBuilders.Timeline
import androidx.wear.tiles.TimelineBuilders.TimelineEntry
import com.dungeoncrawler.wearos.domain.GameConstants
import com.dungeoncrawler.wearos.domain.catalog.DungeonCatalog
import com.dungeoncrawler.wearos.domain.model.Dungeon
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import com.dungeoncrawler.wearos.domain.usecase.ComputeHeroPowerUseCase
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Standalone Tile: current HP, the floor the hero stands on, and how far the next boss is —
 * all without launching the app.
 */
@OptIn(ExperimentalHorologistApi::class)
@AndroidEntryPoint
class DungeonCrawlerTileService : SuspendingTileService() {

    @Inject lateinit var heroRepository: HeroRepository
    @Inject lateinit var computeHeroPower: ComputeHeroPowerUseCase

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ProtoResourceBuilders.Resources =
        ProtoResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build()

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val hero = heroRepository.getHeroStats()
        val progress = heroRepository.getDungeonProgress()
        val power = computeHeroPower.once()
        val dungeon = DungeonCatalog.findById(progress.currentDungeonId) ?: DungeonCatalog.FIRST

        val layout = TileRenderer.render(
            dungeonName = dungeon.name,
            currentFloor = progress.currentFloor,
            totalFloors = Dungeon.FLOORS_PER_DUNGEON,
            currentHp = power.currentHp,
            maxHp = power.maxHp,
            stepsIntoBossCycle = hero.totalSteps % GameConstants.STEPS_PER_BOSS_ENCOUNTER,
            stepsPerBossEncounter = GameConstants.STEPS_PER_BOSS_ENCOUNTER,
            accentColorArgb = dungeon.accentColorArgb,
        )

        val timeline = Timeline.Builder()
            .addTimelineEntry(
                TimelineEntry.Builder()
                    .setLayout(LayoutElementBuilders.Layout.Builder().setRoot(layout).build())
                    .build(),
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTimeline(timeline)
            .setFreshnessIntervalMillis(FRESHNESS_INTERVAL_MILLIS)
            .build()
    }

    private companion object {
        const val RESOURCES_VERSION = "1"

        /** Steps accrue slowly; refreshing every 10 minutes keeps the tile honest without cost. */
        const val FRESHNESS_INTERVAL_MILLIS = 10 * 60 * 1000L
    }
}
