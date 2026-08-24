package com.dungeoncrawler.wearos.tile

import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TimelineBuilders.Timeline
import androidx.wear.tiles.TimelineBuilders.TimelineEntry
import com.dungeoncrawler.wearos.domain.GameConstants
import com.dungeoncrawler.wearos.domain.repository.PlayerRepository
import com.google.android.horologist.tiles.SuspendingTileService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Standalone Tile: shows current HP and a progress bar toward the next boss encounter. */
@AndroidEntryPoint
class DungeonCrawlerTileService : SuspendingTileService() {

    @Inject
    lateinit var playerRepository: PlayerRepository

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ResourceBuilders.Resources =
        ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build()

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val player = playerRepository.observePlayerStats().first()
        val stepsIntoBossCycle = player.totalSteps % GameConstants.STEPS_PER_BOSS_ENCOUNTER

        val layout = TileRenderer.render(
            currentHp = player.currentHp,
            maxHp = player.maxHp,
            stepsIntoBossCycle = stepsIntoBossCycle,
            stepsPerBossEncounter = GameConstants.STEPS_PER_BOSS_ENCOUNTER,
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
            .setTileTimeline(timeline)
            .build()
    }

    private companion object {
        const val RESOURCES_VERSION = "1"
    }
}
