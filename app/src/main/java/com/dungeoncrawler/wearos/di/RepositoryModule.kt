package com.dungeoncrawler.wearos.di

import com.dungeoncrawler.wearos.data.health.HealthRepositoryImpl
import com.dungeoncrawler.wearos.data.repository.GameProgressRepositoryImpl
import com.dungeoncrawler.wearos.data.repository.PlayerRepositoryImpl
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.HealthRepository
import com.dungeoncrawler.wearos.domain.repository.PlayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds
    @Singleton
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository

    @Binds
    @Singleton
    abstract fun bindGameProgressRepository(impl: GameProgressRepositoryImpl): GameProgressRepository
}
