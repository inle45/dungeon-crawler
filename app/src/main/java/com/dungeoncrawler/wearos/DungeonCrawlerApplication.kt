package com.dungeoncrawler.wearos

import android.app.Application
import com.dungeoncrawler.wearos.data.local.db.DatabaseSeeder
import com.dungeoncrawler.wearos.di.ApplicationScope
import com.dungeoncrawler.wearos.domain.model.GameState
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.HealthRepository
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import com.dungeoncrawler.wearos.domain.usecase.TrackStepsUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class DungeonCrawlerApplication : Application() {

    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var healthRepository: HealthRepository
    @Inject lateinit var heroRepository: HeroRepository
    @Inject lateinit var gameProgressRepository: GameProgressRepository
    @Inject lateinit var trackStepsUseCase: TrackStepsUseCase
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            // Seeding must land before anything reads the bestiary or the starter loadout.
            databaseSeeder.seedIfNeeded()
            gameProgressRepository.setGameState(
                GameState.Exploring(heroRepository.getDungeonProgress()),
            )
            healthRepository.startPassiveMonitoring()
            trackStepsUseCase()
        }
    }
}
