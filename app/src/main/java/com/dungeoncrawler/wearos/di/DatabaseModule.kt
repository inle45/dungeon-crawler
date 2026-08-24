package com.dungeoncrawler.wearos.di

import android.content.Context
import androidx.room.Room
import com.dungeoncrawler.wearos.data.local.db.AppDatabase
import com.dungeoncrawler.wearos.data.local.db.dao.HeroDao
import com.dungeoncrawler.wearos.data.local.db.dao.InventoryDao
import com.dungeoncrawler.wearos.data.local.db.dao.MonsterDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            // The save is a local run, not user data worth a migration path across schema churn.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideHeroDao(database: AppDatabase): HeroDao = database.heroDao()

    @Provides
    fun provideInventoryDao(database: AppDatabase): InventoryDao = database.inventoryDao()

    @Provides
    fun provideMonsterDao(database: AppDatabase): MonsterDao = database.monsterDao()
}
