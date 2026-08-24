package com.dungeoncrawler.wearos.di

import com.dungeoncrawler.wearos.data.health.HealthRepositoryImpl
import com.dungeoncrawler.wearos.data.repository.GameProgressRepositoryImpl
import com.dungeoncrawler.wearos.data.repository.HeroRepositoryImpl
import com.dungeoncrawler.wearos.data.repository.InventoryRepositoryImpl
import com.dungeoncrawler.wearos.data.repository.MonsterRepositoryImpl
import com.dungeoncrawler.wearos.domain.repository.GameProgressRepository
import com.dungeoncrawler.wearos.domain.repository.HealthRepository
import com.dungeoncrawler.wearos.domain.repository.HeroRepository
import com.dungeoncrawler.wearos.domain.repository.InventoryRepository
import com.dungeoncrawler.wearos.domain.repository.MonsterRepository
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
    abstract fun bindHeroRepository(impl: HeroRepositoryImpl): HeroRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindMonsterRepository(impl: MonsterRepositoryImpl): MonsterRepository

    @Binds
    @Singleton
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository

    @Binds
    @Singleton
    abstract fun bindGameProgressRepository(impl: GameProgressRepositoryImpl): GameProgressRepository
}
